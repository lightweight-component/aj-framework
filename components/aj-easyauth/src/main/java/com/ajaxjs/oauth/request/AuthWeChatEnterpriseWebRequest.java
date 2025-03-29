package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.enums.scope.AuthWeChatEnterpriseWebScope;
import com.ajaxjs.oauth.utils.AuthChecker;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.util.EncodeTools;

/**
 * <p>
 * 企业微信网页登录
 * </p>
 */
public class AuthWeChatEnterpriseWebRequest extends AbstractAuthWeChatEnterpriseRequest {
    public AuthWeChatEnterpriseWebRequest(AuthConfig config) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_WEB);
    }

    public AuthWeChatEnterpriseWebRequest(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_WEB, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(source.authorize())
            .queryParam("appid", config.getClientId())
            .queryParam("agentid", config.getAgentId())
            .queryParam("redirect_uri", EncodeTools.urlEncodeSafe(config.getRedirectUri()))
            .queryParam("response_type", "code")
            .queryParam("scope", this.getScopes(",", false, AuthChecker.getDefaultScopes(AuthWeChatEnterpriseWebScope.values())))
            .queryParam("state", getRealState(state).concat("#wechat_redirect"))
            .build();
    }
}
