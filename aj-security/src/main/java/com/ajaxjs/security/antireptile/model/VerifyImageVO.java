package com.ajaxjs.security.antireptile.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 供验证页面使用的验证码展示对象。
 * <p>只包含标识、类型和图片数据，不携带正确答案。当前刷新接口实际仅输出标识和图片字段。</p>
 */
@Data
public class VerifyImageVO implements Serializable {
    /**
     * Java 序列化版本标识。
     */
    @Serial
    private static final long serialVersionUID = 345634706484343777L;

    /**
     * 验证码唯一标识，用于关联本地缓存中的正确答案。
     */
    private String verifyId;

    /**
     * 验证码类型预留字段，当前图片生成流程未赋值。
     */
    private String verifyType;

    /**
     * 包含 Data URI 前缀的 Base64 图片字符串，可用于页面图片地址。
     */
    private String verifyImgStr;
}
