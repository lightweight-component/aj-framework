package com.ajaxjs.oauth.utils;

import com.ajaxjs.util.StrUtil;
import com.xkcoding.http.util.MapUtil;
import com.xkcoding.http.util.StringUtil;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构造URL
 */
@Setter
public class UrlBuilder {
    private final Map<String, String> params = new LinkedHashMap<>(7);
    private String baseUrl;

    private UrlBuilder() {
    }

    /**
     * @param baseUrl 基础路径
     * @return the new {@code UrlBuilder}
     */
    public static UrlBuilder fromBaseUrl(String baseUrl) {
        UrlBuilder builder = new UrlBuilder();
        builder.setBaseUrl(baseUrl);

        return builder;
    }

    /**
     * 如果给定字符串{@code str}中不包含{@code appendStr}，则在{@code str}后追加{@code appendStr}；
     * 如果已包含{@code appendStr}，则在{@code str}后追加{@code otherwise}
     *
     * @param str       给定的字符串
     * @param appendStr 需要追加的内容
     * @param otherwise 当{@code appendStr}不满足时追加到{@code str}后的内容
     * @return 追加后的字符串
     */
    public static String appendIfNotContain(String str, String appendStr, String otherwise) {
        if (StrUtil.isEmptyText(str) || StrUtil.isEmptyText(appendStr))
            return str;

        if (str.contains(appendStr))
            return str.concat(otherwise);

        return str.concat(appendStr);
    }

    /**
     * 只读的参数Map
     *
     * @return unmodifiable Map
     */
    public Map<String, Object> getReadOnlyParams() {
        return Collections.unmodifiableMap(params);
    }

    /**
     * 添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     * @return this UrlBuilder
     */
    public UrlBuilder queryParam(String key, Object value) {
        if (StringUtil.isEmpty(key))
            throw new RuntimeException("参数名不能为空");

        String valueAsString = (value != null ? value.toString() : null);
        params.put(key, valueAsString);

        return this;
    }

    /**
     * 构造url
     *
     * @return url
     */
    public String build() {
        return this.build(false);
    }

    /**
     * 构造url
     *
     * @param encode 转码
     * @return url
     */
    public String build(boolean encode) {
        if (MapUtil.isEmpty(params))
            return baseUrl;

        String baseUrl = appendIfNotContain(this.baseUrl, "?", "&");
        String paramString = MapUtil.parseMapToString(params, encode);

        return baseUrl + paramString;
    }
}
