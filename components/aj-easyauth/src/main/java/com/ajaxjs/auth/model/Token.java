package com.ajaxjs.auth.model;

import lombok.Data;

@Data
public class Token {
    private String accessToken;

    private int expireIn;

    private String refreshToken;

    private int refreshTokenExpireIn;

    private String uid;

    private String openId;

    private String accessCode;

    private String unionId;
}
