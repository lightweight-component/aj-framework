package com.ajaxjs.oauth.enums.scope;

import com.ajaxjs.auth.core.AuthScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 钉钉平台 OAuth 授权范围
 * <a href="https://open.dingtalk.com/document/orgapp/obtain-identity-credentials#title-4up-u8w-5ug">...</a>
 */
@Getter
@AllArgsConstructor
public enum AuthDingTalkScope implements AuthScope {
    /**
     * 无需申请	默认开启
     */
    openid("openid", "授权后可获得用户userid", true),
    /**
     * 无需申请	默认开启
     */
    corpid("corpid", "授权后可获得登录过程中用户选择的组织id", false)
    ;

    private final String scope;
    private final String description;
    private final boolean isDefault;
}
