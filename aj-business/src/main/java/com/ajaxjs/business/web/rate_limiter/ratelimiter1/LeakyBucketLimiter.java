package com.ajaxjs.business.web.rate_limiter.ratelimiter1;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * LeakyBucketLimiter
 *
 * @author Tian ZhongBo
 */
public class LeakyBucketLimiter implements RateLimiter {
    private static final int DEFAULT_RATE_LIMIT_PER_SECOND = Integer.MAX_VALUE;

    private static final long NANOSECOND = 1000 * 1000 * 1000;

    private final BlockingQueue<Thread> bucket;

    public LeakyBucketLimiter() {
        this(DEFAULT_RATE_LIMIT_PER_SECOND);
    }

    public LeakyBucketLimiter(int limit) {
        if (limit <= 0) throw new IllegalArgumentException();

        bucket = new LinkedBlockingQueue<>(limit);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();

        TimestampHolder holder = new TimestampHolder(System.nanoTime());
        long interval = NANOSECOND / limit;

        threadPool.submit(() -> {
            while (true) {
                long cur = System.nanoTime();

                if (cur - holder.getTimestamp() >= interval) {
                    Thread thread = bucket.poll();
                    Optional.ofNullable(thread).ifPresent(LockSupport::unpark);
                    holder.setTimestamp(cur);
                }

                try {
                    TimeUnit.NANOSECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void acquire() {
        if (bucket.remainingCapacity() == 0) throw new RejectException();

        Thread thread = Thread.currentThread();
        bucket.add(thread);
        LockSupport.park();
    }
}
