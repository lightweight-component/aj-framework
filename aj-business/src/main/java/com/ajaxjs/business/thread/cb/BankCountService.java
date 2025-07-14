package com.ajaxjs.business.thread.cb;


import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 银行流水处理服务类
 */
public class BankCountService implements Runnable {
    private final int threadCount = 4;

    private Random random = new Random();

    /**
     * 创建4个屏障类，都处理完之后执行当前类的run方法
     */
    private CyclicBarrier c = new CyclicBarrier(threadCount, this);

    private ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    private ConcurrentHashMap<String, Integer> countMap = new ConcurrentHashMap<>();

    /**
     * 开启线程池进行计算
     */
    private void count() {
        System.out.println(">>>>>开始计算>>>>>");

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                //计算当前sheet的银行流水，模拟计算
                int value = random.nextInt(10000);

                try {
                    Thread.sleep(value);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                String threadName = Thread.currentThread().getName();
                countMap.put(threadName, value);
                System.out.println("[" + threadName + "]计算完成:" + value + ", 等待汇总...");

                // 银行流水计算完成,插入一个屏蔽，等待其他线程的计算
                try {
                    c.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void run() {
        int total = 0;
        System.out.println("开始汇总...");

        //汇总结果
        for (String key : countMap.keySet())
            total += countMap.get(key);

        for (Entry<String, Integer> entry : countMap.entrySet()) {
            //total += entry.getValue().intValue();
        }

        //将结果输出
        System.out.println("银行总流水===" + total);

        //关闭线程池
        if (executor != null) {
            executor.shutdown();
            System.out.println(">>>>>计算结束>>>>>");
        }
    }

    public static void main(String[] args) {
        BankCountService service = new BankCountService();
        service.count();
    }
}