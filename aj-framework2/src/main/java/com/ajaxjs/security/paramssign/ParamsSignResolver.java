package com.ajaxjs.security.paramssign;

import com.ajaxjs.util.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public class ParamsSignResolver implements HandlerMethodArgumentResolver {
    final ParamsSign paramsSign;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasMethodAnnotation(ParamsSignCheck.class) || parameter.hasParameterAnnotation(ParamsSignCheck.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        assert request != null;
        String contentType = request.getContentType();
        Map<String, String> params = new HashMap<>();

        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            // 处理 JSON 请求
            String jsonBody = request.getReader().lines().reduce("", (acc, line) -> acc + line);
            Map<String, Object> _params = JsonUtil.json2map(jsonBody);

            if (!_params.containsKey(ParamsSign.SIGN_PARAMS)) // 如果 body 中没有提交 sign 参数，那么从 QueryString 中获取之
                _params.put(ParamsSign.SIGN_PARAMS, request.getParameter(ParamsSign.SIGN_PARAMS));

            _params.forEach((key, value) -> params.put(key, value.toString()));
        } else if (contentType != null && contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
            Map<String, String[]> paramMap = request.getParameterMap(); // 获取所有参数

            paramMap.forEach((key, value) -> params.put(key, value[0])); // 只取第一个值
        }

        if (!params.containsKey(ParamsSign.SIGN_PARAMS))
            throw new IllegalArgumentException("Missing the sign parameter");

        // 校验……
        if (!paramsSign.verify(params))
            throw new SecurityException("Invalid Request Params Sign.");

        params.remove(ParamsSign.SIGN_PARAMS);

        Map<String, Object> result = new HashMap<>(params);
        Class<?> paramType = parameter.getParameterType();

        if (Map.class.isAssignableFrom(paramType))  // 返回 Map
            return result;
        else
            return JsonUtil.map2pojo(result, paramType);  // 返回 POJO
    }
}
