package com.ajaxjs.business.web.rate_limiter.ratelimiter1;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * TokenBucketLimiter
 *
 * @author Tian ZhongBo
 */
public class TokenBucketLimiter implements RateLimiter {
    private static final int DEFAULT_RATE_LIMIT_PER_SECOND = Integer.MAX_VALUE;

    private static final long NANOSECOND = 1000 * 1000 * 1000;

    private static final Object TOKEN = new Object();

    private final Queue<Object> tokenBucket;

    public TokenBucketLimiter() {
        this(DEFAULT_RATE_LIMIT_PER_SECOND);
    }

    public TokenBucketLimiter(int limit) {
        if (limit <= 0) throw new IllegalArgumentException();

        tokenBucket = new LinkedBlockingQueue<>(limit);
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        TimestampHolder holder = new TimestampHolder(System.nanoTime());
        long interval = NANOSECOND / limit;

        threadPool.submit(() -> {
            while (true) {
                long cur = System.nanoTime();

                if (cur - holder.getTimestamp() >= interval) {
                    tokenBucket.offer(TOKEN);
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
        Object token = tokenBucket.poll();
        if (Objects.isNull(token)) throw new RejectException();
    }


    private static final Integer PAGE_SIZE = 3;

    // List 分页
    public static void listPage(String[] args) {
        List<Long> datas = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L);

        //总记录数
        int totalCount = datas.size();

        //分多少次处理
        int requestCount = totalCount / PAGE_SIZE;

        for (int i = 0; i <= requestCount; i++) {
            int fromIndex = i * PAGE_SIZE;
            //如果总数少于PAGE_SIZE,为了防止数组越界,toIndex 直接使用 totalCount 即可
            int toIndex = Math.min(totalCount, (i + 1) * PAGE_SIZE);

            List<Long> subList = datas.subList(fromIndex, toIndex);
            System.out.println(subList);

            //总数不到一页或者刚好等于一页的时候,只需要处理一次就可以退出 for 循环了
            if (toIndex == totalCount)
                break;
        }

    }
}
