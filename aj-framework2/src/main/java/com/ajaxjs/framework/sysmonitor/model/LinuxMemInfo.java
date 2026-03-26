package com.ajaxjs.framework.sysmonitor.model;

import lombok.Data;

@Data
public class LinuxMemInfo {
    String total;

    String free;

    String swapTotal;

    String swapFree;
}
