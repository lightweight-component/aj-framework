package com.ajaxjs.business.net.netcard;

import java.net.NetworkInterface;

/**
 * 网卡类型
 */
public enum Filter {
    /**
     * 过滤器: 所有网卡
     */
    ALL,
    /**
     * 过滤器: 在线设备,see also {@link NetworkInterface#isUp()}
     */
    UP,
    /**
     * 过滤器: 虚拟接口,see also {@link NetworkInterface#isVirtual()}
     */
    VIRTUAL,
    /**
     * 过滤器:LOOPBACK, see also {@link NetworkInterface#isLoopback()}
     */
    LOOPBACK,
    /**
     * 过滤器:物理网卡
     */
    PHYICAL_ONLY,
    /**
     * 过滤器:本地以太网卡
     */
    ETH_NIC,
    /**
     * 过滤器:物理网卡(非虚拟)
     */
    NOVIRTUAL;

}
