package com.ajaxjs.framework.mvc;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.framework.mvc.unifiedreturn.ResponseResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

import static com.ajaxjs.framework.mvc.GlobalExceptionHandler.EXCEPTION_HOLDER;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResultWrapper handleValidation(MethodArgumentNotValidException error) {
        EXCEPTION_HOLDER.set(error);
        String message = error.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining("；"));

        if (!StringUtils.hasText(message))
            message = "请求字段校验失败。";

        return new ResponseResultWrapper().setStatus(0).setErrorCode("400").setMessage(message);
    }

    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest request, Exception e) {
        log.warn("ERROR>>", e);
        log.info("未知异常，请求地址：{}, 错误信息：{}", request.getRequestURI(), e.getMessage());

        ResponseResultWrapper resultWrapper = new ResponseResultWrapper();
        resultWrapper.setStatus(0);

        Throwable _ex = e.getCause() != null ? e.getCause() : e;
        String msg = _ex.getMessage();

        if (msg == null)
            msg = _ex.toString();

        EXCEPTION_HOLDER.set(_ex);

        if (_ex instanceof BusinessException) {
            BusinessException b = (BusinessException) _ex;
            resultWrapper.setErrorCode(StringUtils.hasText(b.getErrCode()) ? b.getErrCode() : "500");
//        }
//        else if (_ex instanceof ICustomException) {
//            int errCode = ((ICustomException) _ex).getErrCode();
//            resultWrapper.setErrorCode(String.valueOf(errCode));
//            resp.setStatus(errCode);
        } else if (_ex instanceof IllegalArgumentException) {// 客户端请求参数错误
            resultWrapper.setErrorCode("400");
//            resp.setStatus(HttpStatus.BAD_REQUEST.value());
        } else if (_ex instanceof SecurityException || _ex instanceof IllegalAccessError || _ex instanceof IllegalAccessException) {// 设置状态码
            resultWrapper.setErrorCode("403");
//            resp.setStatus(HttpStatus.FORBIDDEN.value());
        } else {
            resultWrapper.setErrorCode("500");
//            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        resultWrapper.setMessage(msg);
        MDC.clear();

        return resultWrapper;
    }
}
