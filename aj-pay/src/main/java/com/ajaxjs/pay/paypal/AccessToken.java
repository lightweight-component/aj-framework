package com.ajaxjs.pay.paypal;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import java.util.Map;

@Data
public class AccessToken {
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

        return ObjectHelper.hasText(access_token);
    }

    public String getCacheJson() {
        Map<String, Object> temp = JsonUtil.json2map(json);
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

    public boolean matching() {
        return isAvailable();
    }
}
