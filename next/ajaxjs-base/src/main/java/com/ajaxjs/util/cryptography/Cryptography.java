package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.BytesHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.StringBytes;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;

/**
 * AES/DES/3DES/PBE 对称加密/解密
 */
@Data
@RequiredArgsConstructor
public class Cryptography {
    /**
     * The name of the algorithm
     */
    private final String algorithmName;

    /**
     * 是解密模式还是加密模式？
     */
    private final int mode;

    private Key key;

    public void setKeyData(byte[] keyData) {
        key = new SecretKeySpec(keyData, algorithmName);
    }

    private SecretKey secretKey;

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
        key = new SecretKeySpec(secretKey.getEncoded(), algorithmName);
    }

    private byte[] data;

    public void setDataStr(String dataStr) {
        data = new StringBytes(dataStr).getUTF8_Bytes();
    }

    public void setDataStrBase64(String dataStrBase64) {
        data = new Base64Utils(dataStrBase64).decode();
    }

    private AlgorithmParameterSpec spec;

    private byte[] associatedData;

    public byte[] doCipher() {
        try {
            Cipher cipher = Cipher.getInstance(algorithmName);

            if (spec != null)
                cipher.init(mode, key, spec);
            else
                cipher.init(mode, key);

            if (associatedData != null)
                cipher.updateAAD(associatedData);

            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException("The input string can't over size of 214 bytes", e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Key.", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalArgumentException("Invalid Algorithm Parameter.", e);
        }
    }

    public String doCipherAsStr() {
        return new StringBytes(doCipher()).getUTF8_String();
    }

    public String doCipherAsBase64Str() {
        return new Base64Utils(doCipher()).encodeAsString();
    }

    /**
     * Get hex string of cipher, which is good for encrypt.
     *
     * @return Hex string of cipher.
     */
    public String doCipherAsHexStr() {
        return BytesHelper.bytesToHexStr(doCipher());
    }

    /**
     * Do encrypt
     *
     * @param data The text to be encrypted
     * @param key  The key
     * @return The encrypted string
     */
    public static String AES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    /**
     * Do decrypt
     *
     * @param data The text to be decrypted
     * @param key  The key
     * @return The decrypted string
     */
    public static String AES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.AES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.AES, 128, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    public static String DES_encode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.ENCRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setDataStr(data);

        return cryptography.doCipherAsHexStr();
    }

    public static String DES_decode(String data, String key) {
        Cryptography cryptography = new Cryptography(Constant.DES, Cipher.DECRYPT_MODE);
        cryptography.setSecretKey(SecretKeyMgr.getSecretKey(Constant.DES, 0, SecretKeyMgr.getRandom(Constant.SECURE_RANDOM_ALGORITHM, key)));
        cryptography.setData(BytesHelper.parseHexStr2Byte(data));

        return cryptography.doCipherAsStr();
    }

    public static byte[] tripleDES_encode(String data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.ENCRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    public static String tripleDES_decode(byte[] data, byte[] key) {
        Cryptography cryptography = new Cryptography(Constant.TRIPLE_DES, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(key, Constant.TRIPLE_DES));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }

    /**
     * 初始化盐（salt）
     *
     * @return 盐（salt）
     */
    public static byte[] initSalt() {
        byte[] salt = new byte[8];
        RandomTools.RANDOM.nextBytes(salt);

        return salt;
    }

    public static byte[] PBE_encode(String data, String key, byte[] salt, int iterationCount) {
        Cryptography cryptography = new Cryptography(Constant.PBE, Cipher.ENCRYPT_MODE);
        cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE, new PBEKeySpec(key.toCharArray())));
        cryptography.setSpec(new PBEParameterSpec(salt, iterationCount));// 100
        cryptography.setDataStr(data);

        return cryptography.doCipher();
    }

    public static String PBE_decode(byte[] data, String key, byte[] salt, int iterationCount) {
        Cryptography cryptography = new Cryptography(Constant.PBE, Cipher.DECRYPT_MODE);
        cryptography.setKey(SecretKeyMgr.getSecretKey(Constant.PBE, new PBEKeySpec(key.toCharArray())));
        cryptography.setSpec(new PBEParameterSpec(salt, iterationCount));
        cryptography.setData(data);

        return cryptography.doCipherAsStr();
    }
}
