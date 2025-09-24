package com.ajaxjs.livestream.service;


import com.ajaxjs.livestream.controller.LiveController;
import com.ajaxjs.livestream.model.LiveRecording;
import com.ajaxjs.livestream.model.LiveRoom;
import com.ajaxjs.livestream.model.LiveStream;
import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.crud.Entity;
import com.ajaxjs.sqlman.model.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LiveStreamService implements LiveController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    LiveRecordingService liveRecordingService;

    @Value("${live.srs.server-url}")
    private String srsServerUrl;

    @Value("${live.srs.api-url}")
    private String srsApiUrl;

    @Value("${live.srs.http-flv-url}")
    private String httpFlvUrl;

    @Value("${live.srs.hls-url}")
    private String hlsUrl;

    @Value("${live.push.key-check-enabled}")
    private boolean keyCheckEnabled;

    @Value("${live.push.auth-expire}")
    private long authExpire;

    @Value("${live.push.auth-key}")
    private String authKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 创建直播间
     */
    @Transactional
    @Override
    public LiveRoom createLiveRoom(LiveRoom liveRoom) {
        // 生成推流密钥
        String streamKey = generateStreamKey(liveRoom.getUserId());
        liveRoom.setStreamKey(streamKey);

        // 构建推流地址
        String pushUrl = buildPushUrl(streamKey);
        liveRoom.setStreamUrl(pushUrl);
        // 构建播放地址
        liveRoom.setHlsUrl(hlsUrl + "/" + streamKey + "/" + streamKey + ".m3u8");
        liveRoom.setFlvUrl(httpFlvUrl + "/" + streamKey + "/" + streamKey + ".flv");
        // 设置初始状态
        liveRoom.setStatus(0);
        liveRoom.setViewCount(0L);
        liveRoom.setLikeCount(0L);
        liveRoom.setCreatedAt(LocalDateTime.now());
        liveRoom.setUpdatedAt(LocalDateTime.now());
        Entity.instance().input(liveRoom).create();// 保存到数据库

        return liveRoom;
    }

    @Override
    public LiveRoom getLiveRoom(Long roomId) {
        return getLiveRoomById(roomId);
    }

    public static LiveRoom getLiveRoomById(Long roomId) {
        LiveRoom liveRoom = Sql.instance().input("SELECT * FROM live_room WHERE id = ?", roomId).query(LiveRoom.class);

        if (liveRoom == null)
            throw new IllegalArgumentException("直播间不存在");

        return liveRoom;
    }

    /**
     * 生成推流密钥
     */
    private String generateStreamKey(Long userId) {
        String baseKey = userId + "_" + System.currentTimeMillis(); // 生成基于用户ID和时间戳的唯一密钥
        return DigestUtils.md5DigestAsHex(baseKey.getBytes());
    }

    /**
     * 构建推流地址
     */
    private String buildPushUrl(String streamKey) {
        StringBuilder sb = new StringBuilder(srsServerUrl);
        sb.append("/").append(streamKey);

        // 如果启用了推流验证
        if (keyCheckEnabled) {
            long expireTimestamp = System.currentTimeMillis() / 1000 + authExpire;
            String authString = streamKey + "-" + expireTimestamp + "-" + authKey;
            String authToken = DigestUtils.md5DigestAsHex(authString.getBytes());

            sb.append("?auth_key=").append(authToken).append("&expire=").append(expireTimestamp);
        }

        return sb.toString();
    }

    /**
     * 开始直播
     */
    @Transactional
    @Override
    public LiveRoom startLiveStream(Long roomId) {
        LiveRoom liveRoom = getLiveRoom(roomId);

        // 更新直播间状态为直播中
        liveRoom.setStatus(1);
        liveRoom.setStartTime(LocalDateTime.now());
        Entity.instance().input(liveRoom).update();

        // 创建直播流记录
        LiveStream liveStream = new LiveStream();
        liveStream.setRoomId(roomId);
        liveStream.setStreamId(liveRoom.getStreamKey());
        liveStream.setProtocol("rtmp");
        liveStream.setStatus(1);
        liveStream.setCreatedAt(LocalDateTime.now());
        liveStream.setUpdatedAt(LocalDateTime.now());
        Entity.instance().input(liveStream).create();

        // 更新Redis缓存中的活跃直播间
        redisTemplate.opsForSet().add("live:active_rooms", String.valueOf(roomId));

        return liveRoom;
    }

    /**
     * 结束直播
     */
    @Transactional
    @Override
    public LiveRoom endLiveStream(Long roomId) {
        LiveRoom liveRoom = getLiveRoom(roomId);

        // 更新直播间状态为已结束
        liveRoom.setStatus(2);
        liveRoom.setEndTime(LocalDateTime.now());
        Entity.instance().input(liveRoom).update();

        // 更新直播流状态
        LiveStream liveStream = Sql.instance().input("SELECT * FROM live_stream WHERE room_id = ? AND status = ?", roomId, 1).query(LiveStream.class);

        if (liveStream != null) {
            liveStream.setStatus(2);
            liveStream.setUpdatedAt(LocalDateTime.now());
            Entity.instance().input(liveStream).update();
        }

        redisTemplate.opsForSet().remove("live:active_rooms", String.valueOf(roomId));  // 从Redis中移除活跃直播间

        return liveRoom;
    }

    /**
     * 获取当前活跃的直播间列表
     */
    @Override
    public PageResult<LiveRoom> getActiveLiveRooms(int page, int size) {
        return Sql.instance().input("SELECT * FROM live_room WHERE status = 1 ORDER BY view_count DESC").page(LiveRoom.class, page, size);
    }

    /**
     * 获取热门直播间
     */
    @Override
    public List<LiveRoom> getHotLiveRooms(int limit) {
        // "SELECT * FROM live_room WHERE status = 1 ORDER BY view_count DESC LIMIT #{limit}"
        return Sql.instance().input("SELECT * FROM live_room WHERE status = 1 ORDER BY view_count DESC LIMIT ?", limit).queryList(LiveRoom.class);
    }

    /**
     * 增加直播间观看人数
     */
    public void incrementViewCount(Long roomId) {
        // 使用Redis进行计数
        String key = "live:room:" + roomId + ":view_count";
        redisTemplate.opsForValue().increment(key);

        // 定期同步到数据库
        if (Math.random() < 0.1) {  // 10%概率同步，减少数据库压力
            String countStr = redisTemplate.opsForValue().get(key);
            if (countStr != null) {
                long count = Long.parseLong(countStr);

                LiveRoom room = new LiveRoom();
                room.setId(roomId);
                room.setViewCount(count);
                Entity.instance().input(room).update();
            }
        }
    }

    @Override
    public LiveRecording startRecording(Long roomId) {
        return liveRecordingService.startRecording(roomId);
    }

    @Override
    public LiveRecording stopRecording(Long recordingId) {
        return liveRecordingService.stopRecording(recordingId);
    }

    @Override
    public List<LiveRecording> getRecordings(Long roomId, int page, int size) {
        return liveRecordingService.getRecordings(roomId, page, size);
    }

    /**
     * 校验推流密钥
     */
    public boolean validateStreamKey(String streamKey, String token, String expire) {
        if (!keyCheckEnabled)
            return true;

        try {
            long expireTimestamp = Long.parseLong(expire);
            long currentTime = System.currentTimeMillis() / 1000;

            // 检查是否过期
            if (currentTime > expireTimestamp)
                return false;

            // 验证token
            String authString = streamKey + "-" + expire + "-" + authKey;
            String calculatedToken = DigestUtils.md5DigestAsHex(authString.getBytes());

            return calculatedToken.equals(token);
        } catch (Exception e) {
            log.error("验证推流密钥异常", e);
            return false;
        }
    }

    /**
     * 处理SRS回调 - 流发布
     */
    public void handleStreamPublish(String app, String stream) {
        // 查找对应的直播间
        LiveRoom liveRoom = Sql.instance().input("SELECT * FROM live_room WHERE stream_key = ?", stream).query(LiveRoom.class);

        if (liveRoom != null && liveRoom.getStatus() == 0) {
            startLiveStream(liveRoom.getId()); // 更新直播间状态
            log.info("直播流发布成功: app={}, stream={}, roomId={}", app, stream, liveRoom.getId());
        }
    }

    /**
     * 处理SRS回调 - 流关闭
     */
    public void handleStreamClose(String app, String stream) {
        // 查找对应的直播间
        LiveRoom liveRoom = Sql.instance().input("SELECT * FROM live_room WHERE stream_key = ?", stream).query(LiveRoom.class);

        if (liveRoom != null && liveRoom.getStatus() == 1) {
            // 更新直播间状态
            endLiveStream(liveRoom.getId());
            log.info("直播流关闭: app={}, stream={}, roomId={}", app, stream, liveRoom.getId());
        }
    }

    /**
     * 获取SRS服务器信息
     */
    public Map<String, Object> getSrsServerInfo() {
        try {
            String url = srsApiUrl + "/v1/summaries";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("获取SRS服务器信息异常", e);
            return Collections.emptyMap();
        }
    }
}