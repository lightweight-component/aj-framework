//package com.ajaxjs.rag.service.balance;
//
//import com.alibaba.nacos.api.naming.pojo.Instance;
//
//import java.util.List;
//import java.util.Random;
//
//// 随机负载均衡器实现
//public class RandomLoadBalancer implements LoadBalancer {
//    private final Random random = new Random();
//
//    @Override
//    public Instance select(List<Instance> instances) {
//        if (instances == null || instances.isEmpty())
//            return null;
//
//        return instances.get(random.nextInt(instances.size()));
//    }
//}