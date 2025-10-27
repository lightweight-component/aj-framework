package com.ajaxjs.framework.wechat.applet;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import com.ajaxjs.util.httpremote.Get;

/**
 * 获取小程序 AccessToken
 */
@Slf4j
public class GetToken {

    private final static String TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token";

    public final WeChatAppletConfig appletCfg;

    /**
     * @param appletCfg 包含 AppId 和密钥
     */
    public GetToken(WeChatAppletConfig appletCfg) {
        this.appletCfg = appletCfg;
    }

    /**
     * 当获取到 AccessToken 的时候触发，例如放进 Redis
     */
    private Consumer<String> onTokenGet;

    public Consumer<String> getOnTokenGet() {
        return onTokenGet;
    }

    public void setOnTokenGet(Consumer<String> onTokenGet) {
        this.onTokenGet = onTokenGet;
    }

    /**
     * 获取 Client AccessToken
     */
    public void getAccessToken() {
        log.info("获取 Client AccessToken");

        String params = String.format("?grant_type=client_credential&appid=%s&secret=%s", appletCfg.getAccessKeyId(), appletCfg.getAccessSecret());
        Map<String, Object> map = Get.api(TOKEN_API + params);

        if (map.containsKey("access_token")) {
            String accessToken = map.get("access_token").toString();
            appletCfg.setAccessToken(accessToken);
            log.info("获取令牌成功！ AccessToken [{}]", appletCfg.getAccessToken());

            if (onTokenGet != null) {
                try {
                    onTokenGet.accept(accessToken);
                } catch (Throwable e) {
                    log.warn("获取 Client AccessToken", e);
                }
            }
        } else if (map.containsKey("errcode"))
            log.warn("获取令牌失败！ Error [{}:{}]", map.get("errcode"), map.get("errmsg"));
        else
            log.warn("获取令牌失败！未知异常 [{}]", map);
    }

    /**
     * 获取 Client AccessToken，并加入定时器
     */
    public void init() {
        getAccessToken();
        setTimeout(this::getAccessToken, 7100);
    }

    /**
     * 每隔指定秒数执行一次任务（延迟1秒后开始）
     *
     * @param task    要执行的任务（Lambda 表达式）
     * @param seconds 间隔秒数
     */
    public static void setTimeout(Runnable task, int seconds) {
        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run(); // 执行传入的 Lambda 任务
            }
        };

        // 延迟1秒后开始执行，之后每隔 seconds 秒重复执行
        timer.schedule(timerTask, 1000, (long) seconds * 1000);
    }
}
