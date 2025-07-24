package com.ajaxjs.business.datastru;


import com.ajaxjs.business.datastru.bloom_filter.BloomFilter;
import com.ajaxjs.business.datastru.bloom_filter.JavaBitSet;
import com.ajaxjs.business.datastru.bloom_filter.YourBitSet;
import org.junit.jupiter.api.Test;

public class TestBloom {
    @Test
    public void RedisBitSetTest() {
        //Don't forget auth password, you better use the configured redis client connection.
        //It should be noted that bloomfilter is not responsible for closing and returning redis connection resources.

        //(falsePositiveProbability, expectedNumberOfElements)
        BloomFilter<String> filter = new BloomFilter<String>(0.0001, 10000);
//        Jedis jedis = new Jedis("127.0.0.1", 6379);
//        jedis.auth("1234");
//        filter.bind(new RedisBitSet(jedis, "bloomfilter:key:name"));

        //if you have a redis cluster
        //Set<HostAndPort> nodes = new HashSet<>();
        //nodes.add(new HostAndPort("127.0.0.1", 6379));

        //filter.bind(new RedisBitSet(new JedisCluster(nodes), "bloomfilter:key:name"));

        //you can also use jedispool
        //JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);
        //Jedis jedis = jedisPool.getResource();
        //filter.bind(new RedisBitSet(jedis, "bloomfilter:key:name"));

        filter.add("filter");
        System.out.println(filter.contains("filter"));
        System.out.println(filter.contains("bloom"));
        filter.add("bitset");
        filter.add("redis");
        System.out.println(filter.contains("bitset"));
        System.out.println(filter.contains("redis"));
        System.out.println(filter.contains("mysql"));
        System.out.println(filter.contains("linux"));
        System.out.println(filter.count());
        System.out.println(filter.isEmpty());
        filter.clear();
        System.out.println(filter.isEmpty());
        System.out.println(filter.contains("filter"));

        /*
         Test results:
         true
         false
         true
         true
         false
         false
         3
         false
         true
         false
         */
    }

    @Test
    public void JavaBitSetTest() {
        //(falsePositiveProbability, expectedNumberOfElements)
        BloomFilter<String> filter = new BloomFilter<String>(0.0001, 10000);
        filter.bind(new JavaBitSet());

        filter.add("filter");
        System.out.println(filter.contains("filter"));
        System.out.println(filter.contains("bloom"));
        filter.add("bitset");
        filter.add("redis");
        System.out.println(filter.contains("bitset"));
        System.out.println(filter.contains("redis"));
        System.out.println(filter.contains("mysql"));
        System.out.println(filter.contains("linux"));
        System.out.println(filter.count());
        System.out.println(filter.isEmpty());
        filter.clear();
        System.out.println(filter.isEmpty());
        System.out.println(filter.contains("filter"));

        /*
         Test results:
         true
         false
         true
         true
         false
         false
         3
         false
         true
         false
         */
    }

    @Test
    public void YourBitSetTest() {
        //(falsePositiveProbability, expectedNumberOfElements)
        BloomFilter<String> filter = new BloomFilter<String>(0.0001, 10000);
        filter.bind(new YourBitSet(1000000));

        filter.add("filter");
        System.out.println(filter.contains("filter"));
        System.out.println(filter.contains("bloom"));
        filter.add("bitset");
        filter.add("redis");
        System.out.println(filter.contains("bitset"));
        System.out.println(filter.contains("redis"));
        System.out.println(filter.contains("mysql"));
        System.out.println(filter.contains("linux"));
        System.out.println(filter.count());
        System.out.println(filter.isEmpty());
        filter.clear();
        System.out.println(filter.isEmpty());
        System.out.println(filter.contains("filter"));

        /*
         Test results:
         true
         false
         true
         true
         false
         false
         3
         false
         true
         false
         */


    }
}
