package com.ajaxjs.business.web.load_balance;

import lombok.Data;

@Data
public class ServiceInstance {
    private String serverName;

    private Boolean isolated;

    private Integer qzValue;

    public Boolean isIsolated() {
        return isolated;
    }
}
