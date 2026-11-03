/**
 * DFA 敏感词过滤系统
 * 基于 DFA (Deterministic Finite Automaton) 算法和 Trie 树数据结构实现的敏感词过滤示例。
 * - **高效算法**: 基于 Trie 匹配，最坏扫描开销约为 O(n × L)，其中 L 为最长词的长度
 * - **前缀共享**: 使用 Trie 树优化内存使用
 * - **RESTful API**: 标准化的 API 接口
 */
package com.ajaxjs.security.dfa;