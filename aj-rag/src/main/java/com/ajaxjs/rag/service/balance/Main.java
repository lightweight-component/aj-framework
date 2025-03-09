package com.ajaxjs.rag.service.balance;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;

// 主类，演示如何使用负载均衡客户端
public class Main {
    public static void main(String[] args) throws NacosException {
        String serviceName = "your-service-name";

        // 使用轮询策略
        LoadBalancer roundRobinLoadBalancer = new RoundRobinLoadBalancer();
        NacosLoadBalancingClient roundRobinClient = new NacosLoadBalancingClient(serviceName, roundRobinLoadBalancer);
        Instance roundRobinInstance = roundRobinClient.getNextInstance();
        System.out.println("Round Robin selected instance: " + roundRobinInstance);

        // 使用随机策略
        LoadBalancer randomLoadBalancer = new RandomLoadBalancer();
        NacosLoadBalancingClient randomClient = new NacosLoadBalancingClient(serviceName, randomLoadBalancer);
        Instance randomInstance = randomClient.getNextInstance();
        System.out.println("Random selected instance: " + randomInstance);

        // 使用加权随机策略
        LoadBalancer weightedRandomLoadBalancer = new WeightedRandomLoadBalancer();
        NacosLoadBalancingClient weightedRandomClient = new NacosLoadBalancingClient(serviceName, weightedRandomLoadBalancer);
        Instance weightedRandomInstance = weightedRandomClient.getNextInstance();
        System.out.println("Weighted Random selected instance: " + weightedRandomInstance);
    }
}