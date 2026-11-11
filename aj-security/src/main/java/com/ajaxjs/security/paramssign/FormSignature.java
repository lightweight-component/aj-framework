package com.ajaxjs.security.paramssign;

import com.ajaxjs.util.UrlCodec;
import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

@RequiredArgsConstructor
public class FormSignature {
    final TreeMap<String, String> data;

    final String keySecret;

    void calculateSignature() {
        String signature;
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            if ("Signature".equals(entry.getKey())) // 注意：签名计算时，Signature 参数本身不参与
                continue;

            sb.append("&").append(percentEncode(entry.getKey())).append("=").append(percentEncode(entry.getValue()));
        }

        if (!sb.isEmpty())// 移除第一个 &
            sb.deleteCharAt(0);

        String stringToSign = "POST&" + percentEncode("/") + "&" + percentEncode(sb.toString());
        String key = keySecret + "&"; // 阿里云要求在Secret后面加 '&'

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            signature = Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        data.put("Signature", signature);
    }

    static String percentEncode(String str) {
        return new UrlCodec(str).encodeQueryValue();
    }

    /**
     * 构建 application/x-www-form-urlencoded 格式的请求体
     */
    private static String buildFormData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (String key : params.keySet())
            sb.append("&").append(percentEncode(key)).append("=").append(percentEncode(params.get(key)));

        if (!sb.isEmpty())
            sb.deleteCharAt(0); // Remove leading &

        return sb.toString();
    }
}
