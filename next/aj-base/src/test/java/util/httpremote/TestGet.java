package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestGet {
//    @Test
//    public void testSimpleGet() {
//        String html = Get.simpleGET("https://www.baidu.com");
//        assert html != null;
//        assertTrue(html.contains("百度一下，你就知道"));
//    }

    @Test
    void testGetHTML() {
        String html = Get.text("https://www.baidu.com");
        assertTrue(html.contains("百度一下，你就知道"));
    }

    @Test
    public void testGetApi() {
        Map<String, Object> map = Get.api("https://beta.bingolink.biz/iamapi/user/af38ddf7-dd53-4bad-bee9-0d81abefb817?access_token=bG9jYWw6RUxFQ2Y2aEZnY206R2VESE1SQmZtVQ");
        assertNotNull(map.get("error"));
    }

    @Test
    void testDownload2disk() {
        assertNotNull(BatchDownload.download(HttpConstant.HttpMethod.GET, "https://www.baidu.com/", null, "c:/temp", "baidu"));

        String url = "https://etax.guangdong.chinatax.gov.cn:8443/static_res/images/nlogo14400.png";
        assertNotNull(BatchDownload.download(HttpConstant.HttpMethod.GET, url, null, "c:/temp", null));
    }
}
