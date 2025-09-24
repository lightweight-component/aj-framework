package com.ajaxjs.memviz.model;


import java.util.ArrayList;
import java.util.List;

// Top100类统计信息
public class TopClassStat {
    public String className;
    public String shortName;
    public String packageName;
    public String category;
    public int instanceCount; // 实例数量
    public long totalSize; // 该类所有实例的总内存（浅表大小）
    public String formattedTotalSize; // 格式化的总内存
    public long totalDeepSize; // 该类所有实例的总深度大小
    public String formattedTotalDeepSize; // 格式化的总深度大小
    public long avgSize; // 平均每个实例大小（浅表）
    public String formattedAvgSize; // 格式化的平均大小
    public long avgDeepSize; // 平均每个实例深度大小
    public String formattedAvgDeepSize; // 格式化的平均深度大小
    public int rank; // 排名
    public List<ClassInstance> topInstances; // 该类中内存占用最大的实例列表

    public TopClassStat(String className, String shortName, String packageName, String category,
                        int instanceCount, long totalSize, String formattedTotalSize,
                        long totalDeepSize, String formattedTotalDeepSize,
                        long avgSize, String formattedAvgSize,
                        long avgDeepSize, String formattedAvgDeepSize,
                        int rank, List<ClassInstance> topInstances) {
        this.className = className;
        this.shortName = shortName;
        this.packageName = packageName;
        this.category = category;
        this.instanceCount = instanceCount;
        this.totalSize = totalSize;
        this.formattedTotalSize = formattedTotalSize;
        this.totalDeepSize = totalDeepSize;
        this.formattedTotalDeepSize = formattedTotalDeepSize;
        this.avgSize = avgSize;
        this.formattedAvgSize = formattedAvgSize;
        this.avgDeepSize = avgDeepSize;
        this.formattedAvgDeepSize = formattedAvgDeepSize;
        this.rank = rank;
        this.topInstances = topInstances != null ? topInstances : new ArrayList<>();
    }
}