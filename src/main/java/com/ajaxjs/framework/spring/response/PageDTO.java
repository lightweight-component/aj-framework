package com.ajaxjs.framework.spring.response;

import com.ajaxjs.data.PageResult;
import com.ajaxjs.framework.IBaseModel;
import lombok.Data;

@Data
public class PageDTO implements IBaseModel {
    private PageResult<?> rows;

    private Integer total;
}
