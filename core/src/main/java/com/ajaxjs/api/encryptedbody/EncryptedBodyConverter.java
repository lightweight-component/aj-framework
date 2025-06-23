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

/**
 * 加密解密转换器
 */
public class EncryptedBodyConverter extends MappingJackson2HttpMessageConverter {
    public EncryptedBodyConverter(String publicKey, String privateKey) {
        super();
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    private boolean isEnabled;

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    private final String publicKey;

    private final String privateKey;

    /**
     * 使用私钥解密字符串
     *
     * @param encryptBody 经过 Base64 编码的加密字符串
     * @param privateKey  私钥字符串，用于解密
     * @return 解密后的字符串
     */
    static String decrypt(String encryptBody, String privateKey) {
        byte[] data = EncodeTools.base64Decode(encryptBody);

        return new String(RsaCrypto.decryptByPrivateKey(data, privateKey));
    }

    /**
     * 使用公钥加密字符串
     * <p>
     * 该方法采用RSA加密算法，使用给定的公钥对一段字符串进行加密
     * 加密后的字节数组被转换为 Base64 编码的字符串，以便于传输和存储
     *
     * @param body      需要加密的原始字符串
     * @param publicKey 用于加密的公钥字符串
     * @return 加密后的 Base64 编码字符串
     */
    static String encrypt(String body, String publicKey) {
        byte[] encWord = RsaCrypto.encryptByPublicKey(body.getBytes(), publicKey);
        return EncodeTools.base64EncodeToString(encWord);
    }

    /**
     * 重写 read 方法以支持加密数据的读取
     *
     * @param type         数据类型，用于确定返回对象的类型
     * @param contextClass 上下文类，未在本方法中使用
     * @param inputMessage 包含加密数据的 HTTP 输入消息
     * @return 根据类型参数反序列化后的对象实例
     * @throws IOException                     如果读取或解析过程中发生 I/O 错误
     * @throws HttpMessageNotReadableException 如果消息无法解析为对象实例
     */
    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        Class<?> clz = (Class<?>) type;

        if (isEnabled && clz.getAnnotation(EncryptedData.class) != null) {
            ObjectMapper objectMapper = getObjectMapper();
            DecodeDTO decodeDTO = objectMapper.readValue(inputMessage.getBody(), DecodeDTO.class);
            String encryptBody = decodeDTO.getData();

            String decodeJson = decrypt(encryptBody, privateKey);
            System.out.println(decodeJson);

//            User user = new User();
//            user.setAge(1);
//            user.setName("tom");
//
//            String json = objectMapper.writeValueAsString(user);

            Object result = objectMapper.readValue(decodeJson, clz);
            return result;
        }

        return super.read(type, contextClass, inputMessage);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if(!(type instanceof Class))
            return;

        Class<?> clz = (Class<?>) type;

        if (isEnabled && object instanceof ResponseResultWrapper && clz.getAnnotation(EncryptedData.class) != null) {
            ResponseResultWrapper response = (ResponseResultWrapper) object;
            Object data = response.getData();
            String json = getObjectMapper().writeValueAsString(data);
            String encryptBody = encrypt(json, publicKey);

            response.setData(encryptBody);
        }

        super.writeInternal(object, type, outputMessage);
    }
}
