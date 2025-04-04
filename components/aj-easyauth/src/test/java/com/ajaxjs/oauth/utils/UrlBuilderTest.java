package com.ajaxjs.oauth.utils;

import com.ajaxjs.oauth.config.AuthConfig;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.request.AuthWeChatOpenRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * UrlBuilder测试类
 * </p>
 *
 * @author yangkai.shen (https://xkcoding.com)
 * @date Created in 2019-07-18 16:36
 */
public class UrlBuilderTest {
    @Test
    public void testUrlBuilder() {
        AuthConfig config = AuthConfig.builder()
            .clientId("appid-110110110")
            .clientSecret("secret-110110110")
            .redirectUri("https://xkcoding.com")
            .build();
        String build = UrlBuilder.fromBaseUrl(AuthDefaultSource.WECHAT_OPEN.authorize())
            .queryParam("appid", config.getClientId())
            .queryParam("redirect_uri", config.getRedirectUri())
            .queryParam("response_type", "code")
            .queryParam("scope", "snsapi_login")
            .queryParam("state", "")
            .build(false);
        System.out.println(build);
        AuthWeChatOpenRequest request = new AuthWeChatOpenRequest(config);
        String authorize = request.authorize("state");
        System.out.println(authorize);
    }

    @Test
    public void build() {
        String url = UrlBuilder.fromBaseUrl("https://www.zhyd.me")
            .queryParam("name", "yadong.zhang")
            .build();
        Assertions.assertEquals(url, "https://www.zhyd.me?name=yadong.zhang");

        url = UrlBuilder.fromBaseUrl(url)
            .queryParam("github", "https://github.com/zhangyd-c")
            .build();
        Assertions.assertEquals(url, "https://www.zhyd.me?name=yadong.zhang&github=https://github.com/zhangyd-c");
    }

    @Test
    public void build1() {
        String url = UrlBuilder.fromBaseUrl("https://www.zhyd.me")
            .queryParam("name", "yadong.zhang")
            .build(true);
        Assertions.assertEquals(url, "https://www.zhyd.me?name=yadong.zhang");

        url = UrlBuilder.fromBaseUrl(url)
            .queryParam("github", "https://github.com/zhangyd-c")
            .build(true);
        Assertions.assertEquals(url, "https://www.zhyd.me?name=yadong.zhang&github=https%3A%2F%2Fgithub.com%2Fzhangyd-c");
    }
}
