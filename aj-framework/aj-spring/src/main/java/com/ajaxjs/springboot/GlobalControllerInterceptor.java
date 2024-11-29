package com.ajaxjs.springboot;

import com.ajaxjs.api.InterceptorAction;
import com.ajaxjs.api.limit.LimitAccess;
import com.ajaxjs.api.limit.LimitAccessVerify;
import com.ajaxjs.api.security.referer.HttpReferer;
import com.ajaxjs.api.security.referer.HttpRefererCheck;
import com.ajaxjs.api.time_signature.TimeSignature;
import com.ajaxjs.api.time_signature.TimeSignatureVerify;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * 全局的控制器拦截器
 */
@Slf4j
public class GlobalControllerInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (!(handler instanceof HandlerMethod))
            return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler; // 接口上的方法
        showControllerInfo(req, handlerMethod);
        Method method = handlerMethod.getMethod();

        if (!handler(TimeSignature.class, req, handlerMethod, method, TimeSignatureVerify.class))
            return false;

        if (!handler(LimitAccess.class, req, handlerMethod, method, LimitAccessVerify.class))
            return false;

        if (!handler(HttpReferer.class, req, handlerMethod, method, HttpRefererCheck.class))
            return false;

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> boolean handler(Class<? extends InterceptorAction<T>> serviceClz, HttpServletRequest req, HandlerMethod handlerMethod, Method method, Class<T> annotationType) {
        InterceptorAction<T> service = (InterceptorAction<T>) DiContextUtil.getBean(serviceClz);

        if (service == null)
            return true; // 对应的业务没有创建，放行


        Annotation annotation = null;

        if (service.isGlobalCheck()) {
            // 注解不能实例化，当前缺乏一个获取配置的手段O

        } else {
            Class<?> beanType = handlerMethod.getBeanType(); // or method.getDeclaringClass()
            Annotation classAnnotation = beanType.getAnnotation(annotationType);

            if (classAnnotation != null)
                annotation = classAnnotation;
            else {
                Annotation interfaceAnnotation = handlerMethod.getMethodAnnotation(annotationType);

                if (interfaceAnnotation != null)
                    annotation = interfaceAnnotation;
                else
                    annotation = method.getAnnotation(annotationType);
            }
        }
        if (annotation != null) {
            T a = (T) annotation;
            log.info(String.valueOf(a));

            return service.action(a, req);
        }

        return true;
    }

    /**
     * 获得 Controller 方法名、请求参数和注解信息
     *
     * @param req           请求对象
     * @param handlerMethod 方法
     */
    private static void showControllerInfo(HttpServletRequest req, HandlerMethod handlerMethod) {
        log.info("请求 URL：{} 对应的控制器方法：{}", req.getRequestURL(), handlerMethod);

        StringBuffer s = new StringBuffer();
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.isEmpty()) {
            for (String key : parameterMap.keySet())
                s.append(key).append("=").append(Arrays.toString(parameterMap.get(key))).append("\n");

            log.info("{} 请求参数：\n{}", req.getMethod(), s);
        }
    }
}