package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.cache.AuthStateCache;
import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.model.AuthException;
import com.ajaxjs.oauth.utils.UrlBuilder;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.StrUtil;

/**
 * <p>
 * 新版企业微信 Web 登录，参考 <a href="https://developer.work.weixin.qq.com/document/path/98152">https://developer.work.weixin.qq.com/document/path/98152</a>
 * </p>
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @since 1.16.7
 */
public class AuthWeChatEnterpriseQrcodeV2Request extends AbstractAuthWeChatEnterpriseRequest {
    public AuthWeChatEnterpriseQrcodeV2Request(AuthConfig config) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_V2);
    }

    public AuthWeChatEnterpriseQrcodeV2Request(AuthConfig config, AuthStateCache authStateCache) {
        super(config, AuthDefaultSource.WECHAT_ENTERPRISE_V2, authStateCache);
    }

    @Override
    public String authorize(String state) {
        return UrlBuilder.fromBaseUrl(source.authorize())
            .queryParam("login_type", config.getLoginType())
            // 登录类型为企业自建应用/服务商代开发应用时填企业 CorpID，第三方登录时填登录授权 SuiteID
            .queryParam("appid", config.getClientId())
            // 企业自建应用/服务商代开发应用 AgentID，当login_type=CorpApp时填写
            .queryParam("agentid", config.getAgentId())
            .queryParam("redirect_uri", EncodeTools.urlEncodeSafe(config.getRedirectUri()))
            .queryParam("state", getRealState(state))
            .queryParam("lang", config.getLang())
            .build()
            .concat("#wechat_redirect");
    }

    @Override
    protected void checkConfig(AuthConfig config) {
        super.checkConfig(config);

        if ("CorpApp".equals(config.getLoginType()) && StrUtil.isEmptyText(config.getAgentId()))
            throw new AuthException(ResponseStatus.ILLEGAL_WECHAT_AGENT_ID, source);
    }
}
