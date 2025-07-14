package com.ajaxjs.business.web.load_balance;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TestWeightRoundRoBinLoadBalance {
    @Autowired
    private transient WeightedRoundRoBinLoadBalance weightedRoundRoBinLoadBalance;

    @Test
    public void testWeightedRoundRoBinLoadBalance() {
        ExecutorService executorService = Executors.newFixedThreadPool(18);

        for (int i = 1; i <= 18; i++)
            executorService.execute(() -> System.out.println("当前线程: " + Thread.currentThread().getName() + "选择服务:" + weightedRoundRoBinLoadBalance.chooseServerInstance().getServerName()));
    }

}