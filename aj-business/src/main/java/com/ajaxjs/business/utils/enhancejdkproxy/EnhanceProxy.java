package com.ajaxjs.business.utils.enhancejdkproxy;

import java.lang.reflect.Proxy;

/**
 * 增强的 JDK 代理
 * 提供静态方法来创建代理类实例
 * <a href="https://blog.csdn.net/qq_18515155/article/details/118031761">...</a>
 * <a href="https://blog.csdn.net/qq_18515155/article/details/117334651">...</a>
 **/
public class EnhanceProxy {
    /**
     * 创建一个新的代理类实例
     *
     * @param loader     代理类的类加载器
     * @param interfaces 代理类实现的接口列表
     * @param h          代理类的调用处理器
     * @return 返回创建的代理类实例
     */
    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, EnhanceInvocationHandler h) {
        return Proxy.newProxyInstance(loader, interfaces, h);
    }
}
