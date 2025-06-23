package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.UserGender;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.GlobalAuthUtils;
import com.ajaxjs.oauth.utils.HttpUtils;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthSource;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;

/**
 * <p>
 * 钉钉登录抽象类，负责处理使用钉钉账号登录第三方网站和扫码登录第三方网站两种钉钉的登录方式
 * </p>

 */
public abstract class AbstractAuthDingtalkRequest extends AuthDefaultRequest {

    public AbstractAuthDingtalkRequest(AuthConfig config, AuthSource source) {
        super(config, source);
    }


    public AbstractAuthDingtalkRequest(AuthConfig config, AuthSource source, AuthStateCache authStateCache) {
        super(config, source, authStateCache);
    }

    @Override
    public AuthToken getAccessToken(AuthCallback authCallback) {
        return AuthToken.builder().accessCode(authCallback.getCode()).build();
    }

    @Override
    public AuthUser getUserInfo(AuthToken authToken) {
        String code = authToken.getAccessCode();
        JSONObject param = new JSONObject();
        param.put("tmp_auth_code", code);
        String response = new HttpUtils(config.getHttpConfig()).post(userInfoUrl(authToken), param.toJSONString()).getBody();
        JSONObject object = JSON.parseObject(response);
        if (object.getIntValue("errcode") != 0)
            throw new AuthException(object.getString("errmsg"));

        object = object.getJSONObject("user_info");
        AuthToken token = AuthToken.builder()
            .openId(object.getString("openid"))
            .unionId(object.getString("unionid"))
            .build();
        return AuthUser.builder()
            .rawUserInfo(object)
            .uuid(object.getString("unionid"))
            .nickname(object.getString("nick"))
            .username(object.getString("nick"))
            .gender(UserGender.UNKNOWN)
            .source(source.toString())
            .token(token)
            .build();
    }

    /**
     * 返回带{@code state}参数的授权url，授权回调时会带上这个{@code state}
     *
     * @param state state 验证授权流程的参数，可以防止csrf
     * @return 返回授权地址
     * @since 1.9.3
     */
    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(source.authorize())
            .queryParam("response_type", "code")
            .queryParam("appid", config.getClientId())
            .queryParam("scope", "snsapi_login")
            .queryParam("redirect_uri", config.getRedirectUri())
            .queryParam("state", getRealState(state))
            .build();
    }

    /**
     * 返回获取userInfo的url
     *
     * @param authToken 用户授权后的token
     * @return 返回获取userInfo的url
     */
    @Override
    protected String userInfoUrl(AuthToken authToken) {
        // 根据timestamp, appSecret计算签名值
        String timestamp = System.currentTimeMillis() + "";
        String urlEncodeSignature = GlobalAuthUtils.generateDingTalkSignature(config.getClientSecret(), timestamp);

        return UrlBuilder.fromBaseUrl(source.userInfo())
            .queryParam("signature", urlEncodeSignature)
            .queryParam("timestamp", timestamp)
            .queryParam("accessKey", config.getClientId())
            .build();
    }

}
