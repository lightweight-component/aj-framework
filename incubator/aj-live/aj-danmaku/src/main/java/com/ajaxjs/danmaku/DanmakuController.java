package com.ajaxjs.danmaku;

import com.ajaxjs.danmaku.model.Danmaku;
import com.ajaxjs.danmaku.model.DanmakuDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danmaku")
public interface DanmakuController {
    /**
     * 发送弹幕
     */
    @MessageMapping("/danmaku/send")
    Danmaku sendDanmaku(DanmakuDTO danmakuDTO);

    /**
     * 获取视频的所有弹幕（REST API）
     */
    @GetMapping("/video/{videoId}")
    List<Danmaku> getDanmakusByVideoId(@PathVariable String videoId);

    /**
     * 获取指定时间范围内的弹幕（REST API）
     */
    @GetMapping("/video/{videoId}/timerange")
    List<Danmaku> getDanmakusByVideoIdAndTimeRange(@PathVariable String videoId, @RequestParam Double start, @RequestParam Double end);
}