package com.ajaxjs.security.paramssign;

import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.UrlEncode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Data
@Slf4j
public class ParamsSign {
    @Value("${security.ParamsSign.secret}")
    String secretKey;

    String nonce;

    Map<String, String> paramMap;

    Consumer<String> setNonceUsed;

    /**
     * 校验 nonce 是否已用过
     */
    Predicate<String> containsUsedNonce;

    @Value("${security.ParamsSign.expire: 600}")
    long expireSeconds = 60 * 10;

    public static final String SIGN_PARAMS = "sign";

    @SuppressWarnings("unchecked")
    public String sign(Object params, String accessSecret) {
        Map<String, String> paramMap = new HashMap<>();
        Map<String, Object> map;

        if (params instanceof Map)
            map = (Map<String, Object>) params;
        else
            map = JsonUtil.pojo2map(params);

        map.forEach((key, value) -> paramMap.put(key, value.toString()));

        nonce = RandomTools.generateRandomString(6);
        map.put("nonce", nonce);
        map.put("timestamp", getTimestamp());

        String sortString = sort(paramMap);
        sortString = new UrlEncode(sortString).encodeQuery();
        String sign = HashHelper.getHmacMD5(sortString, accessSecret).hashAsBase64();
        sign = new UrlEncode(sign).encodeQuery(); // needs?

        paramMap.put(SIGN_PARAMS, sign);
        this.paramMap = paramMap;

        return sign;
    }

    public String sign(Object params) {
        return sign(params, secretKey);
    }

    public boolean verify(Map<String, String> params) {
        String clientSign = params.get(SIGN_PARAMS);
        String nonce = params.get("nonce");
        String timestampStr = params.get("timestamp");

        if (nonce == null || timestampStr == null || clientSign == null)
            return false;

        if (containsUsedNonce.test(nonce)) // 校验 nonce 是否已用过（如在 Redis 里查重防重放，这里用 Set 模拟）
            return false;

        // 校验签名
        // 1. 提取并移除 sign 字段
        Map<String, String> paramsForSign = new HashMap<>(params);
        paramsForSign.remove(SIGN_PARAMS); // 必须移除

        // 2. 生成本地签名
        String serverSign = sign(paramsForSign, secretKey);

        // 3. 比对
        if (serverSign.equalsIgnoreCase(clientSign)) {
            // 校验 timestamp
            if (isTimeout(timestampStr, expireSeconds * 1000)) {
                log.warn("Invalid sign, timeout");

                return false;
            } else {
                setNonceUsed.accept(nonce); // 标记 nonce 已用
                return true;
            }
        } else {
            log.warn("Invalid sign");

            return false;
        }
    }

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    /**
     * 请求的时间戳。按照 ISO8601 标准表示，并需要使用 UTC 时间，格式为 yyyy-MM-ddTHH:mm:ssZ。
     */
    static String getTimestamp() {
        return DATETIME_FORMATTER.format(Instant.now());
    }

    /**
     * 根据参数 Key 排序（顺序）
     */
    private static String sort(Map<String, String> paras) {
        TreeMap<String, String> sortParas = new TreeMap<>(paras);
        Iterator<String> it = sortParas.keySet().iterator();
        StringBuilder sb = new StringBuilder();

        while (it.hasNext()) {
            String key = it.next();
            String value = new UrlEncode(paras.get(key)).encodeQuery();
            key = new UrlEncode(key).encodeQuery();

            sb.append("&").append(key).append("=").append(value);
        }

        return sb.substring(1);// 去除第一个多余的&符号
    }

    // 解析 ISO8601 字符串为 long 类型的毫秒时间戳
    static long parseIso8601ToMillis(String timestampStr) {
        return Instant.from(DATETIME_FORMATTER.parse(timestampStr)).toEpochMilli();
    }

    /**
     * 判断给定的时间戳是否已经超时
     *
     * @param timestampStr 时间戳字符串，应符合ISO 8601格式
     * @param expireMillis 允许的最大时间差，单位为毫秒
     * @return 如果时间戳超过当前时间的差值大于expireMillis，则返回true，否则返回false
     */
    static boolean isTimeout(String timestampStr, long expireMillis) {
        long requestTime = parseIso8601ToMillis(timestampStr);
        long now = Instant.now().toEpochMilli();

        return Math.abs(now - requestTime) > expireMillis;
    }

}
