package com.ajaxjs.business.web.rate_limiter.ratelimiter1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RateCounter implements RateLimiter {
    private static final int DEFAULT_RATE_LIMIT_PER_SECOND = Integer.MAX_VALUE;

    private final int limit;

    private final AtomicInteger counter;

    public RateCounter() {
        this(DEFAULT_RATE_LIMIT_PER_SECOND);
    }

    public RateCounter(int limit) {
        if (limit < 0)
            throw new IllegalArgumentException("limit less than zero");

        this.limit = limit;
        counter = new AtomicInteger();
        TimestampHolder holder = new TimestampHolder();
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            while (true) {
                long cur = System.currentTimeMillis();
                if (cur - holder.getTimestamp() >= 1000) {
                    holder.setTimestamp(cur);
                    counter.set(0);
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void acquire() {
        if (counter.incrementAndGet() > limit)
            throw new RejectException();
    }
}
