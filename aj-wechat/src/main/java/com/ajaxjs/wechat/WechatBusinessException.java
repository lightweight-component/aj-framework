package com.ajaxjs.wechat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义的业务异常
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WechatBusinessException extends RuntimeException {
    /**
     * 自定义的错误代码
     */
    private String errCode;

    /**
     * 创建一个业务异常
     *
     * @param msg 业务异常的信息
     */
    public WechatBusinessException(String msg) {
        super(msg);
        this.errCode = "500";
    }
}
