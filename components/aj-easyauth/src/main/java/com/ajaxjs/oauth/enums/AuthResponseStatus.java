package com.ajaxjs.oauth.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JustAuth通用的状态码对照表
 */
@Getter
@AllArgsConstructor
public enum AuthResponseStatus {
    /**
     * 2000：正常；
     * other：调用异常，具体异常内容见{@code msg}
     */
    SUCCESS(2000, "Success"),
    FAILURE(5000, "Failure"),
    NOT_IMPLEMENTED(5001, "Not Implemented"),
    PARAMETER_INCOMPLETE(5002, "Parameter incomplete"),
    UNSUPPORTED(5003, "Unsupported operation"),
    NO_AUTH_SOURCE(5004, "AuthDefaultSource cannot be null"),
    UNIDENTIFIED_PLATFORM(5005, "Unidentified platform"),
    ILLEGAL_REDIRECT_URI(5006, "Illegal redirect uri"),
    ILLEGAL_REQUEST(5007, "Illegal request"),
    ILLEGAL_CODE(5008, "Illegal code"),
    ILLEGAL_STATUS(5009, "Illegal state"),
    REQUIRED_REFRESH_TOKEN(5010, "The refresh token is required; it must not be null"),
    ILLEGAL_TOKEN(5011, "Invalid token"),
    ILLEGAL_KID(5012, "Invalid key identifier(kid)"),
    ILLEGAL_TEAM_ID(5013, "Invalid team id"),
    ILLEGAL_CLIENT_ID(5014, "Invalid client id"),
    ILLEGAL_CLIENT_SECRET(5015, "Invalid client secret"),
    ILLEGAL_WECHAT_AGENT_ID(5016, "Illegal wechat agent id"),
    ;

    private final int code;
    private final String msg;
}

