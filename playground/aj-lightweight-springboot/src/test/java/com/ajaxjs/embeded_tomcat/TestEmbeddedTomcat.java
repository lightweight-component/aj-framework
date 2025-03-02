package com.ajaxjs.embeded_tomcat;

import com.ajaxjs.embeded_tomcat.TomcatConfig;
import com.ajaxjs.embeded_tomcat.TomcatStarter;
import org.junit.Test;

public class TestEmbeddedTomcat {
    @Test
    public void testRun() {
        TomcatConfig cfg = new TomcatConfig();
        TomcatStarter t = new TomcatStarter(cfg);
        t.start();
    }
}
