package com.ajaxjs.generator;

/**
 * 银行卡类型枚举类
 */
public enum BankCardTypeEnum {
    /**
     * 借记卡/储蓄卡
     */
    DEBIT("借记卡/储蓄卡"),
    /**
     * 信用卡/贷记卡
     */
    CREDIT("信用卡/贷记卡");

    @SuppressWarnings("unused")
    private final String name;

    BankCardTypeEnum(String name) {
        this.name = name;
    }
}