package com.ajaxjs.oauth.model;

import com.alibaba.fastjson.JSON;
import com.ajaxjs.oauth.config.AuthDefaultSource;
import com.ajaxjs.oauth.config.AuthExtendSource;
import com.ajaxjs.oauth.config.AuthSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthUserTest {

    @Test
    public void serialize() {

        AuthUser user = AuthUser.builder()
            .nickname("test")
            .build();
        String json = JSON.toJSONString(user);
        Assertions.assertEquals(json, "{\"nickname\":\"test\",\"snapshotUser\":false}");

    }

    @Test
    public void deserialize() {
        AuthUser user = AuthUser.builder()
            .nickname("test")
            .build();
        String json = JSON.toJSONString(user);

        AuthUser deserializeUser = JSON.parseObject(json, AuthUser.class);
        Assertions.assertEquals(deserializeUser.getNickname(), "test");
    }

    @Test
    public void source() {
        AuthSource source = AuthDefaultSource.HUAWEI;
        AuthUser user = AuthUser.builder()
            .source(source.toString())
            .build();
        Assertions.assertEquals(user.getSource(), "HUAWEI");

        source = AuthExtendSource.OTHER;
        user = AuthUser.builder()
            .source(source.toString())
            .build();
        Assertions.assertEquals(user.getSource(), "OTHER");

        source = AuthDefaultSource.HUAWEI;
        Assertions.assertEquals(source, AuthDefaultSource.HUAWEI);

        source = AuthExtendSource.OTHER;
        Assertions.assertEquals(source, AuthExtendSource.OTHER);
    }

}
