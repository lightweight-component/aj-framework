/**
 * Copyright Sp42 frank@ajaxjs.com Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.ajaxjs.framework.wechat;

import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.httpremote.Get;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Consumer;

/**
 * 微信公众号
 */
@Data
@Slf4j
public class OpenAccount {
    /**
     * AppId
     */
    private String appId;

    /**
     * App 密钥
     */
    private String accessSecret;

    private final static String TOKEN_API = "https://api.weixin.qq.com/cgi-bin/token";

    /**
     * 当获取到 AccessToken 的时候触发，例如放进 Redis
     */
    private Consumer<String> onTokenGet;

    private String accessToken;

    /**
     * 获取 Client AccessToken
     */
    public void getAccessToken() {
        log.info("获取 Client AccessToken");

        String params = String.format("?grant_type=client_credential&appid=%s&secret=%s", appId, accessSecret);
        Map<String, Object> map = Get.api(TOKEN_API + params);

        if (map.containsKey("access_token")) {
            accessToken = map.get("access_token").toString();
            log.info("获取令牌成功！ AccessToken [{}]", accessToken);

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
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run(); // 执行传入的 Lambda 任务
            }
        };

        // 延迟1秒后开始执行，之后每隔 seconds 秒重复执行
        new Timer().schedule(timerTask, 1000, (long) seconds * 1000);
    }

    public static boolean init(HttpServletRequest req) {
        String ua = req.getHeader("User-Agent");

        if (ua != null)
            ua = ua.toLowerCase();// 强制转换为小写字母

        // 获取当前页面的 url
        String url = (req.getRemotePort() != 80 ? "https" : req.getScheme()) + "://" + req.getServerName() + req.getRequestURI();
        if (req.getQueryString() != null)
            url += "?" + req.getQueryString();

        String ticket = "";
        Map<String, String> map = generateSignature(url, ticket);
        req.setAttribute("map", map);

        return ua.contains("micromessenger");
    }

    /**
     * 生成签名
     *
     * @param url         页面地址
     * @param jsApiTicket 凭证
     * @return 页面用的数据
     */
    static Map<String, String> generateSignature(String url, String jsApiTicket) {
        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        map.put("jsapi_ticket", jsApiTicket);
        map.put("noncestr", RandomTools.generateRandomString(10));
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("signature", generateSignature(map));

        return map; // 因为签名用的 noncestr 和 timestamp 必须与 wx.config 中的 nonceStr 和 timestamp 相同，所以还需要使用这两个参数
    }

    /**
     * 字段名的 ASCII 码从小到大排序（字典序）后，使用 URL 键值对的格式（即key1=value1&key2=value2…）拼接成字符串
     *
     * @param data Map
     * @return 签名
     */
    private static String generateSignature(Map<String, String> data) {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[0]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (String k : keyArray) {
            String v = data.get(k);
            if (StringUtils.hasText(v)) // 参数值为空，则不参与签名
                sb.append(k).append("=").append(v.trim()).append(++i < data.size() ? "&" : "");
        }

//        return Digest.getSHA1(sb.toString());
        return HashHelper.getSHA1(sb.toString());
    }
}
