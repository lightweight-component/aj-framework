package com.ajaxjs.framework.validator;

import org.springframework.util.StringUtils;

/**
 * 内置业务校验注解与其校验实现的映射。
 */
public enum ValidatorEnum {
    /** 身份证号码校验。 */
    IdCard {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isIDCard(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 邮箱地址校验。 */
    UserMail {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isEmail(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 非空白文本校验。 */
    NotBlank {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !StringUtils.hasText(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 非空值校验。 */
    NotNull {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null)
                throw new ValidatorException(errorMsg);
        }
    },
    /** 中国大陆手机号码校验。 */
    MobileNo {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isMobile(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 用户名格式校验。 */
    Username {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isUsername(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 密码强度校验。 */
    Password {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isPassword(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** 中文文本校验。 */
    Chinese {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isChinese(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** IPv4 地址校验。 */
    Ipv4 {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isIpAddress(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    },
    /** HTTP 或 HTTPS 地址校验。 */
    HttpUrl {
        @Override
        public void validated(Object value, String errorMsg) {
            if (value == null || !ValidatorHelper.isUrl(value.toString()))
                throw new ValidatorException(errorMsg);
        }
    };

    /**
     * 根据注解简单名称查找内置校验器。
     *
     * @param annotationName 注解简单名称
     * @return 对应校验器；不存在时为 {@code null}
     */
    public static ValidatorEnum getInstance(String annotationName) {
        ValidatorEnum[] values = ValidatorEnum.values();

        for (ValidatorEnum validatorEnum : values) {
            if (validatorEnum.name().equals(annotationName))
                return validatorEnum;
        }

        return null;
    }

    /**
     * 校验指定值。
     *
     * @param value    待校验的值
     * @param errorMsg 校验失败提示语
     * @throws ValidatorException 校验失败时抛出
     */
    public abstract void validated(Object value, String errorMsg);
}
