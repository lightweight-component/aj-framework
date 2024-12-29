package com.ajaxjs.util.cryptography;

public interface SecurityTextType {
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

    enum Cryptography {

    }
}
