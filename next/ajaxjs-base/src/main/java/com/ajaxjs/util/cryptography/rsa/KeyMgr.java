package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.StringBytes;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.Cryptography;
import com.ajaxjs.util.io.DataWriter;
import com.ajaxjs.util.io.FileHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 密钥管理
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class KeyMgr implements Constant {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The key size, required. Like 1024, 2048, 4096.
     */
    private final int keySize;

    /**
     * The result of generating a key pair
     */
    private KeyPair keyPair;

    /**
     * Get a pair of keys: public key and private key
     *
     * @return Key pair object
     */
    public KeyPair generateKeyPair() {
        if (keySize == 1024 || keySize == 2048 || keySize == 4096)
            try {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithmName);
                generator.initialize(keySize);
                keyPair = generator.generateKeyPair();

                return keyPair;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
            }
        else
            throw new IllegalArgumentException("Invalid key size: " + keySize);
    }

    /**
     * 返回公钥的 byte[]
     *
     * @return 私钥的 byte[]
     */
    public byte[] getPublicKeyBytes() {
        return keyPair.getPublic().getEncoded();
    }

    /**
     * 返回私钥的 byte[]
     *
     * @return 私钥的 byte[]
     */
    public byte[] getPrivateKeyBytes() {
        return keyPair.getPrivate().getEncoded();
    }

    /**
     * 返回公钥的 Base64 编码
     *
     * @return 私钥的 Base64 编码
     */
    public String getPublicKeyStr() {
        return new Base64Utils(getPublicKeyBytes()).encodeAsString();
    }

    public String getPublicToPem() {
        return publicKeyToPem(getPublicKeyStr());
    }

    /**
     * 返回私钥的 Base64 编码
     *
     * @return 私钥的 Base64 编码
     */
    public String getPrivateKeyStr() {
        return new Base64Utils(getPrivateKeyBytes()).encodeAsString();
    }

    public String getPrivateToPem() {
        return privateKeyToPem(getPrivateKeyStr());
    }

    /* ------------------------- Restore Key ------------------------ */

    /**
     * 还原公钥/私钥
     *
     * @param isPublic 是否公钥，反之私钥
     * @param key      公钥或私钥的字符串表示，应该为 Base64 编码的字符串
     * @return 还原后的公钥或私钥对象，如果还原失败则返回 null
     */
    public static Key restoreKey(boolean isPublic, String key) {
        // auto removes the pem
        if (isPublic)
            key = key.replaceAll("-----\\w+ PUBLIC KEY-----", CommonConstant.EMPTY_STRING);
        else
            key = key.replaceAll("-----\\w+ PRIVATE KEY-----", CommonConstant.EMPTY_STRING);

        key = key.replaceAll("\\s", CommonConstant.EMPTY_STRING);

        byte[] bytes = new Base64Utils(key).decode();

        try {
            KeyFactory f = KeyFactory.getInstance(RSA);

            return isPublic ? f.generatePublic(new X509EncodedKeySpec(bytes)) : f.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Invalid Key. " + key, e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + RSA, e);
        }
    }

    public static PrivateKey restorePrivateKey(String key) {
        Key _key = restoreKey(false, key);

        return (PrivateKey) _key;
    }

    /* ------------------------- PEM ------------------------ */

    /**
     * 将私钥转换为 PEM 格式字符串
     *
     * @param privateKey 待转换的私钥对象
     * @return PEM 格式的私钥字符串
     */
    @Deprecated
    public static String privateKeyToPem(PrivateKey privateKey) {
        String encoded = new Base64Utils(privateKey.getEncoded()).encodeAsString();

        return privateKeyToPem(encoded);
    }

    /**
     * 将私钥转换为 PEM 格式字符串
     *
     * @param encoded 待转换的私钥对象
     * @return PEM 格式的私钥字符串
     */
    public static String privateKeyToPem(String encoded) {
        return "-----BEGIN PRIVATE KEY-----\n" +
                Base64Utils.formatBase64String(encoded) +
                "\n-----END PRIVATE KEY-----";
    }

    /**
     * 将公钥转换为 PEM 格式字符串
     *
     * @param publicKey 公钥对象
     * @return PEM 格式的公钥字符串
     */
    @Deprecated
    public static String publicKeyToPem(PublicKey publicKey) {
        String encoded = new Base64Utils(publicKey.getEncoded()).encodeAsString();

        return publicKeyToPem(encoded);
    }

    /**
     * 将公钥转换为 PEM 格式字符串
     *
     * @param encoded 公钥
     * @return PEM 格式的公钥字符串
     */
    public static String publicKeyToPem(String encoded) {
        return "-----BEGIN PUBLIC KEY-----\n" +
                Base64Utils.formatBase64String(encoded) +
                "\n-----END PUBLIC KEY-----";
    }

    /* ------------------------- encrypt/decrypt ------------------------ */

    /**
     * 处理公钥
     *
     * @param isEncrypt 是否加密(true)，反之为解密（false）
     * @param data      需要加密或解密的数据
     * @param key       公钥或私钥的字符串表示
     * @return 加密或解密后的字节数据
     */
    private static byte[] action(boolean isEncrypt, boolean isPublic, byte[] data, String key) {
        int mode = isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;

        Cryptography cryptography = new Cryptography(RSA, mode);
        cryptography.setKey(restoreKey(isPublic, key));
        cryptography.setData(data);

        return cryptography.doCipher();
    }

    /**
     * 公钥加密
     *
     * @param data 待加密数据
     * @param key  公钥
     * @return 加密后的字节数组
     */
    public static byte[] publicKeyEncrypt(byte[] data, String key) {
        return action(true, true, data, key);
    }

    public static String publicKeyEncryptAsBase64Str(byte[] data, String key) {
        return new Base64Utils(publicKeyEncrypt(data, key)).encodeAsString();
    }

    /**
     * 公钥解密
     *
     * @param data 加密数据
     * @param key  公钥
     * @return 解密后的字节数组
     */
    public static byte[] publicKeyDecrypt(byte[] data, String key) {
        return action(false, true, data, key);
    }

    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @param key  私钥
     * @return 加密后的字节数组
     */
    public static byte[] privateKeyEncrypt(byte[] data, String key) {
        return action(true, false, data, key);
    }

    /**
     * 私钥解密
     *
     * @param data 加密数据
     * @param key  私钥
     * @return 解密后的字节数组
     */
    public static byte[] privateKeyDecrypt(byte[] data, String key) {
        return action(false, false, data, key);
    }

    public static String privateKeyDecryptAsStr(byte[] data, String key) {
        return new StringBytes(privateKeyDecrypt(data, key)).getUTF8_String(); // needs to Base64?
    }

    /**
     * 从输入流中加载私钥
     * 该方法首先将输入流中的字节读取到 ByteArrayOutputStream 中，然后将其转换为字符串形式的私钥，
     * 最后调用另一方法 loadPrivateKey(String) 来解析并返回私钥对象
     *
     * @param in 包含私钥信息的输入流
     * @return 解析后的 PrivateKey 对象
     * @throws IllegalArgumentException 如果输入流中的数据无法被正确读取或解析为私钥，则抛出此异常
     */
    public static PrivateKey loadPrivateKey(InputStream in) {
        return loadPrivateKey(in, CommonConstant.UTF8);
    }

    /**
     * 从输入流中加载私钥
     * 该方法首先将输入流中的字节读取到 ByteArrayOutputStream 中，然后将其转换为字符串形式的私钥，
     * 最后调用另一方法 loadPrivateKey(String) 来解析并返回私钥对象
     *
     * @param in      包含私钥信息的输入流
     * @param charset 字符集编码
     * @return 解析后的 PrivateKey 对象
     * @throws IllegalArgumentException 如果输入流中的数据无法被正确读取或解析为私钥，则抛出此异常
     */
    public static PrivateKey loadPrivateKey(InputStream in, String charset) {
        String privateKey;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream(2048);) {
            new DataWriter(out).write(in);
            privateKey = out.toString(charset);
        } catch (IOException e) {
            throw new UncheckedIOException("无效的密钥", e);
        }

        return restorePrivateKey(privateKey);
    }

    public static PrivateKey loadPrivateKey(String filePath) {
        String fileContent = new FileHelper(filePath).getFileContent();

        return restorePrivateKey(fileContent);
    }
}

