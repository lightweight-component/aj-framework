package com.ajaxjs.business.json.simple3.Grammar;


import com.ajaxjs.business.json.simple3.Lexical.JsonLex;
import com.ajaxjs.business.json.simple3.Lexical.LeptType;
import com.ajaxjs.business.json.simple3.Lexical.Type;

import java.util.*;

public class Operator {
    private final JsonLex lex;
    private final Stack<Integer> statusStack = new Stack<>();//保存所有状态值
    private Object curValue = null;//
    private final Stack<Object> keyStack = new Stack<>();//保存对象建
    private final Stack<Object> objStack = new Stack<>();//保存对象值
    private Object curObj = null;

    public Operator(JsonLex lex) {
        this.lex = lex;
    }

    public Object getCurValue() {
        return curValue;
    }

    public Object getCurObj() {
        return curObj;
    }

    public Integer objs(Integer from, Integer to, LeptType input) {
        if (!Objects.equals(from, Status.BGN)) statusStack.push(from);

        curObj = new HashMap<>();
        objStack.push(curObj);

        return to;
    }

    public Integer arrs(Integer from, Integer to, LeptType input) {
        if (!Objects.equals(from, Status.BGN)) statusStack.push(from);
        curObj = new ArrayList<>();
        objStack.push(curObj);

        return to;
    }

    private Object getRealValue(LeptType input) {
        Object value = null;

        try {
            value = input.getRealValue();
        } catch (RuntimeException e) {
            System.out.println(lex.generateUnexpectedException("字符串转换错误", e).getMessage());
        }

        return value;
    }

    public Integer arrav(Integer from, Integer to, LeptType input) {
        curValue = getRealValue(input);
        ((List<Object>) curObj).add(curValue);

        return to;

    }

    public Integer objak(Integer from, Integer to, LeptType input) {
        keyStack.push(getRealValue(input));

        return to;
    }

    public Integer objav(Integer from, Integer to, LeptType input) {
        curValue = getRealValue(input);
        ((Map<Object, Object>) curObj).put(keyStack.pop(), curValue);

        return to;
    }

    public Integer val(Integer from, Integer to, LeptType input) {
        switch (input.getType()) {
            case Type.ARRE:
            case Type.OBJE:
                curObj = objStack.pop();
                curValue = curObj;
                break;
            case Type.TRUE:
            case Type.FALSE:
            case Type.NULL:
            case Type.STR:
            case Type.NUM:
                curValue = getRealValue(input);
                break;
        }

        if (statusStack.isEmpty()) return Status.EOF;
        else {
            Integer s = statusStack.pop();

            if (Objects.equals(s, Status.ARRBV)) {
                curObj = objStack.peek();
                ((List<Object>) curObj).add(curValue);
                s = Status.ARRAV;
            } else if (s == Status.OBJBV) {
                curObj = objStack.peek();
                ((Map<Object, Object>) curObj).put(keyStack.pop(), curValue);
                s = Status.OBJAV;
            }

            return s;
        }
    }
}
