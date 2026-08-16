package com.ajaxjs.wechat;

import com.ajaxjs.framework.BaseTest;
import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.httpremote.Get;
import com.ajaxjs.wechat.token_refresh.TokenAutoRefresher;
import com.ajaxjs.wechat.token_refresh.TokenWithExpire;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestTokenAutoRefresher extends BaseTest {
    final static String GET_TOKEN = "http://112.94.22.247:8006/xiaozhi/v1/getToken?encryptKey=%s&salt=%s";

    static String getEncryptKey(String key, String random) {
        return HashHelper.md5(random + key);
    }

    @Test
    void test() {
        TokenAutoRefresher tokenAutoRefresher = new TokenAutoRefresher(() -> {
            String random = RandomTools.generateRandomString(6);
            Map<String, Object> result = Get.api(String.format(GET_TOKEN, getEncryptKey("Blxbxx35Zh8b8wcuiEUb", random), random));

            System.out.println(result);
            Map<String, Object> data = (Map<String, Object>) result.get("data");

            return new TokenWithExpire() {
                @Override
                public String getToken() {
                    return (String) data.get("token");
                }

                @Override
                public int getExpire() {
                    return (int) data.get("expire");
                }
            };
        });

        tokenAutoRefresher.start();
    }
}
