//package org.service.balance;
//
//import com.alibaba.nacos.api.naming.pojo.Instance;
//
//import java.util.List;
//import java.util.Random;
//
//// 加权随机负载均衡器实现
//public class WeightedRandomLoadBalancer implements LoadBalancer {
//    private final Random random = new Random();
//
//    @Override
//    public Instance select(List<Instance> instances) {
//        if (instances == null || instances.isEmpty())
//            return null;
//
//        double totalWeight = 0;
//        for (Instance instance : instances)
//            totalWeight += instance.getWeight();
//
//        double randomWeight = random.nextDouble() * totalWeight;
//        double currentWeight = 0;
//        for (Instance instance : instances) {
//            currentWeight += instance.getWeight();
//            if (currentWeight >= randomWeight) {
//                return instance;
//            }
//        }
//
//        return instances.get(0);
//    }
//}