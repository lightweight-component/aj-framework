package com.ajaxjs.business.web.load_balance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 可以看到，同一个请求经过hash后找到的处理请求的服务器是同一台
 */
public class TestConsistentHashLoadBalance {
    @Autowired
    private transient ConsistentHashLoadBalance consistentHashLoadBalance;

    @Test
    public void testConsistentLoadBalance() {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(15, 20, 60,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));

        for (int i = 1; i <= 15; i++) {
            final int index = i;
            poolExecutor.execute(() -> System.out.println("当前线程: " + Thread.currentThread().getName() + ",serviceKey:" + "127.0.0." + index + ",选择服务:" + consistentHashLoadBalance.chooseServerInstance("127.0.0." + index + "#1.0.0").getServerName()));
        }
    }

}
