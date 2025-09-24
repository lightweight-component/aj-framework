package com.ajaxjs.memviz.model;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public String id;        // objectId 或 class@id
    public String label;     // 类名(短)
    public String className; // 类名(全)
    public long shallowSize; // 浅表大小
    public long deepSize;    // 深度大小
    public String category;  // JDK/第三方/业务
    public int instanceCount; // 该类的实例总数
    public String formattedSize; // 格式化的浅表大小显示
    public String formattedDeepSize; // 格式化的深度大小显示
    public String packageName; // 包名
    public boolean isArray; // 是否为数组类型
    public String objectType; // 对象类型描述
    public List<FieldInfo> fields; // 对象的字段信息

    public Node(String id, String label, String className, long shallowSize, String category) {
        this.id = id;
        this.label = label;
        this.className = className;
        this.shallowSize = shallowSize;
        this.category = category;
        this.fields = new ArrayList<>();
    }

    // 增强构造函数
    public Node(String id, String label, String className, long shallowSize, String category,
                int instanceCount, String formattedSize, String packageName, boolean isArray, String objectType,
                long deepSize, String formattedDeepSize) {
        this.id = id;
        this.label = label;
        this.className = className;
        this.shallowSize = shallowSize;
        this.deepSize = deepSize;
        this.category = category;
        this.instanceCount = instanceCount;
        this.formattedSize = formattedSize;
        this.formattedDeepSize = formattedDeepSize;
        this.packageName = packageName;
        this.isArray = isArray;
        this.objectType = objectType;
        this.fields = new ArrayList<>();
    }
}