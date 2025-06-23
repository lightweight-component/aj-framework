package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;

/**
 * 钉钉账号登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.0.0
 */
public class AuthDingTalkAccountRequest extends AbstractAuthDingtalkRequest {

    public AuthDingTalkAccountRequest(AuthConfig config) {
        super(config, AuthDefaultSource.DINGTALK_ACCOUNT);
    }

    public AuthDingTalkAccountRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.DINGTALK_ACCOUNT, authStateCache);
    }
}
