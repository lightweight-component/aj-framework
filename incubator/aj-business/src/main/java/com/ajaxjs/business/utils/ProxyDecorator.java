package com.ajaxjs.business.utils;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 实现接口<I>实例<T>的代理类 <br>
 * 应用层可以根据需要继承此类重写{@link #invoke(Object, Method, Object[])}方法
 * <p>
 * 可以简单的理解decorator pattern的类是一个代理类，它可以转发所有的方法请求到被代理的实例。
 * 并在此基础上实现一些新特性，但对外表现上，代理类与被代理类的行为和功能是一样的。
 * 设计一个类的代理类，很简单，就是通过转发实现被代理类的所有方法，但如果要经常写这样的代理类，是件挺烦人的事儿。
 * 如果被代理的类是一个接口，那么完全可以基于Proxy和InvocationHandler实现一个基本通用的接口代理类，
 * 在特定应用场景下可以省去手工写代码的工作。如下是一个基于Proxy和InvocationHandler实现的一个接口代理类，
 * 它只是简单的转发方法请求到被代理的接口实例，实际使用时可以继承该类根据需要重写invoke方法来添加新的行为特性.
 *
 * <a href="https://blog.csdn.net/10km/article/details/88926179">...</a>
 *
 * @param <I> 接口类型
 * @param <T> 接口实现类型
 * @author guyadong
 */
public class ProxyDecorator<I, T> implements InvocationHandler {
    private final Class<I> interfaceClass;

    protected final T delegate;

    /**
     * 构造方法
     *
     * @param interfaceClass 接口类
     * @param delegate       实现接口的类
     */
    public ProxyDecorator(Class<I> interfaceClass, T delegate) {
        if (null == interfaceClass)
            throw new NullPointerException();

        if (null == delegate)
            throw new NullPointerException();

        if (!(interfaceClass.isInterface() && interfaceClass.isInstance(delegate)))
            throw new IllegalArgumentException("delegate must implement interfaceClass");

        this.interfaceClass = interfaceClass;
        this.delegate = delegate;
    }

    /**
     * 简化版构造函数<br>
     * 当delegate只实现了一个接口时，自动推断接口类型
     *
     * @param delegate
     */
    @SuppressWarnings("unchecked")
    public ProxyDecorator(T delegate) {
        if (null == delegate)
            throw new NullPointerException();

        if (delegate.getClass().getInterfaces().length != 1)
            throw new IllegalArgumentException(
                    String.format("can't determines interface class from %s", delegate.getClass().getName()));

        this.interfaceClass = (Class<I>) delegate.getClass().getInterfaces()[0];
        this.delegate = delegate;
    }

    /**
     * 转发所有接口方法请求到接口实例(delegate),实际使用时可以根据需要重写此方法来添加新的行为特性
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(delegate, args);
    }

    /**
     * 返回代理的接口类
     *
     * @return
     */
    public final Class<I> getInterfaceClass() {
        return interfaceClass;
    }

    /**
     * 返回代理的接口实例
     *
     * @return
     */
    public final T getDelegate() {
        return delegate;
    }

    /**
     * 根据当前对象创建新的接口实例{@link Proxy}
     *
     * @return
     */
    public final I proxyInstance() {
        return interfaceClass
                .cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, this));
    }
}
