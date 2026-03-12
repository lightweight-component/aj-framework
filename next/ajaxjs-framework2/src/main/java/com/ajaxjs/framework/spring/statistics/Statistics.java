package com.ajaxjs.framework.spring.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Statistics {
    private String beanName;

    private long beforeInstantiationTime;

    private long afterInstantiationTime;

    private long beforeInitializationTime;

    private long afterInitializationTime;

    public long calculateTotalCostTime() {
        return calculateInstantiationCostTime() + calculateInitializationCostTime();
    }

    public long calculateInstantiationCostTime() {
        return afterInstantiationTime - beforeInstantiationTime;
    }

    public long calculateInitializationCostTime() {
        return afterInitializationTime - beforeInitializationTime;
    }

    public String toConsoleString() {
        long l = calculateTotalCostTime();

        return "\t" + getBeanName() + "\t" + (l < 0 || l > 10000000 ? "N/A" : l + "ms") + "\t\n";
    }
}
