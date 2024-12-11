package org.example.service;

import com.ajaxjs.model.MailVo;
import com.ajaxjs.service.message.ISendEmail;
import com.ajaxjs.service.tools.IIdCard;
import com.ajaxjs.springboot.annotation.JsonMessage;
import org.apache.dubbo.config.bootstrap.builders.ReferenceBuilder;
import org.example.controller.FooController;
import org.example.model.Foo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FooService implements FooController {
    //    @Autowired(required = false)
    @Resource
    private RedisTemplate<String, Integer> redisTemplate;

    @Override
    @JsonMessage("返回 Foo")
//    @TimeSignatureVerify
    public Foo getFoo() {
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

    @Autowired
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


}
