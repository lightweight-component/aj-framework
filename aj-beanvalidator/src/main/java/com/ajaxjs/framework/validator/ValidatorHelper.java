package com.ajaxjs.framework.validator;

import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 提供常见字段格式的校验辅助方法。
 */
public class ValidatorHelper {
    /**
     * 正则表达式：验证用户名
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,20}$";

    /**
     * 正则表达式：验证密码 (?!^\\d+$)(?!^[a-zA-Z]+$)(?!^[_#@]+$).{8,}
     * 1、密码必须由数字、字符、特殊字符三种中的两种组成
     * 2、密码长度不能少于8个字符
     */
    public static final String REGEX_PASSWORD = "^(?!\\d+$)(?![a-zA-Z]+$)(?![^\\da-zA-Z]+$)(?=[^\\s]+$).{8,16}$";

    /**
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^1[3-9]\\d{9}$";

    /**
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*@([a-zA-Z0-9]+(-[a-zA-Z0-9]+)?\\.)+[a-zA-Z]{2,}$";

    /**
     * 正则表达式：验证汉字
     */
    public static final String REGEX_CHINESE = "^[\u4e00-\u9fff]+$";

    /**
     * 正则表达式：验证身份证
     */
    public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)|(^\\d{17}(\\d|X|x)$)";

    /**
     * 正则表达式：验证URL
     */
    public static final String REGEX_URL = "^https?://[\\w-]+(?:\\.[\\w-]+)+(?:/[\\w\\-./?%&=]*)?$";

    /**
     * 正则表达式：验证IP地址
     */
    public static final String REGEX_IP_ADDR = "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";

    /**
     * 18 位身份证号码校验码计算使用的权重。
     */
    private static final int[] IdWeights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2, 1};

    /**
     * 校验用户名
     *
     * @param username 待校验的用户名
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isUsername(String username) {
        return StringUtils.hasText(username) && Pattern.matches(REGEX_USERNAME, username);
    }

    /**
     * 校验密码
     *
     * @param password 待校验的密码
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isPassword(String password) {
        return StringUtils.hasText(password) && Pattern.matches(REGEX_PASSWORD, password);
    }

    /**
     * 校验手机号
     *
     * @param mobile 待校验的手机号码
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isMobile(String mobile) {
        if (!StringUtils.hasText(mobile))
            return false;

        return Pattern.matches(REGEX_MOBILE, mobile);
    }

    /**
     * 校验邮箱
     *
     * @param email 待校验的邮箱地址
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isEmail(String email) {
        return StringUtils.hasText(email) && Pattern.matches(REGEX_EMAIL, email);
    }

    /**
     * 校验汉字
     *
     * @param chinese 待校验的中文文本
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isChinese(String chinese) {
        return StringUtils.hasText(chinese) && Pattern.matches(REGEX_CHINESE, chinese);
    }

    /**
     * 校验身份证
     *
     * @param idCard 待校验的身份证号码
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isIDCard(String idCard) {
        if (!StringUtils.hasText(idCard))
            return false;

        if (!Pattern.matches(REGEX_ID_CARD, idCard))
            return false;

        if (idCard.length() == 15)
            return isValidDate("19" + idCard.substring(6, 12));

        if (!isValidDate(idCard.substring(6, 14)))
            return false;

        int sum = 0;

        for (int i = 0; i < idCard.length(); i++) {
            String bit = String.valueOf(idCard.charAt(i));

            if (bit.equalsIgnoreCase("x"))
                sum += IdWeights[i] * 10;
            else
                sum += IdWeights[i] * Integer.parseInt(bit);
        }

        return sum % 11 == 1;
    }

    /**
     * 校验 URL
     *
     * @param url 待校验的 URL
     * @return 校验通过返回 true，否则返回 false
     */
    public static boolean isUrl(String url) {
        if (!StringUtils.hasText(url) || !Pattern.matches(REGEX_URL, url))
            return false;

        try {
            URI uri = new URI(url);
            return uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 校验给定的字符串是否符合 IP 地址的格式
     *
     * @param ipAddress 待校验的 IP 地址字符串
     * @return 如果字符串符合 IP 地址的格式，则返回 true；否则返回 false
     */
    public static boolean isIpAddress(String ipAddress) {
        return StringUtils.hasText(ipAddress) && Pattern.matches(REGEX_IP_ADDR, ipAddress);
    }

    /**
     * 判断字符串是否为有效的基本 ISO 日期。
     *
     * @param value 待校验的日期字符串，格式为 {@code yyyyMMdd}
     * @return 有效日期时为 {@code true}
     */
    static boolean isValidDate(String value) {
        try {
            LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
