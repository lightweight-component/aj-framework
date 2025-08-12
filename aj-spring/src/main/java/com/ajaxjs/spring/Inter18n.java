package com.ajaxjs.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Locale;

/**
 * 国际化 i18n
 */
@Component
public class Inter18n {
    /**
     * 从请求中提取 Accept-Language
     *
     * @param request 请求对象
     * @return 本地语言
     */
    public static Locale extractLocale(HttpServletRequest request) {
        Enumeration<Locale> locales = request.getLocales();

        if (locales.hasMoreElements())
            return locales.nextElement(); // 返回第一个（最优先）

        return Locale.getDefault(); // 默认
    }

    public static Locale extractLocale() {
        return extractLocale(DiContextUtil.getRequest());
    }

    @Autowired
    private Environment environment;

    private static final String DEFAULT_LOCALE = "en_US";

    public String getText(String key, String index, String defaultText, String... args) {
        Locale locale = extractLocale();
        String localeStr;

        if (locale == null)
            localeStr = DEFAULT_LOCALE;
        else {
            localeStr = locale.toString();

            if (!StringUtils.hasText(localeStr))
                localeStr = DEFAULT_LOCALE;
        }

        key += "." + localeStr + "." + index;
        String msg = environment.getProperty(key);
        msg = StringUtils.hasText(msg) ? msg : defaultText;

        if (!ObjectUtils.isEmpty(args))
            msg = String.format(msg, args);

        return msg;
    }
}
