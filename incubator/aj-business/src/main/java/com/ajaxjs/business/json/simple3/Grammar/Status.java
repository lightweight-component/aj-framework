package com.ajaxjs.business.json.simple3.Grammar;


import com.ajaxjs.business.json.simple3.Lexical.Type;

import java.lang.reflect.Method;

public class Status {
    //开始解析状态
    public static final Integer BGN = 0;
    //数组值前态
    public static final Integer ARRBV = 1;
    //数组值后态
    public static final Integer ARRAV = 2;
    //对象键前态
    public static final Integer OBJBK = 3;
    //对象键后态
    public static final Integer OBJAK = 4;
    //对象值前态
    public static final Integer OBJBV = 5;
    //对象值后态
    public static final Integer OBJAV = 6;
    //结果态
    public static final Integer VAL = 7;
    //结束态
    public static final Integer EOF = 8;
    //错误态
    public static final Integer ERR = 9;

    //状态机的状态转换矩阵
    public static final Integer[][] STM = {
            /*INPUT——STR NUM DESC SPLIT ARRS OBJS ARRE OBJE FALSE TRUE NULL BGN*/
            /*BGN*/  {VAL, VAL, ERR, ERR, ARRBV, OBJBK, ERR, ERR, VAL, VAL, VAL, BGN},
            /*ARRBV*/{ARRAV, ARRAV, ERR, ERR, ARRBV, OBJBK, VAL, ERR, ARRAV, ARRAV, ARRAV, ERR},
            /*ARRAV*/{ERR, ERR, ERR, ARRBV, ERR, ERR, VAL, ERR, ERR, ERR, ERR, ERR},
            /*OBJBK*/{OBJAK, ERR, ERR, ERR, ERR, ERR, ERR, VAL, ERR, ERR, ERR, ERR},//JSON对象的键只能是JSON字符串，值可以任意
            /*OBJAK*/{ERR, ERR, OBJBV, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR, ERR},
            /*OBJBV*/{OBJAV, OBJAV, ERR, ERR, ARRBV, OBJBK, ERR, ERR, OBJAV, OBJAV, OBJAV, ERR},
            /*OBJAV*/{ERR, ERR, ERR, OBJBK, ERR, ERR, ERR, VAL, ERR, ERR, ERR, ERR},
            /*VAL*/{},//没有后续状态
            /*EOF*/{},//没有后续状态
            /*ERR*/{}//没有后续状态
    };

    //LeptType输入操作列表:当输入为这个类型的LeptType时，我要执行一个什么操作
    /*INPUT —— STR NUM DESC SPLIT ARRS OBJS ARRE OBJE FALSE TRUE NIL BGN*/
    public static final Method[] LtInput = {null, null, null, null, OPT.ARRS, OPT.OBJS, null, null, null, null, null, null //主要是利用动态代理来调用Operator类中的arrs()和objs()方法，分配数组和图
    };

    //目标状态转换操作列表:如果转换为这个状态的话，我会执行什么操作。
    /*TO:BGN ARRBV ARRAV OBJBK OBJAK OBJBV OBJAV VAL EOF ERR*/
    public static final Method[] LtGoal = {null, null, OPT.ARRAV, null, OPT.OBJAK, null, OPT.OBJAV, OPT.VAL, null, null};


    //期望LeptType描述列表
    /*FROM:BGN ARRBV ARRAV OBJBK OBJAK OBJBV OBJAV VAL EOF ERR*/
    public static final String[] ETS = {getExpectStr(BGN), getExpectStr(ARRBV), getExpectStr(ARRAV), getExpectStr(OBJBK), getExpectStr(OBJAK), getExpectStr(OBJBV), getExpectStr(OBJAV), Type.changeTypeToLocalStr(Type.EOF), Type.changeTypeToLocalStr(Type.EOF), Type.changeTypeToLocalStr(Type.EOF)};

    //状态描述列表
    /*BGN ARRBV ARRAV OBJBK OBJAK OBJBV OBJAV VAL EOF ERR*/
    public static final String[] STS = {"解析开始", "数组待值", "数组得值", "对象待键", "对象得键", "对象待值", "对象得值", "得最终值", "解析结束", "异常错误"};

    //将状态数值转换为状态描述
    public static String castLocalStr(Integer s) {
        return STS[s];
    }

    //获取期望Token描述字符串
    public static String getExpectStr(Integer old) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < STM[old].length; i++) {
            Integer s = STM[old][i];

            if (s != null) sb.append(Type.changeTypeToLocalStr(s)).append('|');
        }

        return sb.length() == 0 ? null : sb.deleteCharAt(sb.length() - 1).toString();//deleteCharAt（int  a）只有一个参数，删除索引为a的字符
    }
}
