package com.ajaxjs.memviz.model;

import java.util.ArrayList;
import java.util.List;

public class GraphModel {
    public List<Node> nodes = new ArrayList<>();
    public List<Link> links = new ArrayList<>();
    public List<TopClassStat> top100Classes = new ArrayList<>(); // Top100类统计列表
    public int totalObjects; // 总对象数
    public long totalMemory; // 总内存占用
    public String formattedTotalMemory; // 格式化的总内存

}