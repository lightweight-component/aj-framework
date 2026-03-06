package com.ajaxjs.framework.business.video_clip.model;

public class VideoParams {
    private String inputPath;

    private String outputPath;

    private VideoCodec videoCodec = VideoCodec.H264; // 默ilH264编码

    private AudioCodec audioCodec = AudioCodec.AAC; // 默iAAc编码

    private int crf = 23;//质量控制(e-51)

    private String preset = "medium";//编码速度预设

    private boolean enableWatermark = true;// 水印开关
}
