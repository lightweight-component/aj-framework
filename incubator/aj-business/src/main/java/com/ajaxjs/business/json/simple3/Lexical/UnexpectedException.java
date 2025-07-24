package com.ajaxjs.business.json.simple3.Lexical;

//RuntimeException（运行时异常）：由于程序错误导致的异常。
public class UnexpectedException extends RuntimeException {
    //在进行反序列化时，JVM会把传来的字节流中的serialVersionUID与本地相应实体（类）的serialVersionUID进行比较，
    // 如果相同就认为是一致的，可以进行反序列化，否则就会出现序列化版本不一致的异常。(InvalidClassException)
    private static final long serialVersionUID = 4468284382650630883L;

    private Integer charNum = null;

    private Integer lineNum = null;

    private Integer colNum = null;

    private String desc = null;

    private Throwable cause = null;

    public UnexpectedException() {
        super();
    }

    public UnexpectedException(Integer charNum, Integer lineNum, Integer colNum, String message, Throwable cause) {
        this.charNum = charNum;//cur
        this.colNum = colNum;//行号
        this.lineNum = lineNum;//开始的列号
        this.desc = message;
        this.cause = cause;
    }

    public UnexpectedException(Integer charNum, Integer lineNum, Integer colNum, String message) {
        this.charNum = charNum;
        this.colNum = colNum;
        this.lineNum = lineNum;
        this.desc = message;
    }

    public String getMessage() {
        return "[char:" + charNum + ",line:" + lineNum + ",column:" + colNum + "]" + desc + (cause == null ? "" : cause.toString());
    }

    public String toLocalString() {
        return getMessage();
    }
}


