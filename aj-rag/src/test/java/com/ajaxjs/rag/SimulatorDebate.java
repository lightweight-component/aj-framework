package com.ajaxjs.rag;

import com.ajaxjs.rag.agent.Agent;

public class SimulatorDebate {
    public static void main(String[] args) {
        // 创建 Agent 实例
        Agent agent1 = new Agent("正方", new String[]{"逻辑推理", "语言表达"}, "知识图谱模型");
        Agent agent2 = new Agent("反方", new String[]{"批判性思维", "数据分析"}, "知识图谱模型");
        // 创建 Debate 实例
        Debate debate = new Debate(new Agent[]{agent1, agent2}, "人工智能是否会取代人类工作");
        // 开始辩论赛
        debate.startDebate();
    }
}
