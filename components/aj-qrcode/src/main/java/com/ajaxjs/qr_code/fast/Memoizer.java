/*
 * Fast QR Code generator library
 *
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/fast-qr-code-generator-library
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

package com.ajaxjs.qr_code.fast;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A thread-safe cache based on soft references.
 *
 * @param <T>
 * @param <R>
 */
public final class Memoizer<T, R> {
    private final Function<T, R> function;
    Map<T, SoftReference<R>> cache = new ConcurrentHashMap<>();
    private Set<T> pending = new HashSet<>();

    /**
     * 创建一个基于给定函数的 memoizer 函数，该函数接受一个输入来计算输出。
     * Creates a memoizer based on the given function that takes one input to compute an output.
     *
     * @param func 函数
     */
    public Memoizer(Function<T, R> func) {
        function = func;
    }

    /**
     * 计算 function.apply(arg) 的结果，或者返回之前调用的缓存副本。
     * Computes function.apply(arg) or returns a cached copy of a previous call.
     * <p>
     * 根据给定的参数获取计算结果，如果结果已缓存，则直接返回缓存结果；否则，进行计算并缓存结果后返回。
     * 使用软引用缓存机制，以在内存不足时自动清除缓存，避免内存泄露。
     * 使用同步机制确保线程安全，避免并发计算相同参数的情况。
     *
     * @param arg 函数的输入参数，用于计算和作为缓存的键
     * @return 函数的计算结果
     */
    public R get(T arg) {
        // 尝试非阻塞方式从缓存中获取结果。Non-blocking fast path
        {
            SoftReference<R> ref = cache.get(arg);
            if (ref != null) {
                R result = ref.get();
                if (result != null) return result;
            }
        }

        // 如果缓存中没有结果，则进入阻塞方式获取结果。Sequential slow path
        while (true) {
            synchronized (this) {
                SoftReference<R> ref = cache.get(arg);

                if (ref != null) {// 再次尝试从缓存中获取结果，如果成功且结果有效，则返回结果。
                    R result = ref.get();
                    if (result != null) return result;
                    cache.remove(arg);  // 如果结果无效，则从缓存中移除该条目。
                }

                assert !cache.containsKey(arg);   // 确保在缓存中不存在当前参数的条目。

                if (pending.add(arg)) break; // 尝试将当前参数标记为正在计算中，如果成功，则退出循环开始计算。

                try { // 如果当前参数已经被标记为正在计算中，则等待其他线程完成计算。
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); // 将中断异常转换为运行时异常抛出。
                }
            }
        }

        try { // 执行实际的计算，并将结果缓存起来。
            R result = function.apply(arg);
            cache.put(arg, new SoftReference<>(result));

            return result;
        } finally {
            synchronized (this) {// 计算完成后，移除正在计算中的标记，并唤醒其他等待线程。
                pending.remove(arg);
                this.notifyAll();
            }
        }
    }

}
