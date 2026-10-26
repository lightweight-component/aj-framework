package com.ajaxjs.security.antireptile.servlet;

import com.ajaxjs.security.antireptile.util.CrosUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 接收验证码答案 POST 请求的 Servlet。
 * <p>首次请求时获取业务服务，将表单交给服务校验，以 HTTP 200 返回 JSON 布尔结果，
 * 并设置组件统一的跨域响应头；答案正确与否由响应体表示。</p>
 */
public class ValidateFormServlet extends HttpServlet {
    /**
     * 从 Spring Web 上下文获取的验证码业务服务。
     */
    private ValidateFormService validateFormService;

    /**
     * 是否已完成首次请求触发的初始化。
     */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    private synchronized void init(ServletContext servletContext) {
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        assert ctx != null;
        this.validateFormService = ctx.getBean(ValidateFormService.class);
    }

    /**
     * 校验 multipart 或普通表单中的验证码答案，并写入 JSON 校验结果。
     * <p>首次调用时初始化业务服务；正常处理统一返回 HTTP 200 并关闭响应输出流。</p>
     *
     * @param request  含 verifyId、result、realRequestUri 字段的表单请求
     * @param response 用于输出 UTF-8 JSON 结果的响应
     * @throws IOException 响应写入失败或表单字段解码失败时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!initialized.get()) {
            init(request.getServletContext());
            initialized.set(true);
        }

        String result = validateFormService.validate(request);
        CrosUtil.setCrosHeader(response);
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(result);
        response.getWriter().close();
    }
}
