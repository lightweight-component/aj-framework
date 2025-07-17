package com.ajaxjs.business.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 带权重的随机选择
 *
 * @author <a href="https://www.zifangsky.cn/1545.html">...</a>
 */
public class WeightRandom {
    /**
     * 选项数组
     */
    private final Item[] options;

    /**
     * 权重的临界值
     */
    private BigDecimal[] criticalWeight;

    private final Random rnd = new Random();

    public WeightRandom(Item[] options) {
        if (options == null || options.length < 1)
            throw new IllegalArgumentException("选项数组存在异常！");

        this.options = options;

        // 总权重
        BigDecimal sumWeights = BigDecimal.ZERO;
        // 权重的临界值
        criticalWeight = new BigDecimal[options.length + 1];

        // 1. 计算总权重
        for (Item item : options)
            sumWeights = sumWeights.add(BigDecimal.valueOf(item.getWeight()));

        // 2. 计算每个选项的临界值
        BigDecimal tmpSum = BigDecimal.ZERO;
        criticalWeight[0] = tmpSum;

        for (int i = 0; i < options.length; i++) {
            tmpSum = tmpSum.add(BigDecimal.valueOf(options[i].getWeight()));
            criticalWeight[i + 1] = tmpSum.divide(sumWeights, 2, BigDecimal.ROUND_HALF_UP);
        }
    }

    /**
     * 随机函数
     */
    public String nextItem() {
        double randomValue = rnd.nextDouble();
        // 查找随机值所在区间

        BigDecimal rndValue = new BigDecimal(randomValue);
        int high = criticalWeight.length - 1;
        int low = 0;
        int median = (high + low) / 2;
        int index;
        BigDecimal medianValue;

        while (median != low && median != high) {
            medianValue = criticalWeight[median];

            if (rndValue.compareTo(medianValue) == 0)
                break;
            else if (rndValue.compareTo(medianValue) > 0) {
                low = median;
                median = (high + low) / 2;
            } else {
                high = median;
                median = (high + low) / 2;
            }
        }

        index = median;

        return options[index].getName();
    }

    /**
     * 需要随机的 item
     */
    @Data
    @AllArgsConstructor
    public static class Item {
        /**
         * 名称
         */
        private String name;

        /**
         * 权重
         */
        private double weight;

    }
}
