package com.ajaxjs.message.email;


import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class EmailServiceImpl extends UnicastRemoteObject implements EmailRemoteService {
    public EmailServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public boolean sendEmail(Email email) throws RemoteException {
        MailWithConfig mailWithConfig = new MailWithConfig();
        copyProperties(email, mailWithConfig);

        return true;
    }

    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target must not be null");
        }

        try {
            // 获取源对象和目标对象的属性描述符
            Map<String, PropertyDescriptor> sourceDescriptors = getPropertyDescriptors(source.getClass());
            Map<String, PropertyDescriptor> targetDescriptors = getPropertyDescriptors(target.getClass());

            for (String propertyName : sourceDescriptors.keySet()) {
                if (targetDescriptors.containsKey(propertyName)) {
                    PropertyDescriptor sourceDescriptor = sourceDescriptors.get(propertyName);
                    PropertyDescriptor targetDescriptor = targetDescriptors.get(propertyName);

                    Method readMethod = sourceDescriptor.getReadMethod();
                    Method writeMethod = targetDescriptor.getWriteMethod();

                    // 如果读方法和写方法都存在，则进行拷贝
                    if (readMethod != null && writeMethod != null) {
                        Object value = readMethod.invoke(source); // 从源对象读取属性值
                        writeMethod.invoke(target, value);       // 将属性值写入目标对象
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy properties", e);
        }
    }

    // 获取类的所有属性描述符
    private static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws Exception {
        Map<String, PropertyDescriptor> descriptorMap = new HashMap<>();
        java.beans.BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(clazz);

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
            if (!"class".equals(descriptor.getName()))  // 排除 class 属性
                descriptorMap.put(descriptor.getName(), descriptor);
        }
        return descriptorMap;
    }
}
