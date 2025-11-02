package com.ajaxjs.framework.dataservice.model;

import lombok.Data;

/**
 * A group has many endpoints.
 */
@Data
public class Group {
    Integer id;

    String url;

    String name;
}
