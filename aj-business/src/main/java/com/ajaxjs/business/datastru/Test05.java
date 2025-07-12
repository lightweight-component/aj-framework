package com.ajaxjs.business.datastru;

import java.util.Stack;

/**
 * 括号匹配检查
 * 题目：一个表达式字符串中包含了‘（’，')'，'['，']'，'{'，'}'六种括号，判断这些括号是否匹配。
 * 解决这个问题可以使用一种叫“栈”的数据结构，它是一种FILO（先进后出）的结构，插入（push，入栈）和删除（pop，出栈）元素都是在栈顶进行。代码如下所示：
 * <a href="https://blog.csdn.net/jackfrued/article/details/17221573">...</a>
 */
public class Test05 {
    public static boolean isValid(String exp) {
        String left = "([{";
        String right = ")]}";
        Stack<Character> s = new Stack<>();

        for (int i = 0, len = exp.length(); i < len; i++) {
            char ch = exp.charAt(i);

            if (left.indexOf(ch) != -1)
                s.push(ch);

            else if (right.indexOf(ch) != -1) {
                if (!s.isEmpty()) {
                    char temp = s.pop();

                    if (ch != right.charAt(left.indexOf(temp)))
                        return false;
                } else return false;
            }
        }

        return s.isEmpty();
    }

    public static void main(String[] args) {
        System.out.println(isValid("([()]{})"));
        System.out.println(isValid("((){}"));
        System.out.println(isValid("[{)()]"));
    }
}