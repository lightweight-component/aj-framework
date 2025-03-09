package com.ajaxjs.rag.agent;

/**
 * 用于演示多智能体系统的使用
 */
public class MASExample {
    public static void main(String[] args) {
        // 创建 Agent 实例
        Agent agent = new Agent("分析员", new String[]{"数据分析", "逻辑推理"}, "知识图谱模型");
        agent.learn();
        agent.plan();
        agent.reason();
        agent.decide();
        agent.addAbility("机器学习");
        String llmResponse = agent.callLLM();
        System.out.println("大模型回复: " + llmResponse);

        Environment environment = new Environment("复杂环境");
        environment.perceive();
        environment.affect();

        Interaction interaction = new Interaction("合作");
        interaction.interact();

        HierarchicalOrganization hierarchicalOrganization = new HierarchicalOrganization();
        hierarchicalOrganization.organizeAgents();

        EmergentOrganization emergentOrganization = new EmergentOrganization();
        emergentOrganization.organizeAgents();
    }
}