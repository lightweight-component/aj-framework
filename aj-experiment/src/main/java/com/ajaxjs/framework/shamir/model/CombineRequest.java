package com.ajaxjs.framework.shamir.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 恢复请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombineRequest {
    /**
     * 密钥份额列表
     */
    private List<String> shares;
}