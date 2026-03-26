package com.ajaxjs.framework.sysmonitor;

import com.ajaxjs.framework.sysmonitor.model.JvmInfo;
import com.ajaxjs.framework.sysmonitor.model.LinuxMemInfo;
import com.ajaxjs.framework.sysmonitor.model.SysInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys_monitor")
public class SysMonitorController {
    @GetMapping("/sys_info")
    public SysInfo getSys() {
        return BaseInfo.getSysInfo();
    }

    @GetMapping("/jvm_info")
    public JvmInfo getJvm() {
        return BaseInfo.getJvmInfo();
    }

    @GetMapping("/mem_info")
    public LinuxMemInfo getMem() {
        return LinuxSystemTool.getMemInfo();
    }

    @GetMapping("/cpu_usage")
    public float getCpuUsage() {
        return LinuxSystemTool.getCpuUsage() * 100;
    }
}
