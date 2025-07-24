package com.ajaxjs.business.web.load_balance;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * 按照服务器权重的赋值均衡算法
 * 对一些性能高,负载低的服务器,我们可以给它更高的权值,以便处理更多的请求
 */
@Service("weightedRoundRoBinLoadBalance")
public final class WeightedRoundRoBinLoadBalance extends AbstractLoadBalance {

    @Resource(name = "dynamicUploadRule")
    private transient DynamicUploadRule dynamicUploadRule;

    // 所有服务器负载因子的最大公约数
    private int gcd;

    // 负载因子的最大值
    private int max;

    // 轮询周期
    private int cycle;

    // 当前使用的轮询的索引值
    private int currentIndex = -1;

    /**
     * 一个轮询周期的服务集合,长度为负载因子除以最大公约数相加确定,可重复,AtomicReference保证原子操作
     */
    private final AtomicReference<List<String>> WRRList = new AtomicReference<>();

    @Override
    public void init() {
        super.init();
        buildWRRList();
    }

    @Override
    public ServiceInstance chooseServerInstance() {
        if (!isNotEmpty(WRRList.get())) {
//            logger.info("还未建立起权值轮询服务集合,采用随机算法返回可利用的服务");
            return super.chooseServerInstance();
        }

        ServiceInstance serviceInstance = null;
        synchronized (this) {
            int index = 0;

            while (index < cycle && null == serviceInstance) {
                currentIndex = (currentIndex + 1) % WRRList.get().size();
                String serviceName = WRRList.get().get(currentIndex);
                serviceInstance = getServiceInstanceByServiceName(serviceName);

                if (null == serviceInstance || serviceInstance.isIsolated())
                    index++;
            }
        }
        return serviceInstance;
    }

    /**
     * 初始化可利用的轮询周期内的服务集合
     */
    private void buildWRRList() {
        boolean isGetSucc = false;
        if (!getServiceInstanceList().isEmpty()) {
//            logger.info("获取的服务列表不为空,开始初始化各个服务器对应的负载因子");
            isGetSucc = calcLoadFactors();
        }

        if (isGetSucc) {
            // 生成轮询的server集合
            int total = getServiceInstanceList().size();
            // 上一次服务库索引
            int i = -1;
            // 上一次权值
            int cw = 0;
            List<String> newWrrList = new ArrayList<>(total);
            // 下面的算法，第一次分配时，把当前权重设置为最大权重，服务器中权重大于等于当前权重的，都会分配负载；
            // 第一轮分配完后，当前权重减最大公约数，进行第二轮分配；
            // 如此循环到当前权重为负，则把当前权重重置为最大权重，重新进行循环。一直到轮回周期

            for (int j = 0; j < cycle; j++) {
                while (true) {
                    i = (i + 1) % total;// 服务器下标

                    if (i == 0) {
                        cw = cw - gcd;// 获得处理的权重

                        if (cw <= 0) {
                            cw = max;
                            // 如果没有需要分配的服务
                            if (cw == 0) {
                                newWrrList.add(null);
                                break;
                            }
                        }
                    }
                    ServiceInstance serviceInstance = getServiceInstanceList()
                            .get(i);
                    String serverName = serviceInstance.getServerName();
                    // 如果被轮询到的server权值满足要求记录servername.
                    if (serviceInstance.getQzValue() >= cw) {
                        newWrrList.add(serverName);
                        break;
                    }
                }
            }
            WRRList.set(newWrrList);
        }
    }

    /**
     * 初始化最大公约数，最大权值,轮询周期
     */
    private boolean calcLoadFactors() {
        // 获取所有服务器列表,根据列表返回一开始设置的各服务器负载因子(这里可以是动态文件,可以是查询DB等方式)
        /*
         * 1:获取所有的服务名 2:根据服务名获取不同的服务设置的负载因子
         */
        // 获取所有服务的负载因子
        List<Integer> factors = getDefault();
        if (null == factors || factors.size() == 0)
            return false;

        // 计算最大公约数 eg:10,20的最大公约数是10
        gcd = calcMaxGCD(factors);
        max = calcMaxValue(factors);
        cycle = calcCycle(factors, gcd);

        return true;
    }

    /**
     * 计算轮回周期,每个因子/最大公约数之后相加
     * eg:100/100 + 200/100 + 300/100 = 6
     *
     * @param factors 存储所有的负载因子
     * @param gcd     最大公约数
     */
    private int calcCycle(List<Integer> factors, int gcd) {
        int cycle = 0;
        for (Integer factor : factors) cycle += factor / gcd;

        return cycle;
    }

    /**
     * 计算负载因子最大值
     */
    private int calcMaxValue(List<Integer> factors) {
        int max = 0;

        for (Integer factor : factors) {
            if (factor > max)
                max = factor;
        }

        return max;
    }

    /**
     * 计算最大公约数
     */
    private int calcMaxGCD(List<Integer> factors) {
        int max = 0;

        for (Integer factor : factors) {
            if (factor > 0)
                max = divisor(factor, max);
        }

        return max;
    }

    /**
     * 使用辗转相减法计算
     *
     * @param m 第一次参数
     * @param n 第二个参数
     * @return int最大公约数
     */
    private int divisor(int m, int n) {
        if (m < n) {
            int temp;
            temp = m;
            m = n;
            n = temp;
        }

        if (0 == n)
            return m;

        return divisor(m - n, n);
    }

    /**
     * 获取默认的负载因子
     */
    private List<Integer> getDefault() {
        List<Integer> list = new ArrayList<>();
        List<ServiceInstance> instances = dynamicUploadRule.getServiceInstanceRule();

        for (final ServiceInstance serviceInstance : instances)
            list.add(serviceInstance.getQzValue());

        return list;
    }

    /**
     * 判断非空
     */
    private boolean isNotEmpty(List<String> list) {
        return null != list && list.size() > 0;
    }

}