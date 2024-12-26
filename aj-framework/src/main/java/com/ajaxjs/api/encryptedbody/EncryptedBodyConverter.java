package com.ajaxjs.api.encryptedbody;

import com.ajaxjs.springboot.ResponseResultWrapper;
import com.ajaxjs.util.EncodeTools;
import com.ajaxjs.util.cryptography.RsaCrypto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

public class EncryptedBodyConverter extends MappingJackson2HttpMessageConverter {
    public EncryptedBodyConverter(String publicKey, String privateKey) {
        super();
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    private final String publicKey;

    private final String privateKey;

    static String decrypt(String encryptBody, String privateKey) {
        byte[] data = EncodeTools.base64Decode(encryptBody);

        return new String(RsaCrypto.decryptByPrivateKey(data, privateKey));
    }

    static String encrypt(String body, String publicKey) {
        byte[] encWord = RsaCrypto.encryptByPublicKey(body.getBytes(), publicKey);
        return EncodeTools.base64EncodeToString(encWord);
    }

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

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        Class<?> clz = (Class<?>) type;

        if (object instanceof ResponseResultWrapper && clz.getAnnotation(EncryptedData.class) != null) {
            ResponseResultWrapper response = (ResponseResultWrapper) object;
            Object data = response.getData();
            String json = getObjectMapper().writeValueAsString(data);
            String encryptBody = encrypt(json, publicKey);

            response.setData(encryptBody);
        }

        super.writeInternal(object, type, outputMessage);
    }
}
