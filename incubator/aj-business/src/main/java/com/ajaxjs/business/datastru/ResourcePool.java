package com.ajaxjs.business.datastru;


import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 资源池管理对象<br>
 * <a href="https://blog.csdn.net/10km/article/details/79277610">...</a>
 * {@link #apply()},{@link #free()}用于申请/释放资源,申请的资源对象不可跨线程调用,<br>
 * 通过重写{@link #isNestable()}方法决定是否允许嵌套调用
 * <p>
 * 假有这样一个需求：
 * <p>
 * 有一组类型为R固定数目的资源对象，多个线程在使用资源对象r时需要申请取用一个资源对象，用完后还回以便其他线程使用。
 * <p>
 * 这个需求很简单，用commons-pool就可以实现，但仅为了这个需求就增加一个jar依赖，有点不划算，所以我基于LinkedBlockingQueue设计了一个资源池对象(resource
 * pool)对象来实现这个需求。
 * 资源池对象有两个基本的方法apply()/free()分别用于申请和释放资源。用一个LinkedBlockingQueue类型的queue来保存空闲的资源对象
 * <p>
 * apply() 从资源队列queue中申请一个资源，如果队列为空，线程阻塞，否则就从队列头部取出一个对象，保存在TLS变量中 free()
 * 归还资源对象，将TLS变量中保存的资源对象重新加入queue尾部。
 * <p>
 * apply()/free()必须成对使用
 *
 * @param <R> 资源类型
 * @author guyadong
 */
public class ResourcePool<R> {
    /**
     * 资源队列
     */
    protected final LinkedBlockingQueue<R> queue = new LinkedBlockingQueue<>();

    /**
     * 当前线程申请的资源对象
     */
    private final ThreadLocal<R> tlsResource = new ThreadLocal<>();

    /**
     * 线程嵌套计数
     */
    private final ThreadLocal<Integer> threadNestCount = new ThreadLocal<>();
    private final boolean nestable = isNestable();

    protected ResourcePool() {
    }

    /**
     * 构造方法
     *
     * @param resources 资源对象集合
     * @throws IllegalArgumentException 包含{@code null}元素
     */
    public ResourcePool(Collection<R> resources) {
        for (R r : resources) {
            if (null == r)
                throw new IllegalArgumentException("resources contains null element");

            queue.add(r);
        }
    }

    @SafeVarargs
    public ResourcePool(R... resources) {
        this(Arrays.asList(resources));
    }

    /**
     * 从资源队列{@link #queue}中取出一个对象,保存到{@link #tlsResource}
     *
     * @return
     * @throws InterruptedException
     */
    private R getResource() throws InterruptedException {
        if (null != tlsResource.get())
            // 资源状态异常
            throw new IllegalStateException("INVALID tlsResource state");

        R r;
        if (queue.isEmpty() && null != (r = newResource()))
            queue.offer(r);

        r = open(queue.take());
        tlsResource.set(r);

        return r;
    }

    /**
     * 将{@link #tlsResource}中的资源对象重新加入资源队列{@link #queue},并清除TLS变量{@link #tlsResource}
     */
    private void recycleResource() {
        R r = tlsResource.get();
        if (null == r)
            // 资源状态异常
            throw new IllegalStateException("INVALID tlsResource while recycle");

        // 放回队例
        queue.offer(close(r));
        tlsResource.remove();
    }

    /**
     * (阻塞式)申请当前线程使用的资源对象,不可跨线程使用
     *
     * @return
     * @throws InterruptedException
     */
    public final R applyChecked() throws InterruptedException {
        if (nestable) {
            Integer count = threadNestCount.get();

            if (null == count) {
                // 当前线程第一次申请资源
                count = 1;
                threadNestCount.set(count);

                return getResource();
            } else {
                // 嵌套调用时直接返回TLS变量
                if (null == this.tlsResource.get())
                    // 资源状态异常
                    throw new IllegalStateException("INVALID tlsResource");

                ++count;
                return this.tlsResource.get();
            }
        } else
            return getResource();
    }

    /**
     * (阻塞式)申请当前线程使用的资源对象,不可跨线程使用<br>
     * {@link InterruptedException}封装到{@link RuntimeException}抛出
     *
     * @return
     * @see #applyChecked()
     */
    public final R apply() {
        try {
            return applyChecked();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 释放当前线程占用的资源对象，放回资源队列
     */
    public final void free() {
        if (nestable) {
            Integer count = threadNestCount.get();
            if (null == count) {
                // 申请/释放没有成对调用
                throw new IllegalStateException("INVALID nestCount");
            }
            if (0 == (--count)) {
                threadNestCount.remove();
                recycleResource();
            }
        } else {
            recycleResource();
        }
    }

    /**
     * 是否允许嵌套
     */
    protected boolean isNestable() {
        return false;
    }

    /**
     * 创建一个新的资源对象
     *
     * @return
     */
    protected R newResource() {
        return null;
    }

    /**
     * 资源从队形从取出时调用,子类可重写此方法
     *
     * @param resource
     * @return 返回 {@code resource
     */
    protected R open(R resource) {
        return resource;
    }

    /**
     * 资源对象放回队列时调用,子类可重写此方法
     *
     * @param resource
     * @return 返回 {@code resource}
     */
    protected R close(R resource) {
        return resource;
    }
}