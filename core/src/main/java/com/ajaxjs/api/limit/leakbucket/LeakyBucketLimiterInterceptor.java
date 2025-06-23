package com.ajaxjs.api.limit.leakbucket;

import com.ajaxjs.springboot.DiContextUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
                JSONObject result = new JSONObject();
                if (acquire) {
                    result.put("result", "请求成功");
                } else {
                    result.put("result", "达到访问次数限制，禁止访问");
                    response.getWriter().print(JSON.toJSONString(result));
                }

                System.out.println(result);
                return acquire;
            }
        }

        return true;
    }
}
