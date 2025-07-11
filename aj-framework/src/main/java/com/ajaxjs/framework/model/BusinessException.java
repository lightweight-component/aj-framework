package com.ajaxjs.framework.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义的业务异常
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    public static final long serialVersionUID = -6735897190745766930L;
    /**
     * 自定义的错误代码
     */
    private String errCode;

    /**
     * 创建一个业务异常
     *
     * @param msg 业务异常的信息
     */
    public BusinessException(String msg) {
        super(msg);
        this.errCode = "500";
    }
}
