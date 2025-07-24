package com.ajaxjs.business.json.simple3.Grammar;


import com.ajaxjs.business.json.simple3.Lexical.JsonLex;
import com.ajaxjs.business.json.simple3.Lexical.LeptType;
import com.ajaxjs.business.json.simple3.Lexical.UnexpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class StatusMachine {
    private final JsonLex lex;
    private Integer status = null;
    Operator opt;

    public StatusMachine(String str) {
        if (str == null) throw new NullPointerException("语法解析构造函数不能传递null");
        lex = new JsonLex(str);
        opt = new Operator(lex);
    }

    public Object parse() {
        LeptType lt;
        status = Status.BGN;
        Integer oldStatus = status;

        while ((lt = lex.leptParseValue()) != LeptType.EOF) {
            if (lt == null)
                throw lex.generateUnexpectedException("发现JSON不能识别的LeptType: “" + lex.getCurChar() + "”");

            if (Objects.equals(status, Status.VAL) || Objects.equals(status, Status.EOF) || Objects.equals(status, Status.ERR))
                throw lex.generateUnexpectedException("当前状态【" + Status.castLocalStr(oldStatus) + "】,期待【结束】;却返回" + lt.toLocalString());

            oldStatus = status;
            status = Status.STM[oldStatus][lt.getType()];

            if (Objects.equals(status, Status.ERR))
                throw lex.generateUnexpectedException("当前状态【" + Status.castLocalStr(oldStatus) + "】，期待【" + (Status.ETS[oldStatus] == null ? "结束" : Status.ETS[oldStatus]) + "】;却返回" + lt.toLocalString());

            try {
                Method m = Status.LtInput[lt.getType()];

                if (m != null) {//输入Token操作
                    /*
                     * Object:invoke(Object obj,Method method,Object[] args)
                     * obj一般是指代理类
                     * method是被代理的方法
                     * args为该方法的参数数组
                     */
                    status = (Integer) m.invoke(opt, oldStatus, status, lt);
                }

                m = Status.LtGoal[status];

                if (m != null) //目标状态操作
                    status = (Integer) m.invoke(opt, oldStatus, status, lt);
            } catch (IllegalArgumentException e) {
                throw lex.generateUnexpectedException("【反射调用】传入非法参数", e);
            } catch (IllegalAccessException e) {
                throw lex.generateUnexpectedException("【反射调用】私有方法无法调用", e);
            } catch (InvocationTargetException e) {
                //---------------------------------------------????
                if (e.getTargetException() instanceof UnexpectedException)
                    throw (UnexpectedException) e.getTargetException();
                else throw lex.generateUnexpectedException("运行时异常", e);
            }
        }

        return opt.getCurValue();
    }

}
