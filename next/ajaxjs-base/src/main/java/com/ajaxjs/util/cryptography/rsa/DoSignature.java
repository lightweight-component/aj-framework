package com.ajaxjs.util.cryptography.rsa;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.cryptography.Constant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.charset.StandardCharsets;
import java.security.*;


/**
 * 签名
 * Inputs algorithm, data(string/bytes[]) and private key to generate signature.
 */
@RequiredArgsConstructor
@Accessors(chain = true)
@Data
public class DoSignature {
    /**
     * The name of algorithm, required.
     */
    private final String algorithmName;

    /**
     * The data to be signed, in bytes.
     */
    private byte[] data;

    /**
     * The data to be signed, in string. It'll be converted to bytes in UTF-8 by default.
     */
    private String strData;

    public DoSignature setStrData(String strData) {
        this.strData = strData;
        this.data = strData.getBytes(StandardCharsets.UTF_8);

        return this;
    }

    /**
     * The private key
     */
    private PrivateKey privateKey;

    /**
     * The private key, in string.
     */
    private String privateKeyStr;

    public DoSignature setPrivateKeyStr(String privateKeyStr) {
        this.privateKeyStr = privateKeyStr;
        privateKey = (PrivateKey) KeyMgr.restoreKey(false, privateKeyStr);

        return this;
    }

    /**
     * Sign the data.
     *
     * @return The signature in bytes.
     */
    public byte[] sign() {
        try {
            Signature signature = Signature.getInstance(algorithmName);
            signature.initSign(privateKey);
            signature.update(data);

            return signature.sign();
        } catch (SignatureException e) {
            throw new RuntimeException("Signature failed.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(Constant.NO_SUCH_ALGORITHM + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid Private Key", e);
        }
    }

    /**
     * Sign the data then returns it as Base64 string.
     *
     * @return The signature in Base64 string.
     */
    public String signToString() {
        return new Base64Utils(sign()).encodeAsString();
    }
}
