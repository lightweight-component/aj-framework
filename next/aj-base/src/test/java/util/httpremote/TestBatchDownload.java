package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

public class TestBatchDownload {
    @Test
    public void test() {
        String[] testArr = {"https://yavuzceliker.github.io/sample-images/image-10.jpg",
                "https://inews.gtimg.com/om_ls/Of0ctmwDuNFQfhVJDky1P5LlUzglbsWElQc0U3JipX6g0AA_870492/0",
                "https://graydart.com/lib/assets/img/logo/gd_logo.png",
                "https://httpbin.org/bytes/1024"};

        new BatchDownload(testArr, "c:/temp", null).start();
    }
}
