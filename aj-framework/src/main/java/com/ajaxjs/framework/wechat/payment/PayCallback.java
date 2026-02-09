package com.ajaxjs.framework.wechat.payment;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.framework.wechat.payment.model.PayResult;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.cryptography.CertificateUtils;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.rsa.DoVerify;
import com.ajaxjs.util.cryptography.rsa.KeyMgr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付后的回调处理
 * <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012791861">...</a>
 */
@Slf4j
@RequiredArgsConstructor
public class PayCallback {
    private final String apiV3Key;

    /**
     * 公钥证书路径。注意这只是公钥，不是完整的证书，应该 -----BEGIN PUBLIC KEY----- 开头的
     */
    private final String publicKeyPath;

    private final static String SUCCESS = "TRANSACTION.SUCCESS";

    /**
     * 用户支付后，微信通知我们的接口。
     * 客户端无须调用该接口，该接口由微信支付中心调用
     *
     * @param params 回调参数
     * @return 支付结果
     */
    public PayResult payCallback(Map<String, Object> params) {
        if (params.containsKey("event_type") && SUCCESS.equals(params.get("event_type"))) {
            String cert = decrypt(params, apiV3Key); // 支付成功

            return json2PayResultBean(cert);

//            if (TRADE_STATE.equals(bean.getTrade_state())) {// 再次检查
//                // 业务逻辑判断是否收到钱
//                log.info("收到钱：" + bean.getPayer_total());
//
//                return BaseController.jsonOk();
//            } else
//                throw new NullPointerException("解密失败");
        }

        throw new IllegalArgumentException("返回参数失败！");
    }

    /**
     * 解密回调数据
     *
     * @param params   回调数据
     * @param apiV3Key apiV3Key
     * @return 解密后的 JSON 字符串
     */
    public static String decrypt(Map<String, Object> params, String apiV3Key) {
        @SuppressWarnings("unchecked")
        Map<String, Object> resource = (Map<String, Object>) params.get("resource");
        log.info(params.get("summary") + String.valueOf(resource));

        // 对 resource 对象进行解密
        String ciphertext = resource.get("ciphertext").toString();
        log.info(ciphertext);

        byte[] apiV3KeyByte = apiV3Key.getBytes(StandardCharsets.UTF_8);
        String associatedData = resource.get("associated_data").toString();
        String nonce = resource.get("nonce").toString();

        // 解密
        String cert = CertificateUtils.aesDecryptToString(apiV3KeyByte, associatedData, nonce, ciphertext);
        log.info(cert);

        return cert;
    }

    /**
     * 官方返回的 JSON 是嵌套的，现在将其扁平化
     *
     * @param json JSON
     * @return PayResult
     */
    public static PayResult json2PayResultBean(String json) {
        Map<String, Object> map = JsonUtil.json2map(json);
        PayResult bean = JsonUtil.map2pojo(map, PayResult.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> amount = (Map<String, Object>) map.get("amount");
        bean.setTotal((int) amount.get("total"));
        bean.setPayer_total((int) amount.get("payer_total"));

        @SuppressWarnings("unchecked")
        Map<String, Object> payer = (Map<String, Object>) map.get("payer");
        bean.setPayerOpenId(payer.get("openid").toString());

        log.info("Parsed notification - {}", bean);

        return bean;
    }

    /**
     * 签名验证
     */
    public void verifySignature(String signature, String timestamp, String nonce, String serial, String requestBody) {
        if (ObjectHelper.isEmptyText(signature) || ObjectHelper.isEmptyText(timestamp) || ObjectHelper.isEmptyText(nonce) || ObjectHelper.isEmptyText(serial)) {
            log.warn("Missing required headers for signature verification.");
            throw new BusinessException("Missing required headers for signature verification.");
        }

        // 构造待签名字符串
        String message = timestamp + "\n" + nonce + "\n" + requestBody + "\n";

        boolean isValid = new DoVerify(Constant.SHA256_RSA)
                .setStrData(message)
                .setSignatureBase64(signature)
                .setPublicKey(loadPublicKeyFromPem(serial)).verify();

        if (isValid)
            log.info("Signature verification passed. Powered by AJAX!");
        else {
            log.warn("Invalid signature in notification.");
            throw new BusinessException("Invalid signature in notification.");
        }
    }

    private static final Map<String, PublicKey> WECHAT_PAY_PUBLIC_KEY_MAP = new ConcurrentHashMap<>();

    public PublicKey loadPublicKeyFromPem(String key) {
        if (WECHAT_PAY_PUBLIC_KEY_MAP.containsKey(key))
            return WECHAT_PAY_PUBLIC_KEY_MAP.get(key);

        log.info("loadPublicKeyFromPem");

        String pemContent = DiContextUtil.readResourceAsString(publicKeyPath); // cache it

        if (ObjectHelper.isEmptyText(pemContent))
            throw new BusinessException("公钥证书为空，请检查路径 " + publicKeyPath + " 是否正确");

        Key _key = KeyMgr.restoreKey(true, pemContent);
        PublicKey publicKey = (PublicKey) _key;
        WECHAT_PAY_PUBLIC_KEY_MAP.put(key, publicKey);

        return publicKey;
    }
}
