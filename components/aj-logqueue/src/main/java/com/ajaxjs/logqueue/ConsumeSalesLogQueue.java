package com.ajaxjs.logqueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumeSalesLogQueue {
    SalesLogQueue salesLogService;

    @PostConstruct
    public void startrtThread() {
        ExecutorService e = Executors.newFixedThreadPool(2);//两个大小的固定线程池
        e.submit(new PollSalesLog(salesLogService));
        e.submit(new PollSalesLog(salesLogService));
    }

    class PollSalesLog implements Runnable {
        SalesLogQueue salesLogService;

        public PollSalesLog(SalesLogQueue salesLogService) {
            this.salesLogService = salesLogService;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    SalesLog salesLog = SalesLogQueue.getInstance().poll();

                    if (salesLog != null)
                        salesLogService.saveSalesLog(salesLog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
