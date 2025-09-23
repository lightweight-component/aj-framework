package com.ajaxjs.livestream.controller;

import com.ajaxjs.livestream.model.LiveRecording;
import com.ajaxjs.livestream.model.LiveRoom;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/live")
public interface LiveController {
    /**
     * 创建直播间
     */
    @PostMapping("/room")
    LiveRoom createLiveRoom(@RequestBody LiveRoom liveRoom);

    /**
     * 获取直播间详情
     */
    @GetMapping("/room/{roomId}")
    LiveRoom getLiveRoom(@PathVariable Long roomId);

    /**
     * 开始直播
     */
    @PostMapping("/room/{roomId}/start")
    LiveRoom startLiveStream(@PathVariable Long roomId);

    /**
     * 结束直播
     */
    @PostMapping("/room/{roomId}/end")
    LiveRoom endLiveStream(@PathVariable Long roomId);

    /**
     * 获取活跃直播间列表
     */
    @GetMapping("/rooms/active")
    List<LiveRoom> getActiveLiveRooms(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size);

    /**
     * 获取热门直播间
     */
    @GetMapping("/rooms/hot")
    List<LiveRoom> getHotLiveRooms(@RequestParam(defaultValue = "10") int limit);

    /**
     * 增加观看人数
     */
    @PostMapping("/room/{roomId}/view")
    void incrementViewCount(@PathVariable Long roomId);

    /**
     * 开始录制直播
     */
    @PostMapping("/room/{roomId}/record/start")
    LiveRecording startRecording(@PathVariable Long roomId);

    /**
     * 停止录制直播
     */
    @PostMapping("/record/{recordingId}/stop")
    LiveRecording stopRecording(@PathVariable Long recordingId);

    /**
     * 获取直播回放列表
     */
    @GetMapping("/room/{roomId}/recordings")
    List<LiveRecording> getRecordings(@PathVariable Long roomId, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size);
}