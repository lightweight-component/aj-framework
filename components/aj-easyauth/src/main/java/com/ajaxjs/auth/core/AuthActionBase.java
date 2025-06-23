package com.ajaxjs.auth.core;

import com.ajaxjs.auth.model.Config;

public abstract class AuthActionBase implements AuthAction {
    protected ApiProvider apiProvider;

    protected Config config;

    public AuthActionBase(ApiProvider apiProvider, Config config) {
        this.apiProvider = apiProvider;
        this.config = config;
    }

    private final static String AUTHORIZE_PARAMS = "?response_type=code&client_id=%s&redirect_uri=%s&state=%s";

    @Override
    public String authorize(String state) {
        return apiProvider.authorize() + String.format(AUTHORIZE_PARAMS, config.getClientId(), config.getRedirectUri(), state);
    }
}
