package com.ajaxjs.util.cryptography;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeiXinUtilTest {
    /**
     * 解密小程序提供的加密数据，返回包含手机号码等信息的 JSON 对象
     *
     * @param iv         前端给的
     * @param cipherText 前端给的，密文
     * @param sessionKey 后端申请返回
     * @return 解密后的文本
     */
    public static String aesDecryptPhone(String iv, String cipherText, String sessionKey) {
        byte[] keyData = new Base64Utils(sessionKey).decode();

        Cryptography cryptography = new Cryptography(Constant.AES_WX_MINI_APP, Cipher.DECRYPT_MODE);
        cryptography.setKey(new SecretKeySpec(keyData, Constant.AES)); // little odd, it's AES, differs with AES_WX_MINI_APP.
        cryptography.setSpec(new IvParameterSpec(new Base64Utils(iv).decode()));
        cryptography.setDataStrBase64(cipherText);

        return cryptography.doCipherAsStr();
    }

    @Test
    void testAesDecryptPhone() {
        String sessionKey = "IOv62NY75gNbTYVEe1ogWQ==";
        String iv = "b/+OsOf6+y4Hl6RXJW+CjQ==";
        String ciphertext = "6fxmM2gjyAk5v9mzSnPXw3xv4WTYywHH/JK9A78Zb2K8i9kehzGLd3xalzx8qNgkZ/SG4/kfL8DgpvQBEoygi7K7YNguUW7HNYHkESUiGXId+DGpziBjmxmoPquFZ8N2XF71kn6MYfXVUiwxCHRu5YYlTbKr4IjA2xqKMgAhaK6YsyD1NE9iOH4eYnT9Ky7B54BW0yWVH3NFgkTmEBQTNg==";
        String decryptedText = aesDecryptPhone(iv, ciphertext, sessionKey);
        // Add assertions to validate the decrypted text
        System.out.println(decryptedText);
    }


    /* ----------------- 敏感信息加密 ------------------- */
    /* <a href="https://pay.weixin.qq.com/doc/global/v3/zh/4012354992">...</a> */

    /**
     * 敏感信息加密
     *
     * @param message     数据
     * @param certificate 证书
     * @return 加密后的文本
     */
    public static String encryptOAEP(String message, X509Certificate certificate) {
        Cryptography cryptography = new Cryptography(Constant.RSAES_OAEP, Cipher.ENCRYPT_MODE);
        cryptography.setKey(certificate.getPublicKey());
        cryptography.setDataStr(message);

        return cryptography.doCipherAsBase64Str();
    }

    /**
     * 解密
     *
     * @param cipherText 密文
     * @param privateKey 商户私钥
     * @return 解密后的文本
     */
    public static String decryptOAEP(String cipherText, PrivateKey privateKey) {
        Cryptography cryptography = new Cryptography(Constant.RSAES_OAEP, Cipher.DECRYPT_MODE);
        cryptography.setKey(privateKey);
        cryptography.setDataStrBase64(cipherText);

        return cryptography.doCipherAsStr();
    }

    static String text = "Hello world";

    @Test
    void testEncryptOAEP() {
        // 公钥加密
        String result = encryptOAEP(text, CertificateUtils.getCert("D:\\sp42\\code\\ajaxjs\\aj-util\\src\\test\\java\\com\\ajaxjs\\util\\cryptography\\1623777099_20251021_cert\\apiclient_cert.pem"));
        System.out.println(result);

        // 私钥解密
        PrivateKey privateKey = KeyMgr.loadPrivateKey("D:\\sp42\\code\\ajaxjs\\aj-util\\src\\test\\java\\com\\ajaxjs\\util\\cryptography\\1623777099_20251021_cert\\apiclient_key.pem");

        String decryptOAEP = decryptOAEP(result, privateKey);
        assertEquals(text, decryptOAEP);
    }

}