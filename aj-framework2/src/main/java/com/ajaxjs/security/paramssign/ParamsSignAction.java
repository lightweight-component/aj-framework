package com.ajaxjs.security.paramssign;

import com.ajaxjs.security.InterceptorAction;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Simply check the HTTP Referer
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "security.params-sign")
public class ParamsSignAction extends InterceptorAction<ParamsSignCheck> {
    String secretKey;
    Long expireSeconds;

    @Override
    public boolean action(ParamsSignCheck annotation, HttpServletRequest req) {
        if (!(req instanceof ContentCachingRequestWrapper))
            throw new UnsupportedOperationException("Please add the filter to support ContentCaching");

        ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) req;


        try {
            ServletInputStream inputStream = wrapper.getInputStream();
            toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        byte[] buf = wrapper.getContentAsByteArray();
        String body;

        try {
            body = new String(buf, wrapper.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

//        if (StrUtil.isEmptyText(body))
//            throw new IllegalArgumentException("No params at all. Pls POST Raw body(In JSON).");
//
//        Map<String, Object> map = JsonUtil.json2map(body);
//
//        if (!map.containsKey(ParamsSign.SIGN_PARAMS))
//            throw new IllegalArgumentException("Lack of param: " + ParamsSign.SIGN_PARAMS);
//
//        ParamsSignLocal paramsSign = new ParamsSignLocal();
//        paramsSign.setSecretKey(secretKey);
//
//        if (expireSeconds != null)
//            paramsSign.setExpireSeconds(expireSeconds);
//
//        Map<String, String> paramMap = new HashMap<>();
//        map.forEach((key, value) -> paramMap.put(key, value.toString()));
//
//        if (!paramsSign.verify(paramMap))
//            throw new SecurityException("Invalid Request Params.");

        return true;
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = input.read(data, 0, data.length)) != -1)
            buffer.write(data, 0, nRead);

        return buffer.toByteArray();
    }
}
