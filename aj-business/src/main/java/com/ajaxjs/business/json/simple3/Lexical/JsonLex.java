package com.ajaxjs.business.json.simple3.Lexical;


import java.util.Stack;

/**
 * Json词法分析器
 */
public class JsonLex {

    // 当前行号
    private int lineNum;
    // 用于记录每一行的起始位置
    private final Stack<Integer> colMarks;
    // 用于报错的行游标
    private int startLine;
    // 用于报错的列游标
    private int startCol;
    // 当前字符游标
    private int cur;
    // 保存当前要解析的字符串
    private final String str;
    // 保存当前要解析的字符串的长度
    private final int len;

    //构造函数
    public JsonLex(String s) {
        if (s == null)
            //空指针引起异常
            throw new NullPointerException("词法解析构造函数不能穿空字符串");

        this.lineNum = 0;//当前行号
        this.colMarks = new Stack<>();
        this.colMarks.push(0);//记录每一行的起始位置
        this.startLine = 0;//报错的行游标
        this.startCol = 0;//报错的列游标
        this.cur = -1;//当前游标
        this.str = s;
        this.len = str.length();
    }

    //获取下一个字符
    private char nextChar() {
        if (cur >= this.len - 1) return 0;

        cur++;
        char ch = str.charAt(cur);

        if (ch == '\n') {
            lineNum++;
            colMarks.push(cur);// 用于记录每一行的起始位置
        }

        return ch;
    }

    //生成意外错误，报错
    public UnexpectedException generateUnexpectedException(String str) {
        return new UnexpectedException(cur, startLine, startCol, str);
    }

    public UnexpectedException generateUnexpectedException(String str, Throwable e) {
        return new UnexpectedException(cur, startLine, startCol, str, e);
    }

    private void checkEnd() {
        if (cur >= len - 1) throw generateUnexpectedException("未预期的结束，字符串未结束");
    }

    private String getStrValue(char ch) {
        char tmp;
        int start = cur;

        while ((tmp = nextChar()) != 0) if (tmp == ch) return str.substring(start + 1, cur);

        checkEnd();

        return null;
    }

    private boolean isDecimal(char ch) {
        return isdigit(ch) || (ch == '.');
    }

    private int revertChar() {
        if (cur <= 0) return cur = -1;

        int recur = cur--;

        if (str.charAt(recur) == '\n') {
            lineNum--;
            colMarks.pop();
        }

        return recur;
    }

    //获取当前字符串
    public char getCurChar() {
        if (cur >= len - 1) return 0;

        return str.charAt(cur);
    }

    private boolean isdigit(char ch) {
        return (ch >= '0' && ch <= '9');
    }

    private boolean isdigit_1to9(char ch) {
        return (ch >= '1' && ch <= '9');
    }

    private String getNumValue(char ch) {
        // revertChar();
        int start = cur;
        if (ch == '-') ch = nextChar();

        if (ch == '0') ch = nextChar();
        else {
            if (!isdigit_1to9(ch)) throw generateUnexpectedException("输入的JSON文本不合法");
            while (isdigit(ch)) ch = nextChar();
        }

        if (ch == '.') {
            ch = nextChar();
            if (!isdigit(ch)) throw generateUnexpectedException("输入的JSON文本不合法");
            while (isdigit(ch)) ch = nextChar();
        }

        if (ch == 'e' || ch == 'E') {
            ch = nextChar();
            if (ch == '+' || ch == '-') ch = nextChar();
            if (!isdigit(ch)) throw generateUnexpectedException("输入的JSON文本不合法");
            while (isdigit(ch)) ch = nextChar();
        }

        checkEnd();
        int end = revertChar();

        return str.substring(start, end);
    }

    private boolean isLetterUnderline(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_';//'_'是什么鬼
    }

    private LeptType getDefLeptType() {
        int start = cur;
        char tmp;

        while ((tmp = nextChar()) != 0) {
            if (!isLetterUnderline(tmp)) {
                String s = str.substring(start, revertChar());

                switch (s) {
                    case "true":
                        return LeptType.TRUE;
                    case "false":
                        return LeptType.FALSE;
                    case "null":
                        return LeptType.NULL;
                    default:
                        throw generateUnexpectedException("输入的JSON文本不合法");
                }
            }
        }

        checkEnd();

        return null;
    }

    private boolean isSpace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    }

    public LeptType leptParseValue() {
        if (lineNum == 0) {
            lineNum++;
            return LeptType.BGN;
        }

        char ch;

        while ((ch = nextChar()) != 0) {
            startLine = this.lineNum;
            startCol = getColNum();
            switch (ch) {
                case '[':
                    return LeptType.ARRS;
                case ']':
                    return LeptType.ARRE;
                case '{':
                    return LeptType.OBJS;
                case '}':
                    return LeptType.OBJE;
                case ',':
                    return LeptType.SPLIT;
                case ':':
                    return LeptType.DESC;
                case 't':
                case 'f':
                case 'n':
                    return getDefLeptType();
                case '"':
                    return new LeptType(Type.STR, getStrValue(ch));
                default:
                    if (isSpace(ch)) continue;
                    if (isdigit(ch) || ch == '-') return new LeptType(Type.NUM, getNumValue(ch));
            }
        }

        if (ch == 0) return LeptType.EOF;

        return null;
    }

    public int getLineNum() {
        return lineNum;
    }

    public int getColNum() {
        return cur - colMarks.peek();
    }//Stack的peek方法是返回栈顶的元素但不移除它

    public int getCur() {
        return cur;
    }

    public String getStr() {
        return str;
    }

    public int getLen() {
        return len;
    }
}
