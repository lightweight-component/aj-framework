package com.ajaxjs.rag;

import com.ajaxjs.rag.agent.Agent;

// Debate 类，用于模拟辩论赛
public class Debate {
    private Agent[] agents;
    private String topic;

    public Debate(Agent[] agents, String topic) {
        this.agents = agents;
        this.topic = topic;
    }

    public void startDebate() {
        System.out.println("辩论赛开始，主题是：" + topic);
        for (Agent agent : agents) {
            String debatePoint = agent.generateDebatePoint(topic);
            System.out.println(agent + " 的观点：" + debatePoint);
        }
        System.out.println("辩论赛结束");
    }
}