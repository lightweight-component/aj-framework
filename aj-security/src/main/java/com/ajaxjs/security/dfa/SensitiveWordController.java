package com.ajaxjs.security.dfa;

import com.ajaxjs.security.dfa.model.FilterResult;
import com.ajaxjs.security.dfa.model.SensitiveWordResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词过滤器 API 控制器
 */
@Slf4j
//@RestController
@RequestMapping("/api/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordController {
    private final SensitiveWordFilter sensitiveWordFilter;

    /**
     * 检查文本是否包含敏感词
     */
    @GetMapping("/check")
    public boolean checkSensitiveWord(@RequestParam String text) {
        return sensitiveWordFilter.containsSensitiveWord(text);
    }

    /**
     * 过滤文本中的敏感词
     */
    @GetMapping("/filter")
    public FilterResult filterText(@RequestParam String text, @RequestParam(defaultValue = "*") String replacement) {
        return sensitiveWordFilter.analyze(text, replacement);
    }

    /**
     * 查找文本中的所有敏感词
     */
    @GetMapping("/find-all")
    public List<SensitiveWordResult> findAllSensitiveWords(@RequestParam String text) {
        return sensitiveWordFilter.findAllWords(text);
    }

    /**
     * 添加敏感词到词库
     */
    @PostMapping("/add")
    public boolean addSensitiveWord(@RequestParam String word) {
        if (word == null || word.trim().isEmpty())
            throw new IllegalArgumentException("敏感词不能为空");

        sensitiveWordFilter.addWord(word);

        return true;
    }

    /**
     * 批量添加敏感词
     */
    @PostMapping("/add-batch")
    public boolean addSensitiveWords(@RequestParam String words) {
        if (words == null || words.isEmpty())
            throw new IllegalArgumentException("敏感词列表不能为空");

        sensitiveWordFilter.addWords(parseWords(words));

        return true;
    }

    /**
     * 重新加载敏感词库
     */
    @PostMapping("/reload")
    public boolean reloadSensitiveWords(@RequestParam String words) {
        if (words == null || words.isEmpty())
            throw new IllegalArgumentException("敏感词列表不能为空");

        sensitiveWordFilter.reloadWords(parseWords(words));

        return true;
    }

    private static List<String> parseWords(String words) {
        List<String> parsed = Arrays.stream(words.split(",")).map(String::strip).filter(word -> !word.isEmpty()).distinct().toList();

        if (parsed.isEmpty())
            throw new IllegalArgumentException("敏感词列表不能为空");

        return parsed;
    }

    /**
     * 获取系统状态。
     */
    @GetMapping("/status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "running");
        response.put("algorithm", "DFA (Deterministic Finite Automaton)");
        response.put("dataStructure", "Trie Tree");
        response.put("features", List.of("高效敏感词检测", "实时文本过滤", "批量敏感词管理", "前缀共享优化", "支持重叠匹配"));

        return response;
    }
}
