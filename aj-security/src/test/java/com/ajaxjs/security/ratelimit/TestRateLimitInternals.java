package com.ajaxjs.security.ratelimit;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests package-visible rate-limiting implementation details.
 */
class TestRateLimitInternals {
    @Test
    void testChunkSizeAndClosedStream() throws IOException {
        assertEquals(1024, RateLimitedOutputStream.calculateOptimalChunkSize(1));
        assertEquals(4096, RateLimitedOutputStream.calculateOptimalChunkSize(300 * 1024L));
        assertEquals(8192, RateLimitedOutputStream.calculateOptimalChunkSize(2 * 1024 * 1024L));
        assertEquals(16384, RateLimitedOutputStream.calculateOptimalChunkSize(6 * 1024 * 1024L));

        RateLimitedOutputStream stream = new RateLimitedOutputStream(new ByteArrayOutputStream(), 1024 * 1024L);
        stream.close();
        assertThrows(IOException.class, stream::checkClosed);
    }

    @Test
    void testManagerPackageVisibleOperations() {
        BandwidthLimitManager manager = new BandwidthLimitManager();
        try {
            TokenBucket bucket = manager.getGlobalBucket(10, 5);
            assertSame(bucket, manager.getGlobalBucket(10, 5));

            ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
            assertNotNull(manager.getOrCreateBucket(buckets, "api", 10, 5));
            manager.cleanupMap(buckets, System.currentTimeMillis() + 10 * 60 * 1000L, "API");
            assertTrue(buckets.isEmpty());
            manager.cleanupIdleBuckets();
        } finally {
            manager.shutdown();
        }
    }

    @Test
    void testTokenBucketPackageVisibleOperations() {
        TokenBucket bucket = new TokenBucket(10, 10);
        assertEquals(0, bucket.refillAndCalculateWait(1));
        bucket.acquire(10);
        assertTrue(bucket.refillAndCalculateWait(1) > 0);
        bucket.refill();
        bucket.sleepNanos(0);
    }

    @Test
    void testControllerJsonSerializer() {
        TestController controller = new TestController();
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("name", "demo");
        values.put("count", 2);
        assertEquals("{\"name\":\"demo\",\"count\":2}", controller.toJson(values));
    }
}
