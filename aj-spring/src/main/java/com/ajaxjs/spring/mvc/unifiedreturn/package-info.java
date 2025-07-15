/**
 * Unified Return Object
 *   - 如果控制器方法返回的类型是 ResponseEntity<T>，那么好，很简单，直接处理返回这个对象；
 *   - 如果控制器方法没有返回 ResponseEntity<T>，则最终统一返回机制会自动加上。于是你的控制器返回的类型可以直接是 String/int/long/boolean/void/Object/Map/List/Array 等任意类型，当然也包括 Java Bean（POJO）；
 *   - 如果控制器方法返回的对象实现了接口 ``，那么表示这是一个自定义的返回对象，那么也简单，直接处理返回这个对象。这种适合比较特殊的返回结构，数量不是很多的
 *   - 如果数量太多，希望是全局自定义返回对象的，我们也允许。同时可以达到第二点的效果，即忽略声明容器类，统一返回机制会自动加上。
 */
package com.ajaxjs.spring.mvc.unifiedreturn;