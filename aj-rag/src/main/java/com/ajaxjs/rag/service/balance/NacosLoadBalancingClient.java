//package com.ajaxjs.rag.service.balance;
//
//import com.alibaba.nacos.api.NacosFactory;
//import com.alibaba.nacos.api.exception.NacosException;
//import com.alibaba.nacos.api.naming.NamingService;
//import com.alibaba.nacos.api.naming.listener.Event;
//import com.alibaba.nacos.api.naming.listener.EventListener;
//import com.alibaba.nacos.api.naming.listener.NamingEvent;
//import com.alibaba.nacos.api.naming.pojo.Instance;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Properties;
//
//// 服务发现和负载均衡客户端
//public class NacosLoadBalancingClient {
//    private static final String SERVER_ADDRESSES = "http://124.223.85.176:8848";
//    private static final String NAMESPACE = "public";
//    private final NamingService namingService;
//    private final List<Instance> instances = new ArrayList<>();
//    private final LoadBalancer loadBalancer;
//
//    public NacosLoadBalancingClient(String serviceName, LoadBalancer loadBalancer) throws NacosException {
//        this.loadBalancer = loadBalancer;
//        Properties properties = new Properties();
//        properties.put("serverAddr", SERVER_ADDRESSES);
//        properties.put("namespace", NAMESPACE);
//        namingService = NacosFactory.createNamingService(properties);
//        namingService.subscribe(serviceName, new EventListener() {
//            @Override
//            public void onEvent(Event event) {
//                if (event instanceof NamingEvent) {
//                    NamingEvent namingEvent = (NamingEvent) event;
//                    instances.clear();
//                    instances.addAll(namingEvent.getInstances());
//                    System.out.println("Service instances updated: " + instances);
//                }
//            }
//        });
//
//        instances.addAll(namingService.getAllInstances(serviceName));
//    }
//
//    public Instance getNextInstance() {
//        return loadBalancer.select(instances);
//    }
//}