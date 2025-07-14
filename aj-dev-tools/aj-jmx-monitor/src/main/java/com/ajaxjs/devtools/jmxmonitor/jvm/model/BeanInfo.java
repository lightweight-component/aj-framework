package com.ajaxjs.devtools.jmxmonitor.jvm.model;

import lombok.Data;

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import java.util.List;

/**
 * Bean 信息
 */
@Data
public class BeanInfo {
    private List<BeanAttributeInfo> beanAttributeInfos;

    private MBeanOperationInfo[] operationInfos;

    private MBeanNotificationInfo[] notificationInfos;
}
