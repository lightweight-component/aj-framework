package com.ajaxjs.business.logqueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import javax.annotation.PostConstruct;
public class ConsumeSalesLogQueue {
    SalesLogQueue salesLogService;

    //    @PostConstruct
    public void startThread() {
        ExecutorService e = Executors.newFixedThreadPool(2);//两个大小的固定线程池
        e.submit(new PollSalesLog(salesLogService));
        e.submit(new PollSalesLog(salesLogService));
    }

    static class PollSalesLog implements Runnable {
        SalesLogQueue salesLogService;

        public PollSalesLog(SalesLogQueue salesLogService) {
            this.salesLogService = salesLogService;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SalesLog salesLog = SalesLogQueue.getInstance().poll();

//                    if (salesLog != null)
//                        salesLogService.saveSalesLog(salesLog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
