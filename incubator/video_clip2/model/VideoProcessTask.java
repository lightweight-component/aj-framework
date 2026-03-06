package com.ajaxjs.framework.business.video_clip2.model;

import lombok.Data;

import java.util.List;

@Data
public class VideoProcessTask {
    private String taskId;           // 任务ID
    private String originalUrl;      // 原始视频URL
    private List<ProcessType> operations; // 处理操作列表
    private String callbackUrl;      // 回调URL
    private String status;           // 任务状态
    private String resultUrl;        // 处理结果URL
    private long createTime;         // 创建时间
    private long updateTime;         // 更新时间
}
