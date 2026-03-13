//package com.ajaxjs.security.encryptedbody;
//
//import com.ajaxjs.util.Base64Utils;
//import com.ajaxjs.util.cryptography.rsa.KeyMgr;
//import org.springframework.http.HttpInputMessage;
//import org.springframework.http.HttpOutputMessage;
//import org.springframework.http.converter.HttpMessageNotReadableException;
//import org.springframework.http.converter.HttpMessageNotWritableException;
//import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
//import tools.jackson.databind.json.JsonMapper;
//
//import java.io.IOException;
//import java.lang.reflect.Type;
//
///**
// * 加密解密转换器
// */
//public class EncryptedBodyConverter<T extends IResponseResult> extends JacksonJsonHttpMessageConverter {
//    public EncryptedBodyConverter(String publicKey, String privateKey, Class<T> responseResultType) {
//        super();
//        this.publicKey = publicKey;
//        this.privateKey = privateKey;
//        this.responseResultType = responseResultType;
//    }
//
//    private final Class<T> responseResultType;
//
//    private boolean isEnabled;
//
//    public void setEnabled(boolean isEnabled) {
//        this.isEnabled = isEnabled;
//    }
//
//    private final String publicKey;
//
//    private final String privateKey;
//
//    /**
//     * 使用私钥解密字符串
//     *
//     * @param encryptBody 经过 Base64 编码的加密字符串
//     * @param privateKey  私钥字符串，用于解密
//     * @return 解密后的字符串
//     */
//    static String decrypt(String encryptBody, String privateKey) {
//        byte[] data = new Base64Utils(encryptBody).decode();
//
//        return KeyMgr.privateKeyDecryptAsStr(data, privateKey);
//    }
//
//    /**
//     * 使用公钥加密字符串
//     * <p>
//     * 该方法采用RSA加密算法，使用给定的公钥对一段字符串进行加密
//     * 加密后的字节数组被转换为 Base64 编码的字符串，以便于传输和存储
//     *
//     * @param body      需要加密的原始字符串
//     * @param publicKey 用于加密的公钥字符串
//     * @return 加密后的 Base64 编码字符串
//     */
//    static String encrypt(String body, String publicKey) {
//        return KeyMgr.publicKeyEncryptAsBase64Str(body.getBytes(), publicKey);
//    }
//
//    /**
//     * 重写 read 方法以支持加密数据的读取
//     *
//     * @param type         数据类型，用于确定返回对象的类型
//     * @param contextClass 上下文类，未在本方法中使用
//     * @param inputMessage 包含加密数据的 HTTP 输入消息
//     * @return 根据类型参数反序列化后的对象实例
//     * @throws IOException                     如果读取或解析过程中发生 I/O 错误
//     * @throws HttpMessageNotReadableException 如果消息无法解析为对象实例
//     */
//    @Override
//    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
//        if (type instanceof Class<?>) {
//            Class<?> clz = (Class<?>) type;
//
//            if (isEnabled && clz.getAnnotation(EncryptedData.class) != null) {
//                JsonMapper jsonMapper  = getMapper();
//                DecodeDTO decodeDTO = jsonMapper.readValue(inputMessage.getBody(), DecodeDTO.class);
//                String encryptBody = decodeDTO.getData();
//
//                String decodeJson = decrypt(encryptBody, privateKey);
//                System.out.println(decodeJson);
//
//                return jsonMapper.readValue(decodeJson, clz);
//            }
//        }
//
//        return super.read(type, contextClass, inputMessage);
//    }
//
//    @Override
//    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws  HttpMessageNotWritableException {
//        if (!(type instanceof Class))
//            return;
//
//        Class<?> clz = (Class<?>) type;
//
//        if (isEnabled && responseResultType.isInstance(object) && clz.getAnnotation(EncryptedData.class) != null) {
//            T response = (T) object;
//            Object data = response.getData();
//            String json = getMapper().writeValueAsString(data);
//            String encryptBody = encrypt(json, publicKey);
//
//            response.setData(encryptBody);
//        }
//
//        super.writeInternal(object, type, outputMessage);
//    }
//}
