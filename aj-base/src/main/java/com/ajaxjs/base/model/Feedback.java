package com.ajaxjs.base.model;

import com.ajaxjs.framework.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 留言反馈
 *
 * @author sp42 frank@ajaxjs.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Feedback extends BaseModel {
    private String name;

    private Long userId;

    private String phone;

    private String email;

    private String feedback;

    private String contact;

    private String content;
}
