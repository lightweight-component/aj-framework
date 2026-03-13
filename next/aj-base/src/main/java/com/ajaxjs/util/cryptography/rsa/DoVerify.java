package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.security.*;

/**
 * Verify signature
 * Inputs algorithm, data(string/bytes[]), signature and public key to verify signature.
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class DoVerify {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The data to be verified, in bytes.
     */
    private byte[] data;

    /**
     * The data to be verified, in string. It'll be converted to bytes in UTF-8 by default.
     */
    private String strData;

    public DoVerify setStrData(String strData) {
        this.strData = strData;
        this.data = strData.getBytes(StandardCharsets.UTF_8);

        return this;
    }

    private byte[] signatureData;

    /**
     * @param signatureBase64 The signature string should be a Base64 string.
     * @return This
     */
    public DoVerify setSignatureBase64(String signatureBase64) {
        signatureData = new Base64Utils(signatureBase64).decode();

        return this;
    }

    /**
     * The public key
     */
    private PublicKey publicKey;

    /**
     * The public key, in string.
     */
    private String publicKeyStr;

    public DoVerify setPublicKeyStr(String publicKeyStr) {
        this.publicKeyStr = publicKeyStr;
        publicKey = (PublicKey) KeyMgr.restoreKey(true, publicKeyStr);

        return this;
    }

    /**
     * 验证数字签名的有效性
     *
     * @return boolean 签名验证结果，true 表示签名有效，false 表示签名无效
     * @throws RuntimeException         当签名算法不可用或签名失败时抛出
     * @throws IllegalArgumentException 当公钥无效时抛出
     */
    public boolean verify() {
        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initVerify(publicKey);
            signature.update(data);

            return signature.verify(signatureData);
        } catch (SignatureException e) {
            throw new RuntimeException("Signature failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Private Key", e);
        }
    }
}
