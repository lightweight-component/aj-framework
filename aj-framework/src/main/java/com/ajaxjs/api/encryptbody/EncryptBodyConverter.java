package com.ajaxjs.api.encryptbody;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.cryptography.RsaCrypto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class EncryptBodyConverter extends MappingJackson2HttpMessageConverter {
    public EncryptBodyConverter() {
        super();
    }

    static String decode(String encryptBody, String privateKey) {
        byte[] data = EncodeTools.base64Decode(encryptBody);

        return new String(RsaCrypto.decryptByPrivateKey(data, privateKey));
    }

    String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIwEbyJboyyef3848da7iWrdkVGPhc/m5N/dkEQtEv5EwzOhif06vhcX7S/X7EbWHLzGglkEXvzYcLGPPY6m9eVw0JRudXccd4M/HWOvwBhukSEx5gjftGTtDMh/WCS1zmlaqicyyB2Hy1EXwVdY6zT+gzMnN92oVeD6HERqDN7PAgMBAAECgYB+t8Sco7KZvxhEW5UDcdZ8EOpjljDUZ3Lb5/mYufJmjHTdVWU8+NzwqYbPterwcPaxIjb1uS7+RiQ3jht370K6daZwuii7ipuS8KbghnJodlE6oY4FthFo5l2BJbdnIL7ahiMK2fWN9fj4I4r+7yaenn8Vyq1y94hVoEblJcweAQJBAOkxO86p0/DJ4KdyPVjjz0gtzc5YZSqUjuvKydgblJ7YW6oKiYcqAmnIBQ/Q1oky7ttQ0BvfzET+QxgnZs0CyDECQQCZtj8AOrpcIGvGuV7o6Bl2PzQ7QTq4i+rCCqV8G22p0qWY4Lvcpby7yB86IamDHCRqMuu88taZBWFZpmRKt1b/AkEAnWKlQu5MJQ+bmGf6D6xxkf2YEdSHMx3g+DN912WGAU91TmaeO6OWzV46TFpe8Wp2237HdoitjUMudXbiYyulgQJAV+A71kq83gxe8UZqvyZp4BM0LigVhQnglTx27SPVJwBZUbQxUmyiIIzBZX93JJCDNP+Vi/kcvHC5/gHFVn3ISwJAbMK933DY4CW+dLWdu5DdkVz3FUeHLg7OmCIYAVqQ89eMrkmXzhR9fqMYF7J6BpwukrB0rGY1EooRdPwQ40/tqg==";

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        HttpHeaders headers = inputMessage.getHeaders();
        if (!headers.isEmpty() && headers.containsKey("dodecode")) {
            List<String> dodecode = headers.get("dodecode");

            if ("true".equals(dodecode.get(0))) {
                ObjectMapper objectMapper = getObjectMapper();
                DecodeDTO decodeDTO = objectMapper.readValue(inputMessage.getBody(), DecodeDTO.class);
                String encryptBody = decodeDTO.getData();

                String decodeJson = decode(encryptBody, privateKey);
                System.out.println(decodeJson);

                User user = new User();
                user.setAge(1);
                user.setName("tom");

                String json = objectMapper.writeValueAsString(user);

                Object result = objectMapper.readValue(json, (Class<?>) type);
                return result;
            }
        }

        return super.read(type, contextClass, inputMessage);
    }
}
