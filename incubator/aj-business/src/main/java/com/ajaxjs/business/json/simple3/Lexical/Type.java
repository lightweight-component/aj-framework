package com.ajaxjs.business.json.simple3.Lexical;

/**
 * 保存LeptType类型信息以及相关静态变量
 */

public class Type {
    //JSON中数据的一般类型
    public static final int STR = 0;
    public static final int NUM = 1;
    public static final int DESC = 2;
    public static final int SPLIT = 3;
    public static final int ARRS = 4;
    public static final int OBJS = 5;
    public static final int ARRE = 6;
    public static final int OBJE = 7;
    public static final int FALSE = 8;
    public static final int TRUE = 9;
    public static final int NULL = 10;
    public static final int BGN = 11;
    public static final int EOF = 12;

    //保存类型个数
    public static final int Type_NUM = 13;

    //将类型转换为字符串的转换数组
    public static final String[] CHANGE_STRS = {"STR", "NUM", "DESC", "SPLIT", "ARRS", "OBJS", "ARRE", "OBJE", "FALSE", "TRUE", "NULL", "BGN", "EOF"};

    //将类型转换为字符串的转换数组
    public static final String[] CHANGE_LOCAL_STRS = {"字符串", "数字", ":", ",", "[", "{", "]", "}", "false", "true", "null", "开始", "结束"};

    //将类型转换为String，用于显示结果
    public static String changeTypeToStr(int type) {
        if (type < 0 || type >= Type_NUM) return "undefine";
        else return CHANGE_STRS[type];
    }

    //将类型转换为String，用于报错
    public static String changeTypeToLocalStr(int type) {
        if (type < 0 || type >= Type_NUM) return "undefine";
        else return CHANGE_LOCAL_STRS[type];
    }
}
