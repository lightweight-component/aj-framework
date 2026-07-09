package com.ajaxjs.framework.i18n;

import com.ajaxjs.spring.DiContextUtil;
import jakarta.servlet.http.HttpServletRequest;

public class Internationalization {
    public static final String LANGUAGE_HEADER = "language";

    /**
     * 获取当前请求的语言设置。
     *
     * <p>该方法从HTTP请求头中获取语言标识，若请求上下文不存在或语言头为空，
     * 则默认返回中文（ZH）。</p>
     *
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>从 Spring 上下文获取 HttpServletRequest 对象</li>
     *   <li>若请求对象为空（如JUnit测试等非Web上下文），返回默认语言ZH</li>
     *   <li>从请求头中获取语言标识（{@code LANGUAGE_HEADER}）</li>
     *   <li>若语言头为空，返回默认语言ZH</li>
     *   <li>将语言标识转换为大写后，通过{@link Language#valueOf(String)}获取对应的枚举值</li>
     * </ol>
     *
     * @return 当前请求的语言枚举值，默认为{@link Language#ZH}
     * @throws IllegalArgumentException 如果语言头值无法匹配任何{@link Language}枚举常量
     * @see DiContextUtil#getRequest()
     * @see Language
     */
    public static Language getLanguage() {
        HttpServletRequest request = DiContextUtil.getRequest();

        if (request == null) // might be called from a non-web context, such as junit test
            return Language.ZH;

        String language = request.getHeader(LANGUAGE_HEADER);

        if (language == null)
            return Language.ZH;

        return Language.valueOf(language.toUpperCase());
    }
}
