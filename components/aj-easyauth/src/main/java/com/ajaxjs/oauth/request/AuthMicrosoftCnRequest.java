package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.GlobalAuthUtils;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;

/**
 * 微软中国登录(世纪华联)
 *
 * @author mroldx (xzfqq5201314@gmail.com)
 * @since 1.16.4
 */
public class AuthMicrosoftCnRequest extends AbstractAuthMicrosoftRequest {

    public AuthMicrosoftCnRequest(AuthConfig config) {
        super(config, AuthDefaultSource.MICROSOFT_CN);
    }

    public AuthMicrosoftCnRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.MICROSOFT_CN, authStateCache);
    }

    @Override
    protected void checkConfig(AuthConfig config) {
        super.checkConfig(config);
        // 微软中国的回调地址必须为https的链接或者localhost,不允许使用http
        if (AuthDefaultSource.MICROSOFT_CN == source && !GlobalAuthUtils.isHttpsProtocolOrLocalHost(config.getRedirectUri())) {
            // Microsoft's redirect uri must use the HTTPS or localhost
            throw new AuthException(ResponseStatus.ILLEGAL_REDIRECT_URI, source);
        }
    }

}
