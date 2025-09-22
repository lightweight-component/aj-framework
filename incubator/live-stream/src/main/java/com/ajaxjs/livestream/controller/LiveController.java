package com.ajaxjs.livestream.controller;

import com.ajaxjs.livestream.model.LiveRecording;
import com.ajaxjs.livestream.model.LiveRoom;
import com.ajaxjs.livestream.service.LiveRecordingService;
import com.ajaxjs.livestream.service.LiveStreamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live")
@Slf4j
public class LiveController {

    @Autowired
    private LiveStreamService liveStreamService;

    @Autowired
    private LiveRecordingService recordingService;

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    /**
     * 创建直播间
     */
    @PostMapping("/room")
    public ResponseEntity<LiveRoom> createLiveRoom(@RequestBody LiveRoom liveRoom) {

        LiveRoom createdRoom = liveStreamService.createLiveRoom(liveRoom);
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * 获取直播间详情
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<LiveRoom> getLiveRoom(@PathVariable Long roomId) {

        LiveRoom liveRoom = liveRoomMapper.selectById(roomId);
        if (liveRoom == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(liveRoom);

    }

    /**
     * 开始直播
     */
    @PostMapping("/room/{roomId}/start")
    public ResponseEntity<LiveRoom> startLiveStream(@PathVariable Long roomId) {

        LiveRoom liveRoom = liveStreamService.startLiveStream(roomId);
        return ResponseEntity.ok(liveRoom);
    }

    /**
     * 结束直播
     */
    @PostMapping("/room/{roomId}/end")
    public ResponseEntity<LiveRoom> endLiveStream(@PathVariable Long roomId) {

        LiveRoom liveRoom = liveStreamService.endLiveStream(roomId);
        return ResponseEntity.ok(liveRoom);
    }

    /**
     * 获取活跃直播间列表
     */
    @GetMapping("/rooms/active")
    public ResponseEntity<List<LiveRoom>> getActiveLiveRooms(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {

        List<LiveRoom> rooms = liveStreamService.getActiveLiveRooms(page, size);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 获取热门直播间
     */
    @GetMapping("/rooms/hot")
    public ResponseEntity<List<LiveRoom>> getHotLiveRooms(@RequestParam(defaultValue = "10") int limit) {

        List<LiveRoom> rooms = liveStreamService.getHotLiveRooms(limit);
        return ResponseEntity.ok(rooms);
    }

    /**
     * 增加观看人数
     */
    @PostMapping("/room/{roomId}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long roomId) {

        liveStreamService.incrementViewCount(roomId);
        return ResponseEntity.ok().build();

    }

    /**
     * 开始录制直播
     */
    @PostMapping("/room/{roomId}/record/start")
    public ResponseEntity<LiveRecording> startRecording(@PathVariable Long roomId) {

        LiveRecording recording = recordingService.startRecording(roomId);
        return ResponseEntity.ok(recording);

    }

    /**
     * 停止录制直播
     */
    @PostMapping("/record/{recordingId}/stop")
    public ResponseEntity<LiveRecording> stopRecording(@PathVariable Long recordingId) {

        LiveRecording recording = recordingService.stopRecording(recordingId);
        return ResponseEntity.ok(recording);

    }

    /**
     * 获取直播回放列表
     */
    @GetMapping("/room/{roomId}/recordings")
    public ResponseEntity<List<LiveRecording>> getRecordings(@PathVariable Long roomId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {

        List<LiveRecording> recordings = recordingService.getRecordings(roomId, page, size);
        return ResponseEntity.ok(recordings);

    }
}