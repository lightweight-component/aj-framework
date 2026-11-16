## 前言

敏感词过滤系统已经成为各大平台的必备功能。无论是社交平台的内容审核、电商系统的商品管理，还是游戏系统的聊天监控，都需要高效可靠的敏感词过滤机制来维护健康的内容生态。

传统的字符串查找方式在处理大量敏感词时性能急剧下降，而正则表达式在匹配复杂规则时更是捉襟见肘。

今天，介绍一种基于 DFA（有限状态自动机）算法的高效敏感词过滤方案，通过 Trie 树数据结构实现毫秒级响应，轻松应对敏感内容的实时过滤需求。

https://juejin.cn/post/7574281453708279814
___

## 为什么需要 DFA 算法？

#### 传统方案的性能瓶颈

在敏感词过滤的实际应用中，我们面临着诸多挑战。传统的实现方式往往存在以下问题：

❌ **暴力匹配的低效**

```
// 时间复杂度：O(n × m × k)
// 随着敏感词数量增加，性能急剧下降
for (String word : sensitiveWords) {
    if (text.contains(word)) {
        // 处理敏感词
    }
}
```

这种简单粗暴的方式在小规模应用中尚可接受，但当敏感词数量达到数千甚至数万时，处理一篇1000字的文章可能需要数秒时间，严重影响用户体验。

❌ **正则表达式的局限性** 虽然正则表达式提供了强大的模式匹配能力，但在敏感词过滤场景中却存在明显缺陷：

-   构建超大的正则表达式会导致编译时间过长
-   动态添加敏感词需要重新编译正则表达式
-   内存占用随敏感词数量线性增长
-   匹配效率在复杂规则下大幅下降

#### DFA 算法的优势

DFA 算法通过构建状态机模型，将敏感词匹配过程转化为状态转移，带来了质的飞跃：

✅ **线性时间复杂度**: O(n) - 只需遍历文本一次，不受敏感词数量影响 ✅ **空间共享优化**: 敏感词前缀共享存储，显著减少内存占用 ✅ **确定性匹配**: 无需回溯，避免正则表达式的回溯开销 ✅ **动态扩展友好**: 支持运行时添加、删除敏感词，无需重建整个数据结构

以实际场景为例，当敏感词库从1000个扩展到10000个时：

-   暴力匹配算法耗时增加约10倍
-   DFA算法耗时几乎保持不变

## DFA 算法原理解析

#### DFA 算法的数学本质

DFA（Deterministic Finite Automaton）是一种数学模型，它定义了一个五元组 M = (Q, Σ, δ, q₀, F)：

-   **Q**: 有限状态集合
-   **Σ**: 输入字母表
-   **δ**: 状态转移函数 δ: Q × Σ → Q
-   **q₀**: 初始状态
-   **F**: 接受状态集合

在敏感词过滤中，每个状态代表匹配到某个字符位置的状态转移路径。

#### Trie 树的可视化理解

Trie 树是 DFA 的直观实现，也称为字典树或前缀树。让我们通过一个具体例子来理解：

假设有以下敏感词：\["apple", "app", "application", "apply", "orange"\]

构建的 Trie 树结构如下：

```
root
├── a
│   └── p
│       └── p [结束]  ← "app"
│           └── l
│               ├── e [结束]  ← "apple"
│               ├── i
│               │   └── c
│               │       └── a
│               │           └── t
│               │               └── i
│               │                   └── o
│               │                       └── n [结束]  ← "application"
│               └── y [结束]  ← "apply"
└── o
    └── r
        └── a
            └── n
                └── g
                    └── e [结束]  ← "orange"
```

**Trie 树结构的关键特征**：

**1\. 前缀共享优化**：

-   "app" 作为 "apple"、"application"、"apply" 的共同前缀，只存储一次
-   从 "app" 节点分支出不同的后续字符路径

**2\. 状态转移路径**：

-   每个 `├──` 和 `└──` 表示一个字符状态转移
-   `[结束]` 标记表示 DFA 的接受状态（敏感词结束）

**3\. 空间效率**：

-   5个敏感词只需要存储 10 个字符节点（含重复字符）
-   如果单独存储需要 5 + 3 + 11 + 4 + 6 = 29 个字符

**4\. 查找效率**：

-   查找 "application" 只需要 11 次字符比较
-   查找 "app" 只需要 3 次字符比较且可以提前终止

**关键特征解释**

**1\. 前缀共享**: "app" 作为 "apple"、"application"、"apply" 的前缀，只在树中存储一次

**2\. 节点状态**: 每个节点代表 DFA 的一个状态

**3\. 边转移**: 每条边代表一个字符输入引起的状态转移

**4\. 接受状态**: 标记 \* 的节点表示敏感词的结束状态（接受状态）

例如，当检测文本 "I love apples" 时：

-   从根节点开始，遇到 'a' 转移到 a 节点
-   遇到 'p' 转移到 p 节点
-   遇到 'p' 转移到 p 节点
-   遇到 'l' 转移到 l 节点
-   遇到 'e' 转移到 e 节点（到达接受状态，发现敏感词 "apple"）
-   继续检测 's' 时状态转移失败，回到初始位置继续检测

#### 状态转移的实际过程

假设我们要在文本 "I like apples and apps" 中查找敏感词：

1.  从根节点开始，遇到 'a'，转移到 'a' 节点
2.  遇到 'p'，转移到 'p' 节点
3.  遇到 'p'，转移到 'p' 节点（此时检测到 "app" 是敏感词）
4.  遇到 'l'，转移到 'l' 节点
5.  遇到 'e'，转移到 'e' 节点（此时检测到 "apple" 是敏感词）

整个过程只需要一次线性遍历，无需重复扫描或回溯。

## 核心实现详解

#### 1\. Trie 节点结构设计

Trie 节点是整个 DFA 算法的基础，每个节点代表一个状态：

```
public class TrieNode {
    // 子节点映射：字符 -> Trie节点
    private Map<Character, TrieNode> children = new HashMap<>();

    // 是否为敏感词的结束节点
    private boolean isEnd = false;

    // 完整敏感词内容（便于输出）
    private String keyword;

    public TrieNode getChild(char c) {
        return children.get(c);
    }

    public TrieNode addChild(char c) {
        return children.computeIfAbsent(c, k -> new TrieNode());
    }

    public boolean hasChild(char c) {
        return children.containsKey(c);
    }

    // getters and setters...
}
```

#### 2\. DFA 过滤器核心实现

以下是最精简的 DFA 过滤器实现，包含了核心的匹配逻辑：

```
public class SensitiveWordFilter {
    private TrieNode root;
    private int minWordLength = 1;

    public SensitiveWordFilter(List<String> sensitiveWords) {
        this.root = buildTrie(sensitiveWords);
        this.minWordLength = sensitiveWords.stream()
            .mapToInt(String::length).min().orElse(1);
    }

    /**
     * 构建 Trie 树
     */
    private TrieNode buildTrie(List<String> words) {
        TrieNode root = new TrieNode();
        for (String word : words) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                node = node.addChild(c);
            }
            node.setEnd(true);
            node.setKeyword(word);
        }
        return root;
    }

    /**
     * 检查是否包含敏感词 - 核心 DFA 匹配算法
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.length() < minWordLength) {
            return false;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (dfaMatch(chars, i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * DFA 状态转移匹配
     */
    private boolean dfaMatch(char[] chars, int start) {
        TrieNode node = root;

        for (int i = start; i < chars.length; i++) {
            char c = chars[i];

            if (!node.hasChild(c)) {
                break; // 状态转移失败
            }

            node = node.getChild(c);

            if (node.isEnd()) {
                return true; // 到达接受状态
            }
        }
        return false;
    }

    /**
     * 查找并替换敏感词
     */
    public String filter(String text, String replacement) {
        List<SensitiveWordResult> words = findAllWords(text);

        // 从后往前替换，避免索引变化问题
        StringBuilder result = new StringBuilder(text);
        for (int i = words.size() - 1; i >= 0; i--) {
            SensitiveWordResult word = words.get(i);
            String stars = String.valueOf(replacement != null ? replacement : "*")
                .repeat(word.getEnd() - word.getStart() + 1);
            result.replace(word.getStart(), word.getEnd() + 1, stars);
        }
        return result.toString();
    }

    /**
     * 查找所有敏感词
     */
    public List<SensitiveWordResult> findAllWords(String text) {
        List<SensitiveWordResult> results = new ArrayList<>();

        if (text == null || text.length() < minWordLength) {
            return results;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            TrieNode node = root;
            int j = i;

            while (j < chars.length && node.hasChild(chars[j])) {
                node = node.getChild(chars[j]);
                j++;

                if (node.isEnd()) {
                    results.add(new SensitiveWordResult(
                        text.substring(i, j), i, j - 1));
                }
            }
        }
        return results;
    }
}
```

#### 3\. 敏感词结果封装

```
public class SensitiveWordResult {
    private String word;      // 敏感词内容
    private int start;        // 起始位置
    private int end;          // 结束位置

    public SensitiveWordResult(String word, int start, int end) {
        this.word = word;
        this.start = start;
        this.end = end;
    }

    // getters and toString...
}
```

## 实战应用场景

#### 即时通讯系统中的实时过滤

在高并发的聊天系统中，敏感词过滤需要满足低延迟、高吞吐的要求：

```
public class ChatMessageFilter {
    private SensitiveWordFilter wordFilter;

    // 异步处理敏感词检测
    private ExecutorService filterExecutor = Executors.newFixedThreadPool(10);

    public CompletableFuture<Message> filterMessageAsync(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            String content = message.getContent();

            if (wordFilter.containsSensitiveWord(content)) {
                // 实时替换
                String filtered = wordFilter.filter(content, "***");
                message.setContent(filtered);

                // 记录敏感词统计
                recordSensitiveWords(content);
            }

            return message;
        }, filterExecutor);
    }

    private void recordSensitiveWords(String content) {
        List<SensitiveWordResult> words = wordFilter.findAllWords(content);
        // 统计敏感词出现频率，用于优化词库
        updateWordFrequency(words);
    }
}
```

#### 内容审核系统的多级策略

不同类型的敏感词需要不同的处理策略：

```
public class ContentAuditor {
    private SensitiveWordFilter highRiskFilter;  // 高风险词
    private SensitiveWordFilter mediumRiskFilter; // 中风险词
    private SensitiveWordFilter lowRiskFilter;   // 低风险词

    public AuditResult auditContent(String content) {
        AuditResult result = new AuditResult();

        // 按风险级别检测
        List<SensitiveWordResult> highRiskWords = highRiskFilter.findAllWords(content);
        if (!highRiskWords.isEmpty()) {
            result.setStatus(AuditStatus.REJECT);
            result.setReason("包含高风险敏感词");
            return result;
        }

        List<SensitiveWordResult> mediumRiskWords = mediumRiskFilter.findAllWords(content);
        if (!mediumRiskWords.isEmpty()) {
            result.setStatus(AuditStatus.MANUAL_REVIEW);
            result.setReason("包含中风险敏感词，需要人工审核");
            return result;
        }

        List<SensitiveWordResult> lowRiskWords = lowRiskFilter.findAllWords(content);
        if (!lowRiskWords.isEmpty()) {
            // 低风险词汇直接过滤
            String filtered = lowRiskFilter.filter(content, "***");
            result.setFilteredContent(filtered);
            result.setStatus(AuditStatus.PASS_WITH_FILTER);
        } else {
            result.setStatus(AuditStatus.PASS);
        }

        return result;
    }
}
```

#### 动态词库管理

实际项目中，敏感词库需要动态更新：

```
@Service
public class SensitiveWordManager {
    private volatile SensitiveWordFilter filter;
    private ScheduledExecutorService updateExecutor =
        Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        loadWords();
        // 定期更新词库
        updateExecutor.scheduleAtFixedRate(this::loadWords, 0, 1, TimeUnit.HOURS);
    }

    public void loadWords() {
        try {
            // 从数据库或配置中心加载最新词库
            List<String> words = fetchLatestWords();
            SensitiveWordFilter newFilter = new SensitiveWordFilter(words);

            this.filter = newFilter;

            log.info("敏感词库更新完成，当前词数：{}", words.size());
        } catch (Exception e) {
            log.error("词库更新失败", e);
        }
    }

    public boolean containsSensitiveWord(String text) {
        return filter != null && filter.containsSensitiveWord(text);
    }

    public String filterText(String text) {
        return filter != null ? filter.filter(text, "***") : text;
    }
}
```

