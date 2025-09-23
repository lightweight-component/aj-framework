package com.ajaxjs.danmaku;

import com.ajaxjs.danmaku.model.Danmaku;
import com.ajaxjs.danmaku.model.DanmakuDTO;
import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.crud.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DanmakuService implements DanmakuController {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 保存并发送弹幕
     */
    @Override
    public Danmaku sendDanmaku(DanmakuDTO danmakuDTO) {
        // 内容过滤（简单示例）
        String filteredContent = filterContent(danmakuDTO.getContent());

        // 创建弹幕实体
        Danmaku danmaku = new Danmaku();
        danmaku.setContent(filteredContent);
        danmaku.setColor(danmakuDTO.getColor());
        danmaku.setFontSize(danmakuDTO.getFontSize());
        danmaku.setTime(danmakuDTO.getTime());
        danmaku.setVideoId(danmakuDTO.getVideoId());
        danmaku.setUserId(danmakuDTO.getUserId());
        danmaku.setUsername(danmakuDTO.getUsername());
        danmaku.setCreatedAt(LocalDateTime.now());
        Entity.instance().input(danmaku).create();

        // 通过WebSocket发送到客户端
        messagingTemplate.convertAndSend("/topic/video/" + danmaku.getVideoId(), danmaku);

        return danmaku;
    }

    /**
     * 获取视频的所有弹幕
     */
    @Override
    public List<Danmaku> getDanmakusByVideoId(String videoId) {
        return Sql.instance().input("SELECT * FROM danmaku WHERE video_id = ? ORDER BY time ASC", videoId).queryList(Danmaku.class);
    }

    /**
     * 获取指定时间范围内的弹幕
     */
    @Override
    public List<Danmaku> getDanmakusByVideoIdAndTimeRange(String videoId, Double startTime, Double endTime) {
        return Sql.instance().input("SELECT * FROM danmaku WHERE video_id = #{videoId} AND time BETWEEN ? AND ? ORDER BY time ASC", videoId, startTime, endTime).queryList(Danmaku.class);
    }

    /**
     * 简单的内容过滤实现
     */
    private String filterContent(String content) {
        // 实际应用中这里可能会有更复杂的过滤逻辑
        String[] sensitiveWords = {"敏感词1", "敏感词2", "敏感词3"};
        String filtered = content;

        for (String word : sensitiveWords)
            filtered = filtered.replaceAll(word, "***");

        return filtered;
    }
}