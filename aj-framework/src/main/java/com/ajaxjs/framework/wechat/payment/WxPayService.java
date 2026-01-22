package com.ajaxjs.framework.wechat.payment;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.framework.wechat.merchant.MerchantConfig;
import com.ajaxjs.framework.wechat.merchant.SignerMaker;
import com.ajaxjs.framework.wechat.payment.model.PreOrder;
import com.ajaxjs.framework.wechat.payment.model.RequestPayment;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.cryptography.Constant;
import com.ajaxjs.util.cryptography.rsa.DoSignature;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.security.PrivateKey;
import java.util.Map;

/**
 * 小程序支付业务
 */
@Slf4j
@Data
public class WxPayService {
    @Autowired(required = false)
    private MerchantConfig mchCfg;

    @Value("${wechat.merchant.payNotifyUrl}")
    private String appletPayNotifyUrl;

    private final static String NATIVE_PAY_URL = "/v3/pay/transactions/native";

    /**
     * 发起 Native 支付请求
     *
     * @param appId       AppId
     * @param description 商品简单描述
     * @param outTradeNo  商户系统内部订单号
     * @param amount      金额，单位为分
     * @return 生成的二维码链接 (code_url)，或发生错误时返回 null
     */
    public String createNativePayment(String appId, String description, String outTradeNo, int amount) {
        PreOrder p = new PreOrder();
        p.setAppid(appId);
        p.setMchid(mchCfg.getMchId());
        p.setOut_trade_no(outTradeNo);
        p.setDescription(description);
        p.setNotify_url(appletPayNotifyUrl);

        Map<String, Object> params = JsonUtil.pojo2map(p);
        Map<String, Object> amountParams = ObjectHelper.mapOf("total", amount, "currency", "CNY");
        params.put("amount", amountParams);

        Map<String, Object> result = PayUtils.postMap(mchCfg, NATIVE_PAY_URL, params);

        if ((Boolean) result.get("isOk") && result.get("code") == null) {
            String codeUrl = (String) result.get("code_url");
            log.info("Generated QR Code URL: {}", codeUrl);

            return codeUrl;
        } else
            throw new BusinessException(result.get("message").toString());
    }

    /**
     * 下单
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_1.shtml">...</a>
     *
     * @param openId      用户 OpenId
     * @param totalMoney  交易金额，单位是 分
     * @param outTradeNo  订单号
     * @param description 描述
     * @return 预支付交易会话标识
     */
    public String preOrder(String openId, String appId, int totalMoney, String outTradeNo, String description) {
        Map<String, String> payer = ObjectHelper.mapOf("openid", openId);// 支付者
        Map<String, Integer> amount = ObjectHelper.mapOf("total", totalMoney); // 金额

        log.info(mchCfg.getMchId() + "::::::::" + appletPayNotifyUrl);

        if (!StringUtils.hasText(appletPayNotifyUrl))
            throw new IllegalArgumentException("appletPayNotifyUrl 不能为空");

        // 构建支付参数
        PreOrder p = new PreOrder();
        p.setAppid(appId);
        p.setMchid(mchCfg.getMchId());
        p.setOut_trade_no(outTradeNo);
        p.setDescription(description);
        p.setNotify_url(appletPayNotifyUrl);

        Map<String, Object> params = JsonUtil.pojo2map(p);
        params.put("amount", amount);
        params.put("payer", payer);
        params.put("settle_info", ObjectHelper.mapOf("profit_sharing", true));

        String url = "/v3/pay/transactions/jsapi";
        Map<String, Object> map = PayUtils.postMap(mchCfg, url, params);

        if ((Boolean) map.get("isOk") && map.get("code") == null)
            return map.get("prepay_id").toString();
        else
            throw new BusinessException(map.get("message").toString());
    }

    /**
     * 微信支付订单号查询订单
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_2.shtml">...</a>
     *
     * @param transactionId 微信支付订单号
     */
    public Map<String, Object> getOrderByTransactionId(String transactionId) {
        String url = "/v3/pay/transactions/id/" + transactionId + "?mchid=" + mchCfg.getMchId();

        return PayUtils.get(mchCfg, url, Map.class);
    }

    /**
     * 商户订单号查询订单
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_2.shtml">...</a>
     *
     * @param outTradeNo 商户订单号
     */
    public Map<String, Object> getOrderByOrderNo(String outTradeNo) {
        String url = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "?mchid=" + mchCfg.getMchId();

        return PayUtils.get(mchCfg, url, Map.class);
    }

    /**
     * 关闭订单
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_3.shtml">...</a>
     *
     * @param outTradeNo 商户订单号
     */
    public void closeOrder(String outTradeNo) {
        String url = "/v3/pay/transactions/out-trade-no/" + outTradeNo + "/close";
        Map<String, String> params = ObjectHelper.mapOf("mchid", mchCfg.getMchId());
        PayUtils.postMap(mchCfg, url, params);// 该接口是无数据返回的
    }

    /**
     * 传入预支付交易会话标识 id，生成小程序支付所需参数返回 package 修正，最后转换为 JSON 字符串
     *
     * @param prepayId 预支付交易会话标识
     * @return 小程序支付所需参数
     */
    public String getRequestPayment(String prepayId, String appId) {
        RequestPayment rp = new RequestPayment();
        rp.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000));
        rp.setNonceStr(RandomTools.generateRandomString(10));
        rp.setPrepayIdPackage("prepay_id=" + prepayId);

        String sign = getSign(mchCfg.getPrivateKey(), rp, appId);
        rp.setPaySign(sign);

        Map<String, Object> map = JsonUtil.pojo2map(rp);
        map.put("package", rp.getPrepayIdPackage());
        map.remove("prepayIdPackage");

        return JsonUtil.toJson(map);
    }

    private String getSign(String privateKey, RequestPayment rp, String appId) {
        String sb = appId + "\n" +
                rp.getTimeStamp() + "\n" +
                rp.getNonceStr() + "\n" +
                rp.getPrepayIdPackage() + "\n";

        PrivateKey key = SignerMaker.loadPrivateKeyByPath(privateKey);

        return new DoSignature(Constant.SHA256_RSA).setPrivateKey(key).setStrData(sb).signToString();
    }
}