## 高级优化方案

对于不同规模的敏感词过滤需求，基础的 DFA 算法可能需要进一步优化。以下是几种常用的高级方案：

#### 方案1：双数组 Trie（Double-Array Trie）

**核心思想**：将 Trie 树压缩为两个数组，减小内存使用。

```
// 双数组结构示意
int[] base = new int[size];  // 状态转移基础值
int[] check = new int[size]; // 状态转移检查值

// 状态转移：next_state = base[current_state] + char_code
// if check[next_state] == current_state: 转移成功
```

**适用场景**：词库规模较大

**优点**：内存占用减少 50%-80%

**缺点**：构建复杂度增加，动态更新困难

**应用**：搜索引擎、大型社交平台的离线词库

#### 方案2：AC 自动机（Aho-Corasick）

**核心思想**：在 Trie 基础上增加失败指针，实现一次遍历匹配多个模式。

```
public class AhoCorasickAutomaton {
    private Node root = new Node();

    // Trie 节点结构
    static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node fail;           // 失败指针
        List<String> output = new ArrayList<>();  // 输出模式
    }

    // 构建自动机
    public void build(List<String> patterns) {
        // 1. 构建 Trie 树
        for (String pattern : patterns) {
            Node node = root;
            for (char c : pattern.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new Node());
            }
            node.output.add(pattern);
        }

        // 2. 添加失败指针
        Queue<Node> queue = new LinkedList<>();

        // 初始化根节点的子节点
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.add(child);
        }

        // BFS 构建失败指针
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();

                // 找到失败指针
                Node failNode = current.fail;
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }

                child.fail = (failNode == null) ? root : failNode.children.get(c);
                child.output.addAll(child.fail.output);

                queue.add(child);
            }
        }
    }

    // 搜索匹配
    public List<MatchResult> search(String text) {
        List<MatchResult> results = new ArrayList<>();
        Node node = root;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 失败指针跳转
            while (node != null && !node.children.containsKey(c)) {
                node = node.fail;
            }

            node = (node == null) ? root : node.children.get(c);

            // 输出匹配结果
            for (String pattern : node.output) {
                results.add(new MatchResult(pattern, i - pattern.length() + 1, i));
            }
        }

        return results;
    }

    // 匹配结果
    static class MatchResult {
        String pattern;
        int start;
        int end;

        public MatchResult(String pattern, int start, int end) {
            this.pattern = pattern;
            this.start = start;
            this.end = end;
        }
    }
}
```

**适用场景**：多模式匹配、日志分析

**优点**：可同时匹配多个敏感词，无需多次遍历

**缺点**：空间复杂度较高，实现相对复杂

**应用**：网络安全、内容审核系统

#### 方案3：分片 + 布隆过滤器预筛选

**核心思想**：通过分片降低单机压力，用布隆过滤器快速过滤明显不包含敏感词的文本。

```
// 分片处理示例
public class ShardedFilter {
// 模拟分片
    private List<SensitiveWordFilter> shards;
    private BloomFilter<String> preFilter;

    public boolean containsSensitive(String text) {
        // 布隆过滤器预筛选
        if (!preFilter.mightContain(text)) {
            return false; // 肯定不包含敏感词
        }

        // 分片精确匹配
        int shardIndex = text.hashCode() % shards.size();
        return shards.get(shardIndex).containsSensitiveWord(text);
    }
}
```

**适用场景**：高并发、大规模文本处理

**优点**：支持水平扩展，预处理可过滤大量无效请求

**缺点**：存在误判概率，系统复杂度增加

**应用**：弹幕系统、即时通讯、微服务架构

## 总结

DFA 算法通过 Trie 树结构，为敏感词过滤提供了一个兼具效率和准确性的解决方案。在实际应用中，需要根据具体的业务场景选择合适的实现策略。无论是追求极致性能的聊天系统，还是注重准确率的内容审核平台，DFA 算法都能提供坚实的基础支撑。

## 仓库

```
https://github.com/yuboon/java-examples/tree/master/springboot-dfa
```