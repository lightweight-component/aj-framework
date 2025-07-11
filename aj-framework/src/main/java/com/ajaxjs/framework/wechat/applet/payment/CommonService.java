package com.ajaxjs.framework.wechat.applet.payment;


import com.ajaxjs.framework.wechat.merchant.MerchantConfig;
import com.ajaxjs.util.StrUtil;
import com.ajaxjs.util.cryptography.WeiXinCrypto;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 通用逻辑
 */
@Slf4j
public abstract class CommonService {

    abstract public MerchantConfig getMchCfg();

    /**
     * 解密回调数据
     *
     * @param params 回调数据
     * @return 解密后的 JSON 字符串
     */
    public String decrypt(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resource = (Map<String, Object>) params.get("resource");
        log.info(params.get("summary") + String.valueOf(resource));

        // 对 resource 对象进行解密
        String ciphertext = resource.get("ciphertext").toString();
        log.info(ciphertext);

        byte[] apiV3KeyByte = StrUtil.getUTF8_Bytes(getMchCfg().getApiV3Key());
        byte[] associatedData = StrUtil.getUTF8_Bytes(resource.get("associated_data").toString());
        byte[] nonce = StrUtil.getUTF8_Bytes(resource.get("nonce").toString());

        // 解密
//        AesUtil aesUtil = new AesUtil(apiV3KeyByte);
//        String cert;
//
//        try {
//            cert = aesUtil.decryptToString(associatedData, nonce, ciphertext);
//        } catch (GeneralSecurityException e) {
//            log.warn("decrypt", e);
//        }
        String cert = WeiXinCrypto.aesDecryptToString(apiV3KeyByte, associatedData, nonce, ciphertext);

        log.info(cert);

        return cert;
    }
}
