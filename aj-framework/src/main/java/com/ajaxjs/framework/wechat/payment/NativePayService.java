package com.ajaxjs.framework.wechat.payment;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.framework.wechat.payment.model.PreOrder;
import com.ajaxjs.framework.wechat.merchant.MerchantConfig;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Native 支付
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class NativePayService extends CommonService {
    @Autowired(required = false)
    private MerchantConfig mchCfg;

    @Value("${wechat.merchant.payNotifyUrl}")
    private String appletPayNotifyUrl;

    private final static String PAY_URL = "/v3/pay/transactions/native";

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

        Map<String, Object> result = PayUtils.postMap(mchCfg, PAY_URL, params);

        if ((Boolean) result.get("isOk") && result.get("code") == null) {
            String codeUrl = (String) result.get("code_url");
            log.info("Generated QR Code URL: {}", codeUrl);

            return codeUrl;
        } else
            throw new BusinessException(result.get("message").toString());
    }
}
