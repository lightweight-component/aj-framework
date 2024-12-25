package com.ajaxjs.api.encryptedbody;

import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.cryptography.RsaCrypto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class EncryptedBodyConverter extends MappingJackson2HttpMessageConverter {
    public EncryptedBodyConverter(String privateKey) {
        super();
        this.privateKey = privateKey;
    }

    private String privateKey;

    static String decrypt(String encryptBody, String privateKey) {
        byte[] data = EncodeTools.base64Decode(encryptBody);

        return new String(RsaCrypto.decryptByPrivateKey(data, privateKey));
    }

//    String privateKey = "";

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Class<?> clz = (Class<?>) type;

        if (clz.getAnnotation(EncryptedData.class) != null) {
            ObjectMapper objectMapper = getObjectMapper();
            DecodeDTO decodeDTO = objectMapper.readValue(inputMessage.getBody(), DecodeDTO.class);
            String encryptBody = decodeDTO.getData();

            String decodeJson = decrypt(encryptBody, privateKey);
            System.out.println(decodeJson);

            User user = new User();
            user.setAge(1);
            user.setName("tom");

            String json = objectMapper.writeValueAsString(user);

            Object result = objectMapper.readValue(json, clz);
            return result;
        }

        return super.read(type, contextClass, inputMessage);
    }
}
