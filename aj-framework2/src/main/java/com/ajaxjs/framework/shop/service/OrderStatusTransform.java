package com.ajaxjs.framework.shop.service;

import lombok.extern.slf4j.Slf4j;

/**
 * 通过状态机严格控制订单状态流转
 * 每个状态变更都对应特定的Handler处理业务逻辑
 *
 * 语法糖甜不甜？巧用枚举实现“状态”转换限制
 * <a href="https://cloud.tencent.com/developer/article/1883946">...</a>
 */
@Slf4j
public enum OrderStatusTransform implements IOrderStatusTransform {
    PENDING_PAYMENT {
        @Override
        public boolean canChange(OrderStatusTransform orderStatus) {
            switch (orderStatus) {
                case PAY:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void printOrderStatus() {
            log.info("待支付");
        }
    },
    PAY {
        @Override
        public boolean canChange(OrderStatusTransform orderStatus) {
            //因为退款接口一般都会有延迟，所以会先转化为“退款中”状态
            switch (orderStatus) {
                case REFUNDING:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void printOrderStatus() {
            log.info("支付");
        }
    },
    REFUNDING {
        @Override
        public boolean canChange(OrderStatusTransform orderStatus) {
            switch (orderStatus) {
                case REFUNDED:
                case FAIL_REFUNDED:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void printOrderStatus() {
            log.info("退款中");
        }
    },
    REFUNDED {
        @Override
        public void printOrderStatus() {
            log.info("退款成功");
        }
    },

    FAIL_REFUNDED {
        @Override
        public void printOrderStatus() {
            log.info("退款失败");
        }
    },
    ;

    //自定义转换方法
    public boolean canChange(OrderStatusTransform orderStatus) {
        return false;
    }
}
