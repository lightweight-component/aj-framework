package com.ajaxjs.framework.wechat.payment;

import com.ajaxjs.framework.wechat.payment.model.PayResult;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.cryptography.CertificateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 支付后的回调处理
 * <a href="https://pay.weixin.qq.com/doc/v3/merchant/4012791861">...</a>
 */
@Slf4j
@RequiredArgsConstructor
public class PayCallback {
    private final String apiV3Key;

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
     * 签名验证 (TODO 部分): 这是绝对不能省略的安全步骤。你需要：
     * <p>
     * 从请求头获取 Wechatpay-Signature, Wechatpay-Timestamp, Wechatpay-Nonce, Wechatpay-Serial。
     * 使用 Wechatpay-Serial 找到对应的微信平台公钥证书（证书需要提前获取并缓存）。
     * 构造待签名字符串：{Wechatpay-Timestamp}\n{Wechatpay-Nonce}\n{RequestBody}\n。
     * 使用微信平台公钥和 SHA256withRSA 算法验证 Wechatpay-Signature 是否正确。
     * 如果验证失败，必须返回 "FAIL"。
     */
    public void verifySignature(String signature, String timestamp, String nonce, String serial, String requestBody) {
//        if (ObjectHelper.isEmptyText(signature) || ObjectHelper.isEmptyText(timestamp) || ObjectHelper.isEmptyText(nonce) || ObjectHelper.isEmptyText(serial)) {
//            log.warn("Missing required headers for signature verification.");
//            return "FAIL";
//        }

// 1.2. 获取微信平台证书 (需要根据 serial 获取对应的证书)

        // String platformPublicKeyContent = getPlatformPublicKey(serial); // 需要实现此方法
        // X509Certificate platformCert = WeChatPaySignatureUtil.loadCertificate(platformPublicKeyContent);

        // 1.3. 构造待签名字符串
        String message = timestamp + "\n" + nonce + "\n" + requestBody + "\n";

        // 1.4. 使用平台证书公钥验证签名 (需要实现)
        // boolean isValid = verifySignature(message, signature, platformCert.getPublicKey());

        // if (!isValid) {
        //     logger.warn("Invalid signature in notification.");
        //     return "FAIL";
        // }

        log.info("Signature verification passed. (This is a placeholder, implement real logic!)");
    }
}
