package com.ajaxjs.security.dfa;

import com.ajaxjs.security.dfa.model.FilterResult;
import com.ajaxjs.security.dfa.model.SensitiveWordResult;
import com.ajaxjs.security.dfa.model.TrieNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于 Trie 的敏感词匹配器。查询持有读锁，词库更新持有写锁。
 * 大小写按 Unicode 码点执行简单小写映射，不依赖系统语言，也不改变原文索引。
 * 返回的位置使用 Java/JavaScript 共用的 UTF-16 下标，结束位置包含在匹配范围中。
 */
//@Service
public class SensitiveWordFilter {
    /**
     * 查询使用读锁，添加、批量添加及重新加载使用写锁；一次过滤只查找一次，返回字段保持一致
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private TrieNode root;

    private int minWordLength;

    public SensitiveWordFilter() {
        this(List.of("apple", "app", "application", "apply", "orange"));
    }

    public SensitiveWordFilter(List<String> words) {
        reloadWords(words);
    }

    /**
     * 忽略 null、空白词条，统一在计算最短长度和建树之前规范化。
     */
    private static List<String> normalize(List<String> words) {
        Objects.requireNonNull(words, "词库列表不能为空");

        return words.stream().filter(Objects::nonNull).map(String::strip).filter(word -> !word.isEmpty()).distinct().toList();
    }

    private static void insert(TrieNode root, String word) {
        TrieNode node = root;

        for (int offset = 0; offset < word.length(); ) {
            int codePoint = word.codePointAt(offset);
            node = node.addChild(Character.toLowerCase(codePoint));
            offset += Character.charCount(codePoint);
        }

        node.setEnd(true);
        node.setKeyword(word);
    }

    /**
     * 重建完成后，在写锁内一起发布根节点和最短词长度。空列表表示清空词库。
     */
    public void reloadWords(List<String> words) {
        List<String> normalized = normalize(words);
        TrieNode newRoot = new TrieNode();

        for (String word : normalized)
            insert(newRoot, word);
        int minimum = normalized.stream().mapToInt(String::length).min().orElse(Integer.MAX_VALUE);
        lock.writeLock().lock();

        try {
            root = newRoot;
            minWordLength = minimum;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addWord(String word) {
        addWords(java.util.Collections.singletonList(word));
    }

    /**
     * 整批添加在一个写锁内完成，查询不会看到半批词条。
     */
    public void addWords(List<String> words) {
        List<String> normalized = normalize(words);
        lock.writeLock().lock();

        try {
            for (String word : normalized) {
                insert(root, word);
                minWordLength = Math.min(minWordLength, word.length());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean containsSensitiveWord(String text) {
        return !scan(text, true).isEmpty();
    }

    /**
     * 保留所有匹配，包括同一起点和不同起点的重叠词。
     */
    public List<SensitiveWordResult> findAllWords(String text) {
        return scan(text, false);
    }

    private List<SensitiveWordResult> scan(String text, boolean firstOnly) {
        lock.readLock().lock();

        try {
            List<SensitiveWordResult> matches = new ArrayList<>();

            if (text == null || text.length() < minWordLength)
                return matches;

            for (int start = 0; start < text.length(); start += Character.charCount(text.codePointAt(start))) {
                TrieNode node = root;

                for (int end = start; end < text.length(); ) {
                    int codePoint = text.codePointAt(end);
                    node = node.getChild(Character.toLowerCase(codePoint));

                    if (node == null)
                        break;

                    end += Character.charCount(codePoint);

                    if (node.isEnd()) {
                        matches.add(new SensitiveWordResult(text.substring(start, end), start, end - 1));

                        if (firstOnly)
                            return matches;
                    }
                }
            }

            return matches;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String filter(String text, String replacement) {
        return analyze(text, replacement).getFilteredText();
    }

    /**
     * 一次查询得到全部结果，过滤文本、标记和数量始终对应同一版本的词库。
     */
    public FilterResult analyze(String text, String replacement) {
        List<SensitiveWordResult> words = findAllWords(text);
        String filtered = replace(text, replacement == null ? "*" : replacement, words);

        return new FilterResult(text, filtered, !words.isEmpty(), words, words.size());
    }

    /**
     * 合并重叠区间，从原文向前构造结果，避免在变化后的字符串中继续使用旧下标。
     * 每个被遮盖的 Unicode 码点输出一次 replacement；空字符串表示删除匹配区间。
     */
    private static String replace(String text, String replacement, List<SensitiveWordResult> words) {
        if (words.isEmpty())
            return text;

        StringBuilder output = new StringBuilder();
        int cursor = 0, start = words.get(0).getStart(), end = words.get(0).getEnd() + 1;

        for (int i = 1; i < words.size(); i++) {
            SensitiveWordResult word = words.get(i);

            if (word.getStart() <= end)
                end = Math.max(end, word.getEnd() + 1);
            else {
                output.append(text, cursor, start).append(replacement.repeat(text.codePointCount(start, end)));
                cursor = end;
                start = word.getStart();
                end = word.getEnd() + 1;
            }
        }

        output.append(text, cursor, start).append(replacement.repeat(text.codePointCount(start, end)));

        return output.append(text, end, text.length()).toString();
    }
}
