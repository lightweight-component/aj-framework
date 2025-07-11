package com.ajaxjs.business.net;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Ip {
    /**
     * 获取本地ip地址，有可能会有多个地址, 若有多个网卡则会搜集多个网卡的ip地址
     *
     * @return 地ip地址
     */
    public static Set<InetAddress> resolveLocalAddresses() {
        Set<InetAddress> addrs = new HashSet<>();
        Enumeration<NetworkInterface> ns;

        try {
            ns = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            log.warn("Error when getting network interfaces.", e);
            return null;
        }

        while (ns.hasMoreElements()) {
            NetworkInterface n = ns.nextElement();
            Enumeration<InetAddress> is = n.getInetAddresses();

            while (is.hasMoreElements()) {
                InetAddress i = is.nextElement();

                if (!i.isLoopbackAddress() && !i.isLinkLocalAddress() && !i.isMulticastAddress() && !isSpecialIp(i.getHostAddress()))
                    addrs.add(i);
            }
        }

        return addrs;
    }

    public static Set<String> resolveLocalIps() {
        Set<String> ret = new HashSet<>();

        for (InetAddress addr : resolveLocalAddresses())
            ret.add(addr.getHostAddress());

        return ret;
    }

    public static String getLocalIp() {
        return resolveLocalIps().iterator().next();// 迭代取第一个
    }

    private static boolean isSpecialIp(String ip) {
        if (ip.contains(":"))
            return true;// 排除 IPv6 地址
        if (ip.startsWith("127."))
            return true;
        if (ip.startsWith("169.254."))
            return true;

        return ip.equals("255.255.255.255");
    }

    // simple version
    public static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces.hasMoreElements(); ) {
                NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                if (addresses.hasMoreElements())
                    return addresses.nextElement().getHostAddress();
            }
        } catch (Exception e) {
            log.error("Error when getting host ip address.", e);
        }

        return null;
    }
}
