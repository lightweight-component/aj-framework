package com.ajaxjs.business.json.simple3.Lexical;

/**
 * 保存token
 */

public class LeptType {
    public static final LeptType DESC = new LeptType(Type.DESC);
    public static final LeptType SPLIT = new LeptType(Type.SPLIT);
    public static final LeptType ARRS = new LeptType(Type.ARRS);
    public static final LeptType OBJS = new LeptType(Type.OBJS);
    public static final LeptType ARRE = new LeptType(Type.ARRE);
    public static final LeptType OBJE = new LeptType(Type.OBJE);
    public static final LeptType FALSE = new LeptType(Type.FALSE);
    public static final LeptType TRUE = new LeptType(Type.TRUE);
    public static final LeptType NULL = new LeptType(Type.NULL);
    public static final LeptType BGN = new LeptType(Type.BGN);
    public static final LeptType EOF = new LeptType(Type.EOF);

    // 从Type类中定义的类型
    private final Integer type;
    // 该Type的值
    private String value;

    public LeptType(Integer type) {
        this.type = type;
    }

    public LeptType(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    private String unescape(String value) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (ch == '\\') {
                if (i < value.length() - 1) i++;
                ch = value.charAt(i);

                switch (ch) {
                    case '"':
                        sb.append(ch);
                        break;
                    case '\\':
                        sb.append(ch);
                        break;
                    case '/':
                        sb.append(ch);
                        break;
                    case 'b':
                        sb.append('\b');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'u':
                        String hex = value.substring(i + 1, i + 5);

                        sb.append((char) Integer.parseInt(hex, 16));
                        i += 4;
                        break;
                    default:
                        throw new RuntimeException("“\\”后面期待“\"\\/bfnrtu”中的字符，结果得到“" + ch + "”");
                }
            } else sb.append(ch);
        }

        return sb.toString();
    }

    //因为所有类型Object都包含
    public Object getRealValue() {
        Object realValue = null;

        switch (this.getType()) {
            case Type.TRUE:
                realValue = true;
                break;
            case Type.FALSE:
                realValue = false;
                break;
            case Type.NULL:
                realValue = null;
                break;
            case Type.NUM:
                realValue = Double.parseDouble(value);
                break;
            case Type.STR:
                realValue = unescape(value);
                break;
        }

        return realValue;
    }

    public String toString() {
        if (this.type > 1) //出字符串和数字类型外
            return "[" + Type.changeTypeToStr(this.type) + "]";
        else return "[" + Type.changeTypeToStr(this.type) + ":" + this.value + "]";
    }

    public String toLocalString() {
        if (this.type > 1) return "“" + Type.changeTypeToLocalStr(this.type) + "”";
        else return "“" + Type.changeTypeToLocalStr(this.type) + ":" + this.value + "”";
    }


}
