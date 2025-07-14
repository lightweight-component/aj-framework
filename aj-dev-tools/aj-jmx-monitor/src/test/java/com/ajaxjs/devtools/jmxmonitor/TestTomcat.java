package com.ajaxjs.devtools.jmxmonitor;

import com.ajaxjs.devtools.jmxmonitor.tomcat_jmx.TomcatJmx;
import com.ajaxjs.devtools.jmxmonitor.tomcat_jmx.model.TomcatInfo;
import com.ajaxjs.util.JsonUtil;
import org.junit.Test;

public class TestTomcat {
    String jmxURL = "service:jmx:rmi:///jndi/rmi://127.0.0.1:9011/jmxrmi";

    @Test
    public void testConnectJMX() {
        TomcatInfo info = new TomcatJmx().getInfo(jmxURL);
        System.out.println(JsonUtil.toJson(info));
    }
}
