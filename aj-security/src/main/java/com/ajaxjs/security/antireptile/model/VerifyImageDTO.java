package com.ajaxjs.security.antireptile.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 验证码生成与缓存流程使用的内部数据对象。
 * <p>同时携带图片、标识和正确答案；答案仅用于服务端存储与比对。
 * 向前端输出时应使用不含答案的 {@link VerifyImageVO} 或显式选择公开字段。</p>
 */
@Data
@AllArgsConstructor
public class VerifyImageDTO implements Serializable {
    /**
     * Java 序列化版本标识。
     */
    @Serial
    private static final long serialVersionUID = 6741944800448697513L;

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

    /**
     * 验证码正确答案，仅用于服务端缓存和校验，不应返回前端。
     */
    private String result;
}
