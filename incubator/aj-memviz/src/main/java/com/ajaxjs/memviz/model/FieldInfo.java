package com.ajaxjs.memviz.model;


// 字段信息类，用于存储对象的属性信息
public class FieldInfo {
    public String name;        // 字段名称
    public String type;        // 字段类型
    public String value;       // 字段值（字符串表示）
    public long size;          // 字段浅表大小（字节）
    public String formattedSize; // 格式化的浅表大小
    public long retainedSize;  // 字段深度大小（保留大小）
    public String formattedRetainedSize; // 格式化的深度大小
    public double sizePercent;   // 在对象中的占比（基于浅表大小）
    public double retainedSizePercent; // 在对象中的深度大小占比
    public boolean isPrimitive;  // 是否为基本类型
    public boolean isReference;  // 是否为引用类型

    public FieldInfo(String name, String type, String value, long size,
                     String formattedSize, long retainedSize, String formattedRetainedSize,
                     double sizePercent, double retainedSizePercent,
                     boolean isPrimitive, boolean isReference) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.size = size;
        this.formattedSize = formattedSize;
        this.retainedSize = retainedSize;
        this.formattedRetainedSize = formattedRetainedSize;
        this.sizePercent = sizePercent;
        this.retainedSizePercent = retainedSizePercent;
        this.isPrimitive = isPrimitive;
        this.isReference = isReference;
    }

    // 获取字段浅表大小
    public long getSize() {
        return size;
    }

    // 获取字段深度大小
    public long getRetainedSize() {
        return retainedSize;
    }
}