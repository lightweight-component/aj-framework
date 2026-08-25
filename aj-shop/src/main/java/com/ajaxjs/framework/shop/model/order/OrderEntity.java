package com.ajaxjs.framework.shop.model.order;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.framework.shop.model.payment.PaymentStatus;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Order entity
 */
@Data
@Table("shop_order")
@EqualsAndHashCode(callSuper = true)
public class OrderEntity extends BaseModelV2 {
    /**
     * 订单号，业务唯一标识
     */
    private String orderNo;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 支付渠道 id，例如支付宝、微信支付等
     */
    private Long payChannelId;

    /**
     * 订单状态
     */
    private OrderStatus status;

    /**
     * 支付状态
     */
    private PaymentStatus paymentStatus;

    /**
     * 配送方式
     */
    private DeliveryMethod deliveryMethod;

    /**
     * 商品总金额（SKU * 数量 * 单价的汇总） (单位: 分)
     */
    private Integer goodsAmount;

    /**
     * 优惠金额 (单位: 分)
     */
    private Integer discountAmount;

    /**
     * 运费 (单位: 分)
     */
    private Integer freightAmount;

    /**
     * 税费 (单位: 分)
     */
    private Integer taxAmount;

    /**
     * 最终实付金额（goods_amount - discount_amount + freight_amount + tax_amount） (单位: 分)
     */
    private Integer finalAmount;

    /**
     * 已支付金额 (单位: 分)
     */
    private Integer paidAmount;

    /**
     * 已退款金额 (单位: 分)
     */
    private Integer refundAmount;

    /**
     * 关联到用户地址簿的 ID
     */
    private String addressId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverPhone;

    /**
     * 收货人详细地址
     */
    private String receiverDetailAddress;

    /**
     * 订单创建时间
     */
    private Date createTime;

    /**
     * 支付成功时间
     */
    private Date payTime;

    /**
     * 发货时间
     */
    private Date sendTime;

    /**
     * 订单完成时间
     */
    private Date finishTime;

    /**
     * 订单取消时间
     */
    private Date cancelTime;

    /**
     * 订单过期时间（用于未支付订单自动取消）
     */
    private Date expireTime;

    /**
     * 快递公司名称
     */
    private String expressCompany;

    /**
     * 快递单号
     */
    private String trackingNumber;

    /**
     * 物流状态
     */
    private LogisticsStatus logisticsStatus;

    /**
     * 用户订单备注
     */
    private String remark;

    /**
     * 商家内部备注
     */
    private String sellerRemark;

    /**
     * 订单来源渠道（如：APP、小程序、H5、PC）
     */
    private SourceChannel sourceChannel;

    /**
     * 促销信息（JSON 字段，记录使用的优惠券、活动信息等）
     */
    private String promotionInfo;

    /**
     * 发票 id
     */
    private Long invoiceId;

    /**
     * 风控等级（用于标记可疑订单）
     */
    private Integer riskLevel;

    /**
     * 父订单 ID（用于拆单、合并支付等场景）
     */
    private Long parentOrderId;

    /**
     * 乐观锁版本号（用于并发控制）
     */
    private Integer version;
}
