package com.ajaxjs.framework.shamir.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 拆分请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SplitRequest {
    /**
     * 原始密钥
     */
    private String secret;

    /**
     * 总份额数 (n)
     */
    private Integer totalShares;

    /**
     * 门限值 (t)
     */
    private Integer threshold;
}