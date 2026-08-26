package com.ajaxjs.pay.qq;

import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Post;
import lombok.Data;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Data
public class QqPay implements ApiUrl {
    /**
     * 证书文件目录
     */
    String cerPath;

    /**
     * 证书密码
     */
    String certPass;

    /**
     * 提交付款码支付
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String microPay(Map<String, String> params) {
        return doPost(MICRO_PAY_URL, params);
    }

    /**
     * 统一下单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String unifiedOrder(Map<String, String> params) {
        return doPost(UNIFIED_ORDER_URL, params);
    }

    /**
     * 订单查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderQuery(Map<String, String> params) {
        return doPost(ORDER_QUERY_URL, params);
    }

    /**
     * 关闭订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String closeOrder(Map<String, String> params) {
        return doPost(CLOSE_ORDER_URL, params);
    }

    /**
     * 撤销订单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params) {
        return doPost(ORDER_REVERSE_URL, params);
    }

    /**
     * 撤销订单
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @return {@link String} 请求返回的结果
     */
    public String orderReverse(Map<String, String> params, InputStream certFile) {
        return doPost(ORDER_REVERSE_URL, params, certFile);
    }

    /**
     * 申请退款
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(Map<String, String> params) {
        return doPost(ORDER_REFUND_URL, params);
    }

    /**
     * 申请退款
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @return {@link String} 请求返回的结果
     */
    public String orderRefund(Map<String, String> params, InputStream certFile) {
        return doPost(ORDER_REFUND_URL, params, certFile);
    }

    /**
     * 退款查询
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String refundQuery(Map<String, String> params) {
        return doPost(REFUND_QUERY_URL, params);
    }

    /**
     * 对账单下载
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadBill(Map<String, String> params) {
        return doPost(DOWNLOAD_BILL_URL, params);
    }

    /**
     * 创建现金红包
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String createReadPack(Map<String, String> params) {
        return doPost(CREATE_READ_PACK_URL, params);
    }

    /**
     * 创建现金红包
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @return {@link String} 请求返回的结果
     */
    public String createReadPack(Map<String, String> params, InputStream certFile) {
        return doPost(CREATE_READ_PACK_URL, params, certFile);
    }

    /**
     * 查询红包详情
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String getHbInfo(Map<String, String> params) {
        return doPost(GET_HB_INFO_URL, params);
    }

    /**
     * 下载红包对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadHbBill(Map<String, String> params) {
        return doPost(DOWNLOAD_HB_BILL_URL, params);
    }


    /**
     * 企业付款到余额
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String transfer(Map<String, String> params) {
        return doPost(TRANSFER_URL, params);
    }

    /**
     * 企业付款到余额
     *
     * @param params   请求参数
     * @param certFile 证书文件的 InputStream
     * @return {@link String} 请求返回的结果
     */
    public String transfer(Map<String, String> params, InputStream certFile) {
        return doPost(TRANSFER_URL, params, certFile);
    }

    /**
     * 查询企业付款
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String getTransferInfo(Map<String, String> params) {
        return doPost(GET_TRANSFER_INFO_URL, params);
    }

    /**
     * 下载企业付款对账单
     *
     * @param params 请求参数
     * @return {@link String} 请求返回的结果
     */
    public String downloadTransferBill(Map<String, String> params) {
        return doPost(DOWNLOAD_TRANSFER_BILL_URL, params);
    }

    public String doPost(String url, Map<String, String> params) {
        String xml = MapTool.mapToXml(params);
        Post request = new Post(url, xml.getBytes(StandardCharsets.UTF_8), HttpConstant.CONTENT_TYPE_XML,
                connection -> connection.setRequestProperty(HttpConstant.CONTENT_TYPE, HttpConstant.CONTENT_TYPE_XML));

        return request.getResp().getResponseText();
    }

    public String doPost(String url, Map<String, String> params, String certPath) {
        return null; //todo
//        return Post.api(url, MapTool.mapToXml(params), certPath, certPass);
    }

    public String doPost(String url, Map<String, String> params, InputStream certFile) {
        return null; //todo
//        return Post.api(url, MapTool.mapToXml(params), certFile, certPass);
    }
}
