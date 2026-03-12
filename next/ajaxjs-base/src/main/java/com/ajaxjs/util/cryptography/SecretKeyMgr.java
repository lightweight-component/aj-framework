package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.StringBytes;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

/**
 * 对称加密的密钥工具类
 */
public class SecretKeyMgr {
    /**
     * 获取对称加密用的 SecretKey
     *
     * @param algorithmName The name of algorithm
     * @param secure        Pass 0 if it's optional
     * @param keySize       Pass null if it's optional
     * @return The secret key, it's symmetric
     */
    public static SecretKey getSecretKey(String algorithmName, int keySize, SecureRandom secure) {
        KeyGenerator kg;

        try {
            kg = KeyGenerator.getInstance(algorithmName);

            if (keySize != 0 && secure != null)
                kg.init(keySize, secure);
            else if (keySize == 0 && secure != null)
                kg.init(secure);
            else if (keySize != 0 && secure == null)
                kg.init(keySize);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        return kg.generateKey();
    }

    /**
     * Get the secret key by the given algorithm name and key spec
     *
     * @param algorithmName The name of algorithm
     * @param spec          A (transparent) specification of the key material
     * @return A key does not belong to symmetric or not
     */
    public static Key getSecretKey(String algorithmName, KeySpec spec) {
        try {
            return SecretKeyFactory.getInstance(algorithmName).generateSecret(spec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid Key Spec.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }
    }

    /**
     * 获取指定算法的安全随机数生成器实例，并使用给定密钥作为种子初始化
     *
     * @param algorithmName 随机数生成算法名称，如"SHA1PRNG"
     * @param key           用于初始化随机数生成器的种子密钥字符串
     * @return 初始化完成的 SecureRandom 实例
     * @throws RuntimeException 当指定的算法不存在时抛出
     */
    public static SecureRandom getRandom(String algorithmName, String key) {
        SecureRandom random;

        try {
            random = SecureRandom.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        }

        random.setSeed(new StringBytes(key).getUTF8_Bytes());

        return random;
    }

    /**
     * 根据指定算法和安全随机数生成一个秘密密钥，并将其以 Base64 编码的字符串形式返回
     *
     * @param algorithmName The name of algorithm
     * @param keySize       The size of key
     * @param secure        安全随机数
     * @return Base64 编码后的秘密密钥字符串
     */
    public static String getSecretKeyAsStr(String algorithmName, int keySize, SecureRandom secure) {
        byte[] encoded = getSecretKey(algorithmName, keySize, secure).getEncoded();

        return new Base64Utils(encoded).encodeAsString();
    }
}
