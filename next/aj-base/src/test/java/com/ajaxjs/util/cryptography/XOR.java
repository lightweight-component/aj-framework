package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.BytesHelper;

import java.util.Objects;

/**
 * Exclusive OR
 */
public class XOR {
    /**
     * 使用异或操作符对字符串进行简单加密。
     * 该方法通过将字符串的每个字节与一个固定密钥的哈希值进行异或操作，以达到加密的目的。加密后的字节数组再通过工具方法转换为十六进制字符串返回。
     * 此方法适用于对数据进行简单的安全性保护，但并非安全的加密手段，适用于对安全性要求不高的场景。
     *
     * @param res 需要加密的字符串。
     * @param key 加密使用的密钥。注意，该密钥将被转换为哈希值后用于加密操作。
     * @return 返回加密后的十六进制字符串。
     */
    public static String encode(String res, String key) {
        byte[] bs = res.getBytes();

        for (int i = 0; i < bs.length; i++)
            bs[i] = (byte) xor(bs[i], key);

        return BytesHelper.bytesToHexStr(bs);
    }

    /**
     * 使用异或进行解密
     *
     * @param res 需要解密的密文
     * @param key 秘钥
     * @return 结果
     */
    public static String decode(String res, String key) {
        byte[] bs = BytesHelper.parseHexStr2Byte(res);

        for (int i = 0; i < Objects.requireNonNull(bs).length; i++)
            bs[i] = (byte) xor(bs[i], key);

        return new String(bs);
    }

    /**
     * 使用异或操作对结果进行加密或解密。
     * 异或操作的特点是，如果对相同的两个值进行异或操作，结果是 0；而且异或操作是可逆的，即 a ^ b ^ b = a。
     * 这里使用字符串的哈希值作为密钥，因为字符串的哈希值在大多数情况下是不同的，这样可以增加解密的难度。
     * 但是需要注意，由于哈希冲突的可能性，不同字符串的哈希值可能会相同，这可能会导致解密错误。
     *
     * @param res 需要加密或解密的整数结果。
     * @param key 用于加密或解密的字符串密钥。
     * @return 经过异或操作后的加密或解密结果。
     */
    public static int xor(int res, String key) {
        return res ^ key.hashCode();
    }
}
