package com.ajaxjs.mvc;

import static org.mockito.Mockito.mock;

import java.io.StringWriter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 方便测试的基础类
 *
 * @author sp42 frank@ajaxjs.com
 */
public abstract class BaseControllerTest {
    public static FilterChain chain;

    // 单测技巧，每个 url 对应一个 request、一个 response
    public HttpServletRequest request;

    public HttpServletResponse response;

    public StringWriter writer;

    public ServletOutputStream os;

    /**
     * 控制器的包名
     *
     * @param packageName 包名
     */
    public void init(String packageName) {
        chain = mock(FilterChain.class);
//        ComponentMgr.scan(packageName);
//        MvcDispatcherBase.init(null);
    }
}