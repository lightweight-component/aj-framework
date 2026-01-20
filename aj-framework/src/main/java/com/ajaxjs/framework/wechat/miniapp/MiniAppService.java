package com.ajaxjs.framework.wechat.miniapp;

import lombok.RequiredArgsConstructor;

/**
 * 小程序常见业务
 */
@RequiredArgsConstructor
public class MiniAppService {
    public final WeChatAppletConfig appletCfg;

    /**
     * 生成小程序码的 API
     */
    private static final String CREATE_QRCODE_API = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=";

    private static final String QRCODE_ARGS = "{\"scene\":\"%s\",\"page\":\"%s\",\"width\":%d,\"is_hyaline\":%s,\"env_version\":\"%s\"}";

    /**
     * 获取小程序码
     *
     * @param isTrialVersion 是否生成的是体验版的小程序码
     * @param scene          场景值，最长为 32 个字符。使用 JSON 编码，扩展性比较好
     * @param page           小程序页面路径，例如 pages/index/index
     * @param width          二维码的宽度，单位 px，最小 280px，最大 1280px
     * @param fileName       文件名
     */
    public void qrCode(boolean isTrialVersion, String scene, String page, int width, String fileName) {
        String url = CREATE_QRCODE_API + appletCfg.getAccessToken();
        // env_version 正式版为 "release"，体验版为 "trial"，开发版为 "develop"。默认是正式版。
        // 配置参数
        String requestJson = String.format(QRCODE_ARGS, scene, page, width, false, isTrialVersion ? "trial" : "release");
        System.out.println(requestJson);

        // 请求
//        Post.showPic(url, conn -> {
//            try (OutputStream outputStream = conn.getOutputStream()) {
//                outputStream.write(requestJson.getBytes(StandardCharsets.UTF_8));
//                outputStream.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, DiContextUtil.getResponse());
    }
}
