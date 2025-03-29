package com.ajaxjs.oauth.request;

import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.util.RandomTools;
import org.junit.jupiter.api.Test;

public class AuthWeChatEnterpriseWebRequestTest {
    @Test
    public void authorize() {
        AuthRequest request = new AuthWeChatEnterpriseWebRequest(AuthConfig.builder()
            .clientId("a")
            .clientSecret("a")
            .redirectUri("https://www.justauth.cn")
            .build());
        System.out.println(request.authorize(RandomTools.uuid()));
    }
}
