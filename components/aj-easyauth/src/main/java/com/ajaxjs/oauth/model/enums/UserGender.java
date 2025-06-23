package com.ajaxjs.oauth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.ajaxjs.util.StrUtil;
import java.util.Arrays;
import java.util.List;

/**
 * 用户性别
 */
@Getter
@AllArgsConstructor
public enum UserGender {
    /**
     * MALE/FEMALE为正常值，通过{@link UserGender#getRealGender(String)}方法获取真实的性别
     * UNKNOWN为容错值，部分平台不会返回用户性别，为了方便统一，使用UNKNOWN标记所有未知或不可测的用户性别信息
     */
    MALE("1", "男"),
    FEMALE("0", "女"),
    UNKNOWN("-1", "未知");

    private final String code;

    private final String desc;

    final static List<String> MALE_LIST = Arrays.asList("m", "男", "1", "male");

    /**
     * 获取用户的实际性别，常规网站
     *
     * @param originalGender 用户第三方标注的原始性别
     * @return 用户性别
     */
    public static UserGender getRealGender(String originalGender) {
        if (null == originalGender || UNKNOWN.getCode().equals(originalGender))
            return UNKNOWN;

        if (MALE_LIST.contains(originalGender.toLowerCase()))
            return MALE;

        return FEMALE;
    }

    /**
     * 获取微信平台用户的实际性别，0表示未定义，1表示男性，2表示女性
     *
     * @param originalGender 用户第三方标注的原始性别
     * @return 用户性别
     */
    public static UserGender getWechatRealGender(String originalGender) {
        if (StrUtil.isEmptyText(originalGender) || "0".equals(originalGender))
            return UserGender.UNKNOWN;

        return getRealGender(originalGender);
    }
}
