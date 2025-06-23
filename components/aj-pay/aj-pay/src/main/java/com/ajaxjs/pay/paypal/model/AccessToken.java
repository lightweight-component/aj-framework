
package com.ajaxjs.pay.paypal.model;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ajaxjs.pay.core.utils.RetryUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>IJPay 让支付触手可及，封装了微信支付、支付宝支付、银联支付常用的支付方式以及各种常用的接口。</p>
 *
 * <p>不依赖任何第三方 mvc 框架，仅仅作为工具使用简单快速完成支付模块的开发，可轻松嵌入到任何系统里。 </p>
 *
 * <p>IJPay 交流群: 723992875、864988890</p>
 *
 * <p>Node.js 版: <a href="https://gitee.com/javen205/TNWX">https://gitee.com/javen205/TNWX</a></p>
 *
 * @author Javen
 */
@Data
public class AccessToken implements Serializable, RetryUtils.ResultCheck {
    private static final long serialVersionUID = 150495825818051646L;
    private String access_token;
    private String token_type;
    private String app_id;
    private Integer expires_in;
    private Long expiredTime;
    private String json;
    /**
     * http 请求状态码
     */
    private Integer status;

    public AccessToken(String json, int httpCode) {
        this.json = json;
        this.status = httpCode;

        try {
            Map<String, Object> map = JsonUtil.json2map(json);
            this.access_token = map.get("access_token").toString();
            this.expires_in = Integer.parseInt(map.get("expires_in").toString());
            this.app_id = map.get("app_id").toString();
            this.token_type = map.get("token_type").toString();

            if (expires_in != null)
                this.expiredTime = System.currentTimeMillis() + ((expires_in - 9) * 1000L);

            if (map.containsKey("expiredTime"))
                this.expiredTime = Long.parseLong(map.get("expiredTime").toString());

            if (map.containsKey("status"))
                this.status = Integer.parseInt(map.get("status").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAvailable() {
        if (status != 200)
            return false;

        if (expiredTime == null)
            return false;

        if (expiredTime < System.currentTimeMillis())
            return false;

        return StrUtil.hasText(access_token);
    }

    public String getCacheJson() {
        Map<String, Object> temp = JSONUtil.toBean(json, Map.class);
        temp.put("expiredTime", expiredTime);
        temp.remove("expires_in");
        temp.remove("scope");
        temp.remove("nonce");

        return JsonUtil.toJson(temp);
    }

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String accessToken) {
        this.access_token = accessToken;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String tokenType) {
        this.token_type = tokenType;
    }

    public String getAppId() {
        return app_id;
    }

    public void setAppId(String appId) {
        this.app_id = appId;
    }

    public Integer getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expires_in = expiresIn;
    }

    @Override
    public boolean matching() {
        return isAvailable();
    }
}
