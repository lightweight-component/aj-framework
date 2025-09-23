package com.ajaxjs.danmaku.model;

import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Table("danmaku")
public class Danmaku {
    private Long id;

    private String content;  // 弹幕内容

    private String color;    // 弹幕颜色

    private Integer fontSize; // 字体大小

    private Double time;     // 视频时间点

    private String videoId;  // 关联的视频ID

    private String userId;   // 发送用户ID

    private String username; // 用户名

    private LocalDateTime createdAt; // 创建时间
}