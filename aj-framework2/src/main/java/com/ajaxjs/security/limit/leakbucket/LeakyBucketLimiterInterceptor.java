package com.ajaxjs.security.limit.leakbucket;


import com.ajaxjs.security.SecurityInterceptor;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.ObjectHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class LeakyBucketLimiterInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 判断方法是否包含CounterLimit，有这个注解就需要进行限速操作
            if (handlerMethod.hasMethodAnnotation(LeakyBucketLimit.class)) {
                LeakyBucketLimit annotation = handlerMethod.getMethod().getAnnotation(LeakyBucketLimit.class);
                LeakyBucket leakyBucket = (LeakyBucket) DiContextUtil.getBean(annotation.limitClass());

                assert leakyBucket != null;
                boolean acquire = leakyBucket.acquire();
                response.setContentType("text/json;charset=utf-8");
                Map<String, String> result = ObjectHelper.mapOf("result", null);

                if (acquire) {
                    result.put("result", "请求成功");
                } else {
                    result.put("result", "达到访问次数限制，禁止访问");
                    response.getWriter().print(JsonUtil.toJson(result));

                }
                return acquire;
            }
        }

        return true;
    }
}
