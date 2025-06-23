package com.ajaxjs.pay.wxpay;


import com.ajaxjs.pay.core.kit.WxPayKit;
import com.ajaxjs.pay.wxpay.enums.WxDomainEnum;
import com.ajaxjs.pay.wxpay.enums.v3.BasePayApiEnum;
import com.ajaxjs.pay.wxpay.enums.v3.CertAlgorithmTypeEnum;
import com.ajaxjs.pay.wxpay.enums.v3.OtherApiEnum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WxPayKitTest {

    @Test
    public void hmacSHA256() {
        Assertions.assertEquals("8ae6af1a6f6e75f20b8240f320f33e1a376105c5668f1b57a591cd61fe409ee3",
                WxPayKit.hmacSha256("IJPay", "123"));
    }

    @Test
    public void md5() {
        Assertions.assertEquals("cbfc2149d454ecf4ab0f32e58430fcdd",
                WxPayKit.md5("IJPay"));
    }

    @Test
    public void encryptData() {
        Assertions.assertEquals("K8fdh/6THGfTKio8pxXS6Q==",
                WxPayKit.encryptData("IJPay", "42cc1d91bab89b65ff55b19e28fff4f0"));
    }

    @Test
    public void decryptData() {
        Assertions.assertEquals("IJPay",
                WxPayKit.decryptData(
                        WxPayKit.encryptData("IJPay", "42cc1d91bab89b65ff55b19e28fff4f0"),
                        "42cc1d91bab89b65ff55b19e28fff4f0"));
    }

    @Test
    public void StringFormat() {
        Assertions.assertEquals(WxDomainEnum.CHINA.toString().concat(String.format(BasePayApiEnum.ORDER_QUERY_BY_TRANSACTION_ID.toString(), "123456789")),
                String.format(WxDomainEnum.CHINA.toString().concat(BasePayApiEnum.ORDER_QUERY_BY_TRANSACTION_ID.toString()), "123456789"));
    }

    @Test
    public void certAlgorithmTypeEnum() {
        Assertions.assertEquals("/v3/certificates?algorithm_type=SM2", CertAlgorithmTypeEnum.getCertSuffixUrl(CertAlgorithmTypeEnum.SM2));
    }

    @Test
    public void certAlgorithmTypeEnumNone() {
        Assertions.assertEquals("/v3/certificates", CertAlgorithmTypeEnum.getCertSuffixUrl(CertAlgorithmTypeEnum.NONE));
    }

    @Test
    public void certAlgorithmTypeEnumOther() {
        Assertions.assertEquals("/v3/certificates", CertAlgorithmTypeEnum.getCertSuffixUrl("OTHER"));
    }

    @Test
    public void getCertUrl() {
        Assertions.assertEquals(WxDomainEnum.CHINA.getDomain() + String.format(OtherApiEnum.GET_CERTIFICATES.getUrl()),
                CertAlgorithmTypeEnum.getCertUrl(WxDomainEnum.CHINA, CertAlgorithmTypeEnum.NONE));
    }

    @Test
    public void getCertUrlBySm2() {
        Assertions.assertEquals(WxDomainEnum.CHINA.getDomain() + String.format(OtherApiEnum.GET_CERTIFICATES_BY_ALGORITHM_TYPE.getUrl(), CertAlgorithmTypeEnum.SM2.getCode()),
                CertAlgorithmTypeEnum.getCertUrl(WxDomainEnum.CHINA, CertAlgorithmTypeEnum.SM2));
    }
}
