package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

/**
 * BeanAttributeValue
 */
@Data
public class BeanAttributeValue {
    private boolean isCompositeData;

    private Object data;
}
