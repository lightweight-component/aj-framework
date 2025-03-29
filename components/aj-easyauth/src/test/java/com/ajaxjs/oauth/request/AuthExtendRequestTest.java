package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.model.AuthCallback;
import com.ajaxjs.oauth.model.AuthResponse;
import com.ajaxjs.oauth.model.AuthToken;
import com.ajaxjs.oauth.model.AuthUser;
import com.ajaxjs.util.RandomTools;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 自定义扩展的第三方request的测试类，用于演示具体的用法
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com)
 * @version 1.0
 * @since 1.12.0
 */
public class AuthExtendRequestTest {
    @Test
    public void authorize() {
        AuthRequest request = new AuthExtendRequest(AuthConfig.builder()
            .clientId("clientId")
            .clientSecret("clientSecret")
            .redirectUri("http://redirectUri")
            .build());
        String authorize = request.authorize(RandomTools.uuid());
        System.out.println(authorize);

        Assertions.assertNotNull(authorize);
    }

    @Test
    public void login() {
        AuthRequest request = new AuthExtendRequest(AuthConfig.builder()
            .clientId("clientId")
            .clientSecret("clientSecret")
            .redirectUri("http://redirectUri")
            .build());

        String state = RandomTools.uuid();
        request.authorize(state);
        AuthCallback callback = AuthCallback.builder()
            .code("code")
            .state(state)
            .build();
        AuthResponse<AuthUser> response = request.login(callback);
        Assertions.assertNotNull(response);

        AuthUser user = response.getData();
        Assertions.assertNotNull(user);
        System.out.println(JSON.toJSONString(user));
    }

    @Test
    public void revoke() {
        AuthRequest request = new AuthExtendRequest(AuthConfig.builder()
            .clientId("clientId")
            .clientSecret("clientSecret")
            .redirectUri("http://redirectUri")
            .build());

        AuthResponse response = request.revoke(AuthToken.builder().build());
        Assertions.assertNotNull(response);
        System.out.println(JSON.toJSONString(response));
    }

    @Test
    public void refresh() {
        AuthRequest request = new AuthExtendRequest(AuthConfig.builder()
            .clientId("clientId")
            .clientSecret("clientSecret")
            .redirectUri("http://redirectUri")
            .build());

        AuthResponse<AuthToken> response = request.refresh(AuthToken.builder().build());
        Assertions.assertNotNull(response);
        System.out.println(JSON.toJSONString(response.getData()));

    }
}
