package com.ajaxjs.auth.core;

import com.ajaxjs.auth.model.AuthException;
import com.ajaxjs.auth.model.enums.ResponseStatus;
import com.ajaxjs.oauth.request.AuthDefaultRequest;

/**
 * Unified API Endpoint for the OAuth Platform.
 */
public interface ApiProvider {
    /**
     * The API of authorization.
     *
     * @return url The API of authorize
     */
    String authorize();

    /**
     * This is the API used to get an access token.
     *
     * @return url Access token API
     */
    String accessToken();

    /**
     * This is the API used to get the user information.
     *
     * @return url User information API
     */
    String userInfo();

    /**
     * This is the API used to revoke authorization.
     *
     * @return url Revoke authorization API
     */
    default String revoke() {
        throw new AuthException(ResponseStatus.UNSUPPORTED);
    }

    /**
     * This is the API used to refresh authorization.
     *
     * @return url Refresh authorization API
     */
    default String refresh() {
        throw new AuthException(ResponseStatus.UNSUPPORTED);
    }

    /**
     * 平台对应的 AuthRequest 实现类，必须继承自 {@link AuthDefaultRequest}
     *
     * @return class
     */
    Class<? extends AuthDefaultRequest> getTargetClass();
}
