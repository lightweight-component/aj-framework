package com.ajaxjs.util.cryptography;

public interface Constant {
    String AES = "AES";

    String AES_WX_MINI_APP = "AES/CBC/PKCS5Padding";

    String AES_WX_MINI_APP2 = "AES/GCM/NoPadding";

    String DES = "DES";

    @SuppressWarnings("SpellCheckingInspection")
    String TRIPLE_DES = "DESede";

    /**
     * 定义加密方式 支持以下任意一种算法
     *
     * <pre>
     * PBEWithMD5AndDES
     * PBEWithMD5AndTripleDES
     * PBEWithSHA1AndDESede
     * PBEWithSHA1AndRC2_40
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    String PBE = "PBEWITHMD5andDES";

    String RSA = "RSA";// "RSA/ECB/PKCS1Padding"

    String MD5_RSA = "MD5withRSA";

    String SHA256_RSA = "SHA256withRSA";

    String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * Optimal Asymmetric Encryption Padding
     */
    String RSAES_OAEP = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    String NO_SUCH_ALGORITHM = "No Such Algorithm in this Java. ";

    enum Encode {
        BASE16,

        BASE32,
        BASE58,
        BASE64,
        BASE91,
    }

    enum Digest {
        Md5,
        Md5WithSalt,
    }
}
