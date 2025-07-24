package com.ajaxjs.business.utils.enhancejdkproxy;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * JoinPointInfo 类用于封装连接点信息，包括方法、参数、目标对象和代理对象
 * 主要用于在 AOP（面向切面编程）中记录和传递方法执行时的相关信息
 */
@Data
public class JoinPointInfo {
    /**
     * 正在被拦截的 方法对象
     */
    private Method method;

    /**
     * 方法的参数数组
     */
    private Object[] args;

    /**
     * 目标对象，即被代理的实际对象
     */
    private Object target;

    /**
     * 代理对象，用于执行代理行为的对象
     */
    private Object proxy;

    /**
     * Builder 类用于构建 JoinPointInfo 对象
     * 提供了一种链式调用的方式来设置各个属性，最后通 过build 方法创建 JoinPointInfo 实例
     */
    public static final class Builder {
        // Builder模式的私有构造方法，防止外部直接实例化
        private Method method;
        private Object[] args;
        private Object target;
        private Object proxy;

        // Builder模式的静态工厂方法，提供外部获取Builder实例的方式
        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder args(Object[] args) {
            this.args = args;
            return this;
        }

        public Builder target(Object target) {
            this.target = target;
            return this;
        }

        public Builder proxy(Object proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * 构建 JoinPointInfo 对象的方法
         * 将 Builder 内部设置的属性值赋给新创建的 JoinPointInfo 对象
         *
         * @return JoinPointInfo 对象，包含 method、args、target 和 proxy 信息
         */
        public JoinPointInfo build() {
            JoinPointInfo joinPointInfo = new JoinPointInfo();
            joinPointInfo.args = this.args;
            joinPointInfo.target = this.target;
            joinPointInfo.method = this.method;
            joinPointInfo.proxy = this.proxy;

            return joinPointInfo;
        }
    }
}
