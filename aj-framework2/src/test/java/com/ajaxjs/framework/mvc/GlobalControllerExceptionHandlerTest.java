package com.ajaxjs.framework.mvc;

import com.ajaxjs.framework.mvc.unifiedreturn.UnifiedResponseHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalControllerExceptionHandlerTest {
    @AfterEach
    void cleanup() {
        GlobalExceptionHandler.EXCEPTION_HOLDER.remove();
    }

    private static MethodArgumentNotValidException invalid(String... messages) throws Exception {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "input");
        for (String message : messages)
            errors.addError(new FieldError("input", "password", "secret-must-not-leak", false, null, null, message));
        errors.addError(new ObjectError("input", "对象校验失败"));
        return new MethodArgumentNotValidException(
                new MethodParameter(Endpoint.class.getMethod("invalid"), -1), errors);
    }

    @Test
    void collectsFieldAndObjectMessagesWithoutRejectedValues() throws Exception {
        MethodArgumentNotValidException error = invalid("请输入名称。", "请输入名称。", null, " ");
        var result = new GlobalControllerExceptionHandler().handleValidation(error);
        assertEquals(0, result.getStatus());
        assertEquals("400", result.getErrorCode());
        assertEquals("请输入名称。；对象校验失败", result.getMessage());
        assertSame(error, GlobalExceptionHandler.EXCEPTION_HOLDER.get());
    }

    @Test
    void fallsBackWhenNoUsableMessages() throws Exception {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(new Object(), "input");
        errors.addError(new ObjectError("input", " "));
        var error = new MethodArgumentNotValidException(
                new MethodParameter(Endpoint.class.getMethod("invalid"), -1), errors);
        assertEquals("请求字段校验失败。", new GlobalControllerExceptionHandler().handleValidation(error).getMessage());
    }

    @Test
    void mvcUsesSpecificHandlerAndDoesNotDoubleWrapResponse() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Endpoint())
                .setControllerAdvice(new GlobalControllerExceptionHandler(), new UnifiedResponseHandler()).build();
        mvc.perform(get("/invalid").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(0))
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.message").value("请输入名称。；对象校验失败"))
                .andExpect(jsonPath("$.data").isEmpty());
        assertInstanceOf(MethodArgumentNotValidException.class, GlobalExceptionHandler.EXCEPTION_HOLDER.get());
        mvc.perform(get("/other"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorCode").value("400"))
                .andExpect(jsonPath("$.message").value("参数错误"));
    }

    @RestController
    public static class Endpoint {
        @GetMapping("/invalid")
        public Object invalid() throws Exception {
            throw GlobalControllerExceptionHandlerTest.invalid("请输入名称。");
        }

        @GetMapping("/other")
        public Object other() {
            throw new IllegalArgumentException("参数错误");
        }
    }
}
