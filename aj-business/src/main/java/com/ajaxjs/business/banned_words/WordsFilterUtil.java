package com.ajaxjs.business.banned_words;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;


/**
 * 多叉树关键词过滤(DFA算法)
 * 高效Java实现敏感词过滤算法工具包 <a href="https://blog.csdn.net/ranjio_z/article/details/75446147">...</a>
 * <a href="https://blog.csdn.net/qq_40838030/article/details/82625910">...</a>
 * <a href="https://blog.csdn.net/qq_40838030/article/details/82625910">...</a>
 *
 * @author Alex.Zhangrj
 * 北京师范大学计算机系2000级 张人杰
 * zhrenjie04@126.com
 * 1307776259@qq.com
 * 3.0版本，增加正向词库，完美跳过“江阴毛纺厂”问题
 */
public class WordsFilterUtil {
    private static final Node tree;
    private static final Node positiveTree;

    /**
     * 用于判断单字符是否为标点符号的模式，单字符判断基本不影响效率
     */
    private static final Pattern p;

    static {
        tree = new Node();

        try (InputStream is = getResource("/sensitive-words.dict");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Properties prop = new Properties();
            prop.load(reader);
            Enumeration<String> en = (Enumeration<String>) prop.propertyNames();

            while (en.hasMoreElements()) {
                String word = en.nextElement();
                insertWord(tree, word, Double.parseDouble(prop.getProperty(word)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
         * 用于判断单字符是否为标点符号的模式，单字符判断基本不影响效率
         */
        p = Pattern.compile("[\\pP\\pZ\\pS\\pM\\pC]", Pattern.CASE_INSENSITIVE);
        positiveTree = new Node();

        try (InputStream is = getResource("/positive-words.dict");
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Properties prop = new Properties();
            prop.load(reader);
            Enumeration<String> en = (Enumeration<String>) prop.propertyNames();

            while (en.hasMoreElements()) {
                String word = en.nextElement();
                insertWord(positiveTree, word, Double.parseDouble(prop.getProperty(word)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static InputStream getResource(String path) {
        InputStream is = WordsFilterUtil.class.getResourceAsStream(path);

        if (is == null)
            is = WordsFilterUtil.class.getClassLoader().getResourceAsStream(path);

        return is;
    }

    private static void insertWord(Node tree, String word, double level) {
        word = word.toLowerCase();
        Node node = tree;
        for (int i = 0; i < word.length(); i++)
            node = node.addChar(word.charAt(i));

        node.setEnd(true);
        node.setLevel(level);
        node.setWord(word);
    }

    private static boolean isPunctuationChar(String c) {
        return !p.matcher(c).find();
    }

    private static PunctuationOrHtmlFilteredResult filterPunctuation(String originalString) {
        StringBuilder filteredString = new StringBuilder();
        ArrayList<Integer> charOffsets = new ArrayList<>();

        for (int i = 0; i < originalString.length(); i++) {
            String c = String.valueOf(originalString.charAt(i));

            if (isPunctuationChar(c)) {
                filteredString.append(c);
                charOffsets.add(i);
            }
        }

        PunctuationOrHtmlFilteredResult result = new PunctuationOrHtmlFilteredResult();
        result.setOriginalString(originalString);
        result.setFilteredString(filteredString);
        result.setCharOffsets(charOffsets);

        return result;
    }

    private static PunctuationOrHtmlFilteredResult filterPunctuationAndHtml(String originalString) {
        StringBuilder filteredString = new StringBuilder();
        ArrayList<Integer> charOffsets = new ArrayList<>();

        for (int i = 0, k; i < originalString.length(); i++) {
            String c = String.valueOf(originalString.charAt(i));

            if (originalString.charAt(i) == '<') {
                for (k = i + 1; k < originalString.length(); k++) {
                    if (originalString.charAt(k) == '<') {
                        k = i;
                        break;
                    }

                    if (originalString.charAt(k) == '>')
                        break;
                }

                i = k;
            } else {
                if (isPunctuationChar(c)) {
                    filteredString.append(c);
                    charOffsets.add(i);
                }
            }
        }

        PunctuationOrHtmlFilteredResult result = new PunctuationOrHtmlFilteredResult();
        result.setOriginalString(originalString);
        result.setFilteredString(filteredString);
        result.setCharOffsets(charOffsets);

        return result;
    }

    private static FilteredResult filter(PunctuationOrHtmlFilteredResult pohResult, char replacement) {
        StringBuilder sentence = pohResult.getFilteredString();
        StringBuilder sb = new StringBuilder(pohResult.getOriginalString());
        ArrayList<Integer> charOffsets = pohResult.getCharOffsets();
        List<Word> positiveWords = simpleFilter2DictFindWords(sentence, positiveTree);
        List<Word> sensitiveWords = simpleFilter2DictFindWords(sentence, tree);
        Iterator<Word> sIt = sensitiveWords.iterator();

        while (sIt.hasNext()) {
            Word sWord = sIt.next();
            int i;

            // 跳过前置单词
            for (i = 0; i < positiveWords.size(); i++) {
                Word pWord = positiveWords.get(i);
                if (pWord.getEndPos() >= sWord.getStartPos())
                    break;

            }

            for (; i < positiveWords.size(); i++) {
                Word pWord = positiveWords.get(i);

                if (pWord.getStartPos() > sWord.getEndPos())
                    break;

                if (pWord.getStartPos() < sWord.getStartPos() && pWord.getEndPos() >= sWord.getStartPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                } else if (pWord.getStartPos() <= sWord.getEndPos() && pWord.getEndPos() > sWord.getEndPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                } else if (pWord.getStartPos() <= sWord.getStartPos() && pWord.getEndPos() >= sWord.getEndPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                }
            }
        }

        double maxLevel = 0.0;
        StringBuilder badWords = new StringBuilder();

        for (Word word : sensitiveWords) {
            badWords.append(word.getWord()).append(",");

            if (word.getLevel() > maxLevel)
                maxLevel = word.getLevel();
        }

        StringBuilder goodWords = new StringBuilder();
        for (Word word : positiveWords)
            goodWords.append(word.getWord()).append(",");

        for (Word word : sensitiveWords) {
            for (int i = 0; i < word.getPos().length; i++)
                sb.replace(charOffsets.get(word.getPos()[i]), charOffsets.get(word.getPos()[i]) + 1, String.valueOf(replacement));
        }

        FilteredResult result = new FilteredResult();
        result.setBadWords(badWords.toString());
        result.setGoodWords(goodWords.toString());
        result.setFilteredContent(sb.toString());
        result.setOriginalContent(pohResult.getOriginalString());
        result.setLevel(maxLevel);
        result.setHasSensitiveWords(!sensitiveWords.isEmpty());

        return result;
    }

    /**
     * 简单句子过滤
     * 不处理特殊符号，不处理html，简单句子的过滤
     * 不能过滤中间带特殊符号的关键词，如：黄_色_漫_画
     *
     * @param sentence    需要过滤的句子
     * @param replacement 关键词替换的字符
     * @return 结果对象：FilteredResult
     */
    public static FilteredResult simpleFilter(String sentence, char replacement) {
        final StringBuilder sb = new StringBuilder(sentence);
        List<Word> positiveWords = simpleFilter2DictFindWords(sb, positiveTree);
        List<Word> sensitiveWords = simpleFilter2DictFindWords(sb, tree);
        Iterator<Word> sIt = sensitiveWords.iterator();

        while (sIt.hasNext()) {
            Word sWord = sIt.next();
            int i;

            // 跳过前置单词
            for (i = 0; i < positiveWords.size(); i++) {
                Word pWord = positiveWords.get(i);

                if (pWord.getEndPos() >= sWord.getStartPos())
                    break;
            }

            for (; i < positiveWords.size(); i++) {
                Word pWord = positiveWords.get(i);
                if (pWord.getStartPos() > sWord.getEndPos())
                    break;

                if (pWord.getStartPos() < sWord.getStartPos() && pWord.getEndPos() >= sWord.getStartPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                } else if (pWord.getStartPos() <= sWord.getEndPos() && pWord.getEndPos() > sWord.getEndPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                } else if (pWord.getStartPos() <= sWord.getStartPos() && pWord.getEndPos() >= sWord.getEndPos() && pWord.getLevel() > sWord.getLevel()) {
                    sIt.remove();
                    break;
                }
            }
        }

        double maxLevel = 0.0;
        StringBuilder badWords = new StringBuilder();

        for (Word word : sensitiveWords) {
            badWords.append(word.getWord()).append(",");

            if (word.getLevel() > maxLevel)
                maxLevel = word.getLevel();
        }

        StringBuilder goodWords = new StringBuilder();

        for (Word word : positiveWords)
            goodWords.append(word.getWord()).append(",");

        for (Word word : sensitiveWords) {
            for (int i = 0; i < word.getPos().length; i++)
                sb.replace(word.getPos()[i], word.getPos()[i] + 1, String.valueOf(replacement));
        }

        FilteredResult result = new FilteredResult();
        result.setBadWords(badWords.toString());
        result.setGoodWords(goodWords.toString());
        result.setFilteredContent(sb.toString());
        result.setOriginalContent(sentence);
        result.setLevel(maxLevel);
        result.setHasSensitiveWords(!sensitiveWords.isEmpty());

        return result;
    }

    private static List<Word> simpleFilter2DictFindWords(StringBuilder sentence, Node dictTree) {
        List<Word> foundWords = new LinkedList<>();
        Node node;
        int start, end;

        for (int i = 0; i < sentence.length(); i++) {
            start = i;
            end = i;
            node = dictTree;
            Node lastFoundNode = null;

            for (int j = i; j < sentence.length(); j++) {
                node = node.findChar(toLowerCase(sentence.charAt(j)));

                if (node == null)
                    break;

                if (node.isEnd()) {
                    end = j;
                    lastFoundNode = node;
                }
            }

            if (end > start) {
                int[] pos = new int[end - start + 1];
                for (int j = 0; j < pos.length; j++)
                    pos[j] = start + j;

                Word word = new Word();
                word.setPos(pos);
                word.setStartPos(start);
                word.setEndPos(end);
                word.setLevel(lastFoundNode.getLevel());
                word.setWord(lastFoundNode.getWord());
                foundWords.add(word);
            }
        }

        return foundWords;
    }

    /**
     * 纯文本过滤，不处理 html 标签，直接将去除所有特殊符号后的字符串拼接后进行过滤，
     * 可能会去除html标签内的文字，比如：如果有关键字“fuck font”，过滤fuck<font>a</font>后的结果为****<****>a</font>
     *
     * @param originalString 原始需过滤的串
     * @param replacement    替换的符号
     * @return 结果
     */
    public static FilteredResult filterTextWithPunctuation(String originalString, char replacement) {
        return filter(filterPunctuation(originalString), replacement);
    }

    /**
     * html过滤，处理html标签，不处理html标签内的文字，略有不足，会跳过<>标签内的所有内容，比如：如果有关键字“fuck”，过滤<a title="fuck">fuck</a>后的结果为<a title="fuck">****</a>
     *
     * @param originalString 原始需过滤的串
     * @param replacement    替换的符号
     * @return 结果
     */
    public static FilteredResult filterHtml(String originalString, char replacement) {
        return filter(filterPunctuationAndHtml(originalString), replacement);
    }

    public static char toLowerCase(char c) {
        if (c >= 'A' && c <= 'Z')
            return (char) (c + 32);
        else
            return c;
    }
}
