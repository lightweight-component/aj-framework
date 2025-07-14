package com.ajaxjs.business.web.rate_limiter.ratelimiter2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppRuleConfig {
    private String appId;

    private List<ApiLimit> limits;
}
