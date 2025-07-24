package com.ajaxjs.redis.queue;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用redis实现简单队列
 * <p>
 * 一个考试项目，每场考试有上千人在线答题，在考试结束时提交考试结果保存到数据库。因为保存的考试数据比较多，入库慢导致前端请求超时和服务崩溃。
 * 考虑使用队列解决，队列作用是异步、解耦和削峰。
 * 具体思路是将保存考试分解成两个步骤：
 * 第一步：前端提交考试成绩保存到redis
 * 第二步：后台异步从redis获取考试成绩并入库
 * 好处是前端提交考试后执行第一步操作即可完成提交，提高了请求的响应时间；后台异步将redis中的数据入库，减少了服务器和数据库的压力。
 * <p>
 * <a href="https://blog.xinpapa.com/2020/03/30/redis-queue/">...</a>
 *
 例子中使用了一个线程来消费队列，如果数据库性能允许也可以启用多个线程消费队列。
 因为rpop操作是原子性的，所以即使多个线程消费一个队列，也不会出现重复消费的问题。

 */
@Service
public class ExamQueueService implements InitializingBean {
    //队列名
    public final String QUEUE_NAME = "examRecord";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 入队
     */
    public Long enqueue(Object record) {
//        JSONObject object = new JSONObject();
//        object.put("record", record);

        return redisTemplate.opsForList().leftPush(QUEUE_NAME, record);
    }

    /**
     * 出队
     */
    public Object dequeue() {
        return redisTemplate.opsForList().rightPop(QUEUE_NAME);
    }

    /**
     * 消费队列（此方法在Spring容器初始化的时候执行）
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void afterPropertiesSet() {
        //启用一个线程来消费队列，将数据从队列中取出来并入库
        //数据库性能允许的情况下，可以启用多个线程
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    //从队列中获取一条数据（获取后队列中会自动删除此数据）
                    Object dequeue = this.dequeue();

                    if (dequeue == null) {
                        //如果队列为空，则休眠一秒
                        Thread.sleep(1000L);
                    } else {
                        //数据入库...略
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }
}