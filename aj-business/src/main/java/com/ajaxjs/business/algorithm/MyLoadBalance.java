package com.ajaxjs.business.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 两种方法实现轮询负载均衡算法 <a href="https://blog.csdn.net/5iasp/article/details/79126041">...</a>
 */
public class MyLoadBalance {
    public static void main(String[] args) {
        List<String> ips = new ArrayList<>();
        ips.add("192.168.0.1");
        ips.add("192.168.0.2");
        ips.add("192.168.0.3");

        // 测试第一种方法
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));
        System.out.println("选择ip:" + doSelect2(ips));

        // 测试第二种方法

        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
        System.out.println("选择ip:" + doSelect(ips));
    }

    private static int index = 0;

    /**
     * 加锁同步实现线程安全的轮询负载均衡算法
     */
    public static synchronized String doSelect(List<String> iplist) {
        if (index >= iplist.size())
            index = 0;

        String ip = iplist.get(index);
        index++;

        return ip;
    }

    private static AtomicInteger index_ = new AtomicInteger(0);

    /**
     * 原子类实现线程安全的轮询负载均衡算法
     */
    public static String doSelect2(List<String> iplist) {
        if (index_.get() >= iplist.size())
            index_ = new AtomicInteger(0);

        String ip = iplist.get(index_.get());
        index_.incrementAndGet();

        return ip;
    }

    public static String doSelect3(List<String> iplist) {
        int idx = index_.getAndIncrement();

        if (idx >= iplist.size()) {
            idx = 0;
            index_.set(0);
        }

        return iplist.get(idx);
    }
}