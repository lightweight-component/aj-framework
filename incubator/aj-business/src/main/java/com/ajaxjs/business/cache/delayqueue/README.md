# 基于 DelayQueue 带有回调的超时缓存实现

https://blog.csdn.net/jgteng/article/details/56015699

平时项目中总会用到超时缓存，并且希望超时之后会触发一些后续处理逻辑，比如心跳。一般做法就是把这些数据放到一个队列中，然后线程定时遍历检查是否超时。于是想自己实现一个简单的可以自动超时的缓存队列。在查了一些资料后发现 DelayQueue 是非常合适而且非常简便的，网络上基于 DelayQueue 实现超时缓存队列的文章也很多，本文实现的超时队列原理类似，支持多次检查并增加了一些超时回调方法。


## DelayQueue介绍

DelayQueue 是 BlockingQueu e的一种，其内部的元素需要实现`Delayed`接口并实现`getDelay`方法。`getDelay`方法返回该元素距离失效还剩余的时间，当`<=0`时元素就失效了，就可以从队列中获取到。正是利用 DelayQueue 的这个特性，定时取出 DelayQueue 中失效的元素，来实现超时缓存的功能。


本文实现的超时缓存叫做 ExpireCache，先看下 ExpireCache 的使用和特性。

```java
import java.util.concurrent.TimeUnit;
 

public class ExpireCacheTest {
    public static void main(String[] args) throws InterruptedException {
        //创建一个ExpireCache,
        ExpireCache<String, String> expireCache = ExpireCache
                // 四个参数分别是：首次超时时间，超时继续检查时间间隔，时间单位，超时后是否连续检查
                .setExpireTime(10, 2, TimeUnit.SECONDS, true)
                //超时多少次从队列中移除，第四个参数为true才有效，为false时第一次超时就会被删掉
                .setCheckTimes(5)
                //注册一个超时回调，元素超时后会触发handler方法
                .build(new ExpireCallBack() {
                    @Override
                    public void handler(Object key, boolean isEnd) throws Exception {
                        System.out.println("元素:[" + key + "]超时,超时是否删除：" + isEnd);
                    }
                });
 
        expireCache.put("key_a", "v_a");
        expireCache.put("key_b", "v_b");
 
        TimeUnit.SECONDS.sleep(2);
 
        //expire会重新计算超时时间
        expireCache.expire("key_a");
 
        TimeUnit.SECONDS.sleep(2);
 
        System.out.println(expireCache.get("key_a"));
 
        //put可以更新已存在的值，并重新计算超时时间
        expireCache.put("key_a", "v_a_new");
 
        System.out.println(expireCache.get("key_a"));
 
        Thread.sleep(100000000);
    }
}
```

## 使用说明

看一下创建一个 ExpireCache 的方法

```java
ExpireCache<String, String> expireCache = ExpireCache
// 四个参数分别是：首次超时时间，超时继续检查时间间隔，时间单位，超时后是否连续检查
.setExpireTime(10, 2, TimeUnit.SECONDS, true)
//超时多少次从队列中移除，第四个参数为true才有效，为false时第一次超时就会被删掉
.setCheckTimes(5)
//注册一个超时回调，元素超时后会触发handler方法
.build(new ExpireCallBack() {
    @Override
    public void handler(Object key, boolean isEnd) throws Exception {
        //回调方法逻辑
    }
});
```

API

- setExpireTime(long firstExpireTime, long subsequentExpireTime, TimeUnit unit, boolean continueCheckAfterExpire)
- firstExpireTime : 超时时间- 
- subsequentExpireTime : 下次检查时间间隔，只有第四个参数为true才生效
- unit：时间单位
- continueCheckAfterExpire: 超时后是否继续检查，false:失败后直接从队列中移除，true: 超时不移除，继续检查，和第二个参数配合
- setCheckTimes(Integer times) 设置共检查多少次，当设置连续检查为true时才生效。当检查到达该次数后会从队列中移除。
- build(ExpireCallBack<K, V> callBack) 创建的时候可以传一个回调函数，每次元素失效检查的时候都会触发该回调方法。
- build() 当不需要回调的时候可以使用该方法创建。
- put(K key, V value) 添加元素，有则更新，同时会重新计算超时时间。
- V get(K key)取元素
- boolean containsKey(K key) 判断是否存在该元素
- boolean expire(K key) 更新超时时间，会重新计算超时。
