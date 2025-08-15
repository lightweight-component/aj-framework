package com.ajaxjs.business.net.netcard;


import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 多网卡环境下获取MAC地址
 * <p>
 * <a href="https://blog.csdn.net/10km/article/details/78569962">...</a>
 */
public class GetCardInfo {
    public static boolean doFilter(Filter filter, NetworkInterface input) {
        if (null == input)
            return false;

        byte[] hardwareAddress;

        try {
            switch (filter) {
                case UP:
                    return input.isUp();
                case VIRTUAL:
                    return input.isVirtual();
                case LOOPBACK:
                    return input.isLoopback();

                case PHYSICAL_ONLY: {
                    hardwareAddress = input.getHardwareAddress();
                    return null != hardwareAddress
                            && hardwareAddress.length > 0
                            && !input.isVirtual()
                            && !AddressHelper.isVMMac(hardwareAddress);
                }
                case ETH_NIC: {
                    hardwareAddress = input.getHardwareAddress();
                    return null != hardwareAddress
                            && hardwareAddress.length > 0
                            && !input.isVirtual()
                            && !input.isLoopback()
                            && input.getInetAddresses().hasMoreElements();
                }
                case NOVIRTUAL: {
                    hardwareAddress = input.getHardwareAddress();
                    return null != hardwareAddress
                            && hardwareAddress.length > 0
                            && !input.isVirtual();
                }

                case ALL:
                default:
                    return true;
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据过滤器{@code filters}指定的条件(AND)返回网卡设备对象
     *
     * @param filters 网卡类型
     * @return 网卡设备对象
     */
    public static Set<NetworkInterface> getNICs(Filter... filters) {
        return getNICs(GetCardInfo::doFilter, filters);
    }

    public static Set<NetworkInterface> getNICs(BiFunction<Filter, NetworkInterface, Boolean> fn, Filter... filters) {
        if (null == filters)
            filters = new Filter[]{Filter.ALL};

        Set<NetworkInterface> filteredInterfaces = new HashSet<>();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();

                for (Filter filter : filters) {
                    if (fn.apply(filter, ni))
                        filteredInterfaces.add(ni);
                }
            }

            return filteredInterfaces;
        } catch (SocketException e) {
            throw new RuntimeException("Something wrong in Socket.", e);
        }
    }

    /**
     * 根据过滤器(filter)指定的规则返回符合要求的所有物理(非虚拟)网卡的IP地址
     *
     * @param fns
     * @return 过滤后的IP地址集合
     */

    @SafeVarargs
    public static Set<InetAddress> addressesOfNoVirtualNICs(Predicate<InetAddress>... fns) {
        Predicate<InetAddress> allFn = Utils.and(fns);
        Set<InetAddress> sets = new HashSet<>();

        for (NetworkInterface nic : getNoVirtualNICs()) {
            Enumeration<InetAddress> ips = nic.getInetAddresses();

            while (ips.hasMoreElements()) {
                InetAddress ip = ips.nextElement();

                if (allFn.test(ip))
                    sets.add(ip);
            }
        }

        return sets;
    }

    /**
     * 根据过滤器(filter)指定的规则返回符合要求的所有物理网卡的IP地址
     *
     * @param fns
     * @return 过滤后的IP地址集合
     */

    @SafeVarargs
    public static Set<InetAddress> addressesOfPhysicalNICs(Predicate<InetAddress>... fns) {
        Predicate<InetAddress> allFn = Utils.and(fns);
        Set<InetAddress> sets = new HashSet<>();

        for (NetworkInterface nic : getPhysicalNICs()) {
            Enumeration<InetAddress> ips = nic.getInetAddresses();

            while (ips.hasMoreElements()) {
                InetAddress ip = ips.nextElement();

                if (allFn.test(ip))
                    sets.add(ip);
            }
        }

        return sets;
    }

    public static final Predicate<InetAddress> FILTER_IPV4 = (ip) -> ip instanceof Inet4Address;

    public static final Predicate<InetAddress> FILTER_NOT_LINK_LOCAL = (ip) -> !ip.isLinkLocalAddress();

    /**
     * 返回所有物理网卡绑定的IP(ipv4)地址
     *
     * @return IP地址集合
     */
    public static Set<InetAddress> ipv4AddressesOfPhysicalNICs() {
        return addressesOfPhysicalNICs(FILTER_IPV4, FILTER_NOT_LINK_LOCAL);
    }

    /**
     * 返回所有物理(非虚拟)网卡绑定的IP(ipv4)地址
     *
     * @return IP(ipv4)地址集合
     */
    public static Set<InetAddress> ipv4AddressesOfNoVirtualNICs() {
        return addressesOfNoVirtualNICs(FILTER_IPV4, FILTER_NOT_LINK_LOCAL);
    }

    /**
     * 返回所有物理网卡
     *
     * @return 所有物理网卡
     */
    public static Set<NetworkInterface> getPhysicalNICs() {
        return getNICs(Filter.PHYSICAL_ONLY, Filter.UP);
    }

    /**
     * 返回所有物理(非虚拟)网卡
     *
     * @return 物理(非虚拟)网卡集合
     */
    public static Set<NetworkInterface> getNoVirtualNICs() {
        return getNICs(Filter.NOVIRTUAL, Filter.UP);
    }


    /**
     * 将{@code byte[]} 转换为{@code radix}指定格式的字符串
     *
     * @param source
     * @param separator 分隔符
     * @param radix     进制基数
     * @return {@code source}为{@code null}时返回空字符串
     */
    public static String format(byte[] source, String separator, Radix radix) {
        if (null == source)
            return "";

        if (null == separator)
            separator = "";

        List<String> hex = Utils.asList(source).stream().map(item -> String.copyValueOf(new char[]{
                Character.forDigit((item & 240) >> 4, radix.value),
                Character.forDigit(item & 15, radix.value)
        })).collect(Collectors.toList());

        return String.join(separator, hex);
    }

    /**
     * MAC地址格式(16进制)格式化{@code source}指定的字节数组
     */
    public static String formatMac(byte[] source, String separator) {
        return format(source, separator, Radix.HEX).toUpperCase();
    }

    /**
     * 以IP地址格式(点分位)格式化{@code source}指定的字节数组<br>
     */
    public static String formatIp(byte[] source) {
        return format(source, ".", Radix.DEC);
    }

    /**
     * 返回指定{@code address}绑定的网卡的物理地址(MAC)
     *
     * @param address IP 地址
     * @return 指定的{@code address}没有绑定在任何网卡上返回{@code null}
     * 、、
     */
    public static byte[] getMacAddress(InetAddress address) {
        try {
            NetworkInterface nic = NetworkInterface.getByInetAddress(address);

            return null == nic ? null : nic.getHardwareAddress();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param nic       网卡对象
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(NetworkInterface nic, String separator) {
        try {
            return format(nic.getHardwareAddress(), separator, Radix.HEX);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 参见 {@link #getMacAddress(InetAddress)}
     *
     * @param address   IP 地址
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(InetAddress address, String separator) {
        return format(getMacAddress(address), separator, Radix.HEX);
    }
}
