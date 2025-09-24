package com.ajaxjs.livestream.model;

import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播流信息实体
 */
@Data
@Table("live_stream")
public class LiveStream {
    private Long id;
    
    private Long roomId;
    
    private String streamId;  // 流ID
    
    private String protocol;  // 协议类型：rtmp/hls/flv
    
    private Integer bitrate;  // 码率
    
    private String resolution; // 分辨率
    
    private Integer status;   // 0:未启动 1:活跃 2:已结束
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}