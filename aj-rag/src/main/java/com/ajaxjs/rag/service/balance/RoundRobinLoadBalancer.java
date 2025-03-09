//package com.ajaxjs.rag.service.balance;
//
//import com.alibaba.nacos.api.naming.pojo.Instance;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//// 轮询负载均衡器实现
//public class RoundRobinLoadBalancer implements LoadBalancer {
//    private final AtomicInteger index = new AtomicInteger(0);
//
//    @Override
//    public Instance select(List<Instance> instances) {
//        if (instances == null || instances.isEmpty())
//            return null;
//
//        int currentIndex = index.getAndIncrement() % instances.size();
//        return instances.get(currentIndex);
//    }
//}