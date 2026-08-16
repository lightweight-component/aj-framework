package com.ajaxjs.wechat.payment;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.wechat.merchant.MerchantConfig;
import com.ajaxjs.wechat.payment.model.RefundResult;
import com.ajaxjs.wechat.payment.model.RefundNotifyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 退款处理
 */
@Slf4j
@Service
public class RefundService {
    @Autowired(required = false)
    private MerchantConfig mchCfg;

    @Value("${wechat.merchant.refundNotifyUrl}")
    private String refundNotifyUrl;

    /**
     * 申请退款 <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_9.shtml">...</a>
     *
     * @param transactionId 微信支付订单号
     * @param refundMoney   退款金额
     * @param totalMoney    原订单金额
     * @param outRefundNo   商户退款单号
     * @param reason        退款原因
     */
    public RefundResult refund(String transactionId, long refundMoney, long totalMoney, String outRefundNo, String reason) {
        Map<String, Object> params = new HashMap<>();
        params.put("transaction_id", transactionId);
        params.put("notify_url", refundNotifyUrl);
        params.put("out_refund_no", outRefundNo);
        params.put("reason", reason);
        params.put("amount", ObjectHelper.mapOf("refund", refundMoney, "total", totalMoney, "currency", "CNY"));

        String url = "/v3/refund/domestic/refunds";
        Map<String, Object> map = PayUtils.postMap(mchCfg, url, params);
        RefundResult bean = JsonUtil.map2pojo(map, RefundResult.class);
        log.info("RefundResult -------------- {}", bean);
        // RefundResultAmount 对象对不上
//        RefundResultAmount amount = JsonUtil.map2pojo((Map<String, Object>) map.get("amount"), RefundResultAmount.class);
//        bean.setAmount(amount);

        return bean;
    }

    private final static String SUCCESS = "REFUND.SUCCESS";

    /**
     * 用户退款后，微信通知我们的接口。
     * 客户端无须调用该接口，该接口由微信支付中心调用
     *
     * @param params 回调参数
     * @return 支付结果
     */
    public RefundNotifyResult payCallback(Map<String, Object> params) {
        if (params.containsKey("event_type") && SUCCESS.equals(params.get("event_type"))) {
            String cert = PayCallback.decrypt(params, mchCfg.getApiV3Key()); // 支付成功

            // 官方返回的 JSON 是嵌套的，现在将其扁平化
            Map<String, Object> map = JsonUtil.json2map(cert);
            RefundNotifyResult bean = JsonUtil.map2pojo(map, RefundNotifyResult.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> amount = (Map<String, Object>) map.get("amount");
            bean.setTotal((int) amount.get("total"));
            bean.setRefund((int) amount.get("refund"));
            bean.setPayer_total((int) amount.get("payer_total"));
            bean.setPayer_refund((int) amount.get("payer_refund"));

            log.info("Parsed notification - {}", bean);

            return bean;
        }

        throw new IllegalArgumentException("返回参数失败！");
    }

    /**
     * 查询单笔退款
     * <a href="https://pay.weixin.qq.com/wiki/doc/apiv3/apis/chapter3_5_10.shtml">...</a>
     *
     * @param outRefundNo 商户退款单号日期
     * @return 退款结果
     */
    public Map<String, Object> queryRefund(String outRefundNo) {
        String url = "/v3/refund/domestic/refunds/" + outRefundNo;

        return PayUtils.get(mchCfg, url, Map.class);
    }
}