package com.ajaxjs.redis;

//import com.ajaxjs.desensitize.annotation.Desensitize;


import com.ajaxjs.redis.leveltwocache.LevelTwoCacheManager;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FooService {
    @Autowired(required = false)

    private RedisTemplate<String, Integer> redisTemplate;

    @Resource
    LevelTwoCacheManager cacheManager;

    public void getFoo() {
        cacheManager.createRedisCache().put("foo2", "hihi");
        System.out.println(cacheManager.getCache("foo").get("foo2").get());
//        RedisUtils.getInstance().set("bar", "888");
//        redisTemplate.opsForValue().set("foo", 1);
    }

    @Resource
    private RedissonClient redissonClient;

    public boolean rLock() {
        // https://blog.csdn.net/w8y56f/article/details/114699555
        RLock lock = redissonClient.getLock("10000");

        try {
            lock.lock();//阻塞
            //            boolean b = lock.tryLock();//非阻塞
            System.out.println(redisTemplate);
            Object v = redisTemplate.opsForValue().get("stock");

            int stock = v == null ? 0 : Integer.parseInt(String.valueOf(v));
            if (stock > 0) {
                //下单
                stock -= 1;
                redisTemplate.opsForValue().set("stock", stock);
                System.out.println("扣减成功，库存stock：" + stock);

            } else {
                //没库存
                System.out.println("扣减失败，库存不足");
            }

            return true;
        } finally {
            if (lock.isHeldByCurrentThread())
                lock.unlock();//释放锁
        }
    }
}
