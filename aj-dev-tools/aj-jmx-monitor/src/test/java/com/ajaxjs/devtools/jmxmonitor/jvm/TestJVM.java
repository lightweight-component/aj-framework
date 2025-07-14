package com.ajaxjs.devtools.jmxmonitor.jvm;

import com.ajaxjs.developertools.BaseTest;
import com.ajaxjs.devtools.jmxmonitor.jvm.model.Node;
import com.ajaxjs.devtools.jmxmonitor.jvm.model.Overview;
import com.ajaxjs.devtools.jmxmonitor.jvm.model.Vm;
import com.ajaxjs.util.TestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestJVM extends BaseTest {
    @Autowired
    MonitorDashboardService dashboardService;

    @Test
    public void testOverview() {
        jvmMonitorController.attachLocalJvm(17168);
        Overview overview = dashboardService.overview();
        TestHelper.printJson(overview);

        assertNotNull(overview);
    }

    @Autowired
    JvmMonitorController jvmMonitorController;

    @Test
    public void testVM() {
        List<Vm> localJvm = jvmMonitorController.getLocalJvmProcessList();
//        TestHelper.printJson(localJvm);
        assertNotNull(localJvm);
        jvmMonitorController.attachLocalJvm(17168);
        testOverview();
    }

    @Test
    public void testJmxService() {
        List<Vm> localJvm = jvmMonitorController.getLocalJvmProcessList();
        TestHelper.printJson(localJvm);

        assertNotNull(localJvm);

        jvmMonitorController.attachLocalJvm(11844);
        List<Node> domains = jvmMonitorController.getDomains();
        TestHelper.printJson(domains);
        assertNotNull(domains);
    }

}
