package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

import javax.management.MBeanAttributeInfo;

/**
 * BeanAttributeInfo
 */
@Data
public class BeanAttributeInfo {
    private String name;

    private BeanAttributeValue value;

    private MBeanAttributeInfo attributeInfo;
}
