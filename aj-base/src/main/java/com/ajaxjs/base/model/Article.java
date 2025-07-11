package com.ajaxjs.base.model;

import com.ajaxjs.framework.model.BaseModel;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图文
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("article")
public class Article extends BaseModel {
}
