package com.ajaxjs.api.encryptedbody;

import lombok.Data;

/**
 * 承载加密的数据
 */
@Data
public class DecodeDTO {
    /**
     * Encrypted data
     */
    private String data;
}
