package com.ajaxjs.rag.agent;

import com.ajaxjs.rag.constant.Config;
import org.json.JSONObject;
import com.ajaxjs.rag.service.LLM.OpenAIChatService;

import java.io.IOException;

/**
 * Agent 类，包含角色、能力和知识模型等属性
 */
public class Agent {
    private final String role; // 角色
    private String[] abilities; // 能力
    private String knowledgeModel; // 知识模型

    // 构造函数
    public Agent(String role, String[] abilities, String knowledgeModel) {
        this.role = role;
        this.abilities = abilities;
        this.knowledgeModel = knowledgeModel;
    }

    // 学习方法
    public void learn() {
        // 这里可以实现具体的学习逻辑
        System.out.println(role + " 正在学习...");
    }

    // 规划方法
    public void plan() {
        // 这里可以实现具体的规划逻辑
        System.out.println(role + " 正在规划...");
    }

    // 推理方法
    public void reason() {
        // 这里可以实现具体的推理逻辑
        System.out.println(role + " 正在推理...");
    }

    // 决策方法
    public void decide() {
        // 这里可以实现具体的决策逻辑
        System.out.println(role + " 正在决策...");
    }

    // 添加能力方法
    public void addAbility(String ability) {
        String[] newAbilities = new String[abilities.length + 1];
        System.arraycopy(abilities, 0, newAbilities, 0, abilities.length);
        newAbilities[abilities.length] = ability;
        abilities = newAbilities;
        System.out.println(role + " 已添加能力: " + ability);
    }

    // 调用大模型能力
    public String callLLM() {
        String apiKey = Config.API_KEY; // 替换为您的API密钥
        String model = Config.LLM_MODEL;// 使用百川Baichuan3-Turbo模型
        String url = Config.LLM_URL;// API的URL
        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

        try {
            // 构建请求参数
            JSONObject params = new JSONObject()
                    .put("model", model)
                    .put("messages", new JSONObject[]{
                            new JSONObject().put("role", "user").put("content", "一些需要询问的内容")
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);

            // 调用大模型生成回复
            return openAIChatService.generateText(url, params);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 调用大模型生成辩论观点
    public String generateDebatePoint(String topic) {
        String apiKey = Config.API_KEY; // 替换为您的API密钥
        String model = Config.LLM_MODEL;    // 使用百川Baichuan3-Turbo模型
        String url = Config.LLM_URL; // API的URL
        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

        try {
            // 构建请求参数
            JSONObject params = new JSONObject()
                    .put("model", model)
                    .put("messages", new JSONObject[]{
                            new JSONObject().put("role", "user").put("content", "针对辩论赛主题：" + topic + "，作为 " + role + " 发表观点")
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);
            // 调用大模型生成回复
            return openAIChatService.generateText(url, params);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
