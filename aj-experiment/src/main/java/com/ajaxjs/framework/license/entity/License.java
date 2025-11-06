package com.ajaxjs.framework.license.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 许可证实体类
 */
@Data
@NoArgsConstructor
@JsonPropertyOrder({"subject", "issuedTo", "hardwareId", "expireAt", "features"})
public class License {
    private String subject;        // 软件名称
    private String issuedTo;       // 授权给谁
    private String hardwareId;     // 硬件指纹（主板序列号）

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireAt;    // 到期时间

    private List<String> features; // 功能权限列表
    private String signature;      // 签名（JSON序列化时忽略）

    public License(String subject, String issuedTo, String hardwareId, LocalDate expireAt, List<String> features) {
        this.subject = subject;
        this.issuedTo = issuedTo;
        this.hardwareId = hardwareId;
        this.expireAt = expireAt;
        this.features = features;
    }
}