package org.example.service;

//import com.ajaxjs.desensitize.annotation.Desensitize;

import com.ajaxjs.springboot.annotation.JsonMessage;
import com.ajaxjs.util.cache.leveltwocache.LevelTwoCacheManager;
import org.example.controller.FooController;
import org.example.model.Foo;
import org.example.model.User;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Service
public class FooService implements FooController {
    //    @Autowired(required = false)
//    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    //    @Resource
    LevelTwoCacheManager cacheManager;

    @Override
    @JsonMessage("返回 Foo")
//    @TimeSignatureVerify
    public Foo getFoo() {
        cacheManager.createRedisCache().put("foo2", "hihi");
        System.out.println(cacheManager.getCache("foo").get("foo2").get());
//        RedisUtils.getInstance().set("bar", "888");
//        redisTemplate.opsForValue().set("foo", 1);
//        EntityManager entityManager = JpaUtil.getEntityManager();
//        entityManager.getTransaction().begin();
//
//        MyEntity entity = new MyEntity();
//        entity.setName("Example");
//        entityManager.persist(entity);
//
//        entityManager.getTransaction().commit();
//        entityManager.close();

        Foo foo = new Foo();
        foo.setName("hi");
//        throw new BusinessException("业务异常");

        return foo;
    }

    //    @Resource
    private RedissonClient redissonClient;

    @Override
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
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();//释放锁
            }
        }

    }

    @Override
    public boolean jsonSubmit(@RequestBody User user) {
        // 处理接收到的 user 对象
        System.out.println("Received user: " + user.getName() + ", " + user.getAge());
        return false;
    }

    @Override
    public User User() {
        User user = new User();
        user.setAge(1);
        user.setName("tom");

        return user;
    }

    @Override
//    @Desensitize
    public User UserDesensitize() {
        User user = new User();
        user.setAge(1);
        user.setName("tom");
        user.setPhone("13711118120");

        return user;
    }

    @Autowired
    JSONPlaceHolderClient jsonPlaceHolderClient;

    @Override
    public boolean testOpenFeign() {
        System.out.println(jsonPlaceHolderClient);
        List<Post> list = jsonPlaceHolderClient.getPosts();
//        System.out.println(list);
        Map<String, Object> map = jsonPlaceHolderClient.getPostMapById(1L);
//        System.out.println(map);
        return true;
    }


}
