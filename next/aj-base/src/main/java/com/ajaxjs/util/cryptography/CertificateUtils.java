package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.StringBytes;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.cert.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utils for certificate
 */
public class CertificateUtils {
    /**
     * Get a certificate by a file path
     * X509Certificate is text file, not binary file.
     *
     * @param filePath File path
     * @return Certificate Object
     */
    public static X509Certificate getCert(String filePath) {
        try {
            return getCert(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("File Not Found:" + filePath, e);
        }
    }

    /**
     * Get a certificate by an Input Stream
     * X509Certificate is text file, not binary file.
     *
     * @param in Input stream, which contains a certificate. When it's done, it will be closed.
     * @return Certificate Object
     * @throws UncheckedIOException 如果证书读取过程中发生 IO 错误，则抛出运行时异常
     */
    public static X509Certificate getCert(InputStream in) {
        try (InputStream input = in) {
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(in);
            cert.checkValidity();

            return cert;
        } catch (CertificateExpiredException e) {
            throw new RuntimeException("Certificate has been expired", e);
        } catch (CertificateNotYetValidException e) {
            throw new RuntimeException("Certificate is not yet valid", e); // 证书尚未生效
        } catch (CertificateException e) {
            throw new RuntimeException("Certificate validity failed.", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 反序列化证书并解密
     *
     * @param apiV3Key APIv3 密钥
     * @param pMap     下载证书的请求返回体
     * @return 证书 list
     */
    @SuppressWarnings("unchecked")
    public static Map<BigInteger, X509Certificate> deserializeToCerts(String apiV3Key, Map<String, Object> pMap) {
        byte[] apiV3KeyByte = new StringBytes(apiV3Key).getUTF8_Bytes();
        List<Map<String, Object>> list = (List<Map<String, Object>>) pMap.get("data");
        Map<BigInteger, X509Certificate> newCertList = new HashMap<>();

        if (!ObjectHelper.isEmpty(list)) {
            for (Map<String, Object> map : list) {
                Map<String, Object> certificate = (Map<String, Object>) map.get("encrypt_certificate");

                // 解密
                String cert = aesDecryptToString(apiV3KeyByte,
                        remove(certificate.get("associated_data")),
                        remove(certificate.get("nonce")),
                        remove(certificate.get("ciphertext")));

                X509Certificate x509Cert = getCert(new ByteArrayInputStream(new StringBytes(cert).getUTF8_Bytes()));
                newCertList.put(x509Cert.getSerialNumber(), x509Cert);
            }
        }

        return newCertList;
    }

    private static String remove(Object v) {
        return v.toString().replace("\"", "");
    }

    /**
     * AEAD_AES_256_GCM 解密
     *
     * @param aesKey         key 密钥，ApiV3Key，长度必须为32个字节
     * @param associatedData 相关数据
     * @param nonce          随机字符串
     * @param cipherText     密文
     * @return 解密后的文本
     */
//    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
//        if (aesKey.length != 32)
//            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");
//
//        GCMParameterSpec spec = new GCMParameterSpec(128, nonce);
//
//        return doCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, aesKey, spec, cipherText, associatedData);
//    }
    public static String aesDecryptToString(byte[] aesKey, String associatedData, String nonce, String cipherText) {
        return aesDecryptToString(aesKey, new StringBytes(associatedData).getUTF8_Bytes(), new StringBytes(nonce).getUTF8_Bytes(), cipherText);
    }

    public static String aesDecryptToString(byte[] aesKey, byte[] associatedData, byte[] nonce, String cipherText) {
        if (aesKey.length != 32)
            throw new IllegalArgumentException("无效的 ApiV3Key，长度必须为32个字节");

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP2, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(aesKey, Constant.AES)); // little odd, it's AES.
        cryptography.setSpec(new GCMParameterSpec(128, nonce));
        cryptography.setDataStrBase64(cipherText);
        cryptography.setAssociatedData(associatedData);

        return cryptography.doCipherAsStr();
    }
}
