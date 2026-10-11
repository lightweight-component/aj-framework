package com.ajaxjs.framework.holiday;


import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 日期类型，工作日对应结果为 0, 休息日对应结果为 1, 节假日对应的结果为 2；
 */
@Getter
@RequiredArgsConstructor
public enum DayType {
    /**
     * 工作日
     */
    WORKING_DAY(0),

    /**
     * 休息日
     */
    REST_DAY(1),

    /**
     * 节假日
     */
    HOLIDAY(2);

    @JsonValue
    private final int type;

    /**
     * 将 type 转换成枚举
     *
     * @param type type
     * @return DaysType
     */
    public static DayType from(int type) {
        return switch (type) {
            case 0 -> WORKING_DAY;
            case 1 -> REST_DAY;
            case 2 -> HOLIDAY;
            default -> throw new IllegalArgumentException("未知的 DaysType:" + type);
        };
    }

}
