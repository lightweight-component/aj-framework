package com.ajaxjs.framework.wechat.payment.model;

import lombok.Data;

@Data
public class PayResultAmount {
    private Long payer_total;
    private Long total;
    private String currency;
    private String payer_currency;
}
