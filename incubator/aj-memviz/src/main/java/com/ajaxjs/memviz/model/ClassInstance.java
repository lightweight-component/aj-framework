package com.ajaxjs.memviz.model;

import java.util.ArrayList;
import java.util.List;

// 类的实例信息
public class ClassInstance {
    public String id;
    public long size; // 浅表大小
    public String formattedSize; // 格式化的浅表大小
    public long retainedSize; // 深度大小（保留大小）
    public String formattedRetainedSize; // 格式化的深度大小
    public int rank; // 在该类中的排名
    public String packageName; // 包名
    public String objectType; // 对象类型
    public boolean isArray; // 是否数组
    public double sizePercentInClass; // 在该类中的内存占比
    public List<FieldInfo> fields; // 添加字段信息列表

    public ClassInstance(String id, long size, String formattedSize,
                         long retainedSize, String formattedRetainedSize, int rank,
                         String packageName, String objectType, boolean isArray, double sizePercentInClass) {
        this.id = id;
        this.size = size;
        this.formattedSize = formattedSize;
        this.retainedSize = retainedSize;
        this.formattedRetainedSize = formattedRetainedSize;
        this.rank = rank;
        this.packageName = packageName;
        this.objectType = objectType;
        this.isArray = isArray;
        this.sizePercentInClass = sizePercentInClass;
        this.fields = new ArrayList<>();
    }
}