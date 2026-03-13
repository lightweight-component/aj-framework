package com.ajaxjs.security.captcha.google;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.util.httpremote.Post;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 *
 */
@Data
@Slf4j
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.captcha.google.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.captcha.google")
public class GoogleCaptcha extends InterceptorAction<GoogleCaptchaCheck> {
    private String accessSecret;

    /**
     * 校验表单时候客户端传过来的 token 参数名
     */
    public final static String PARAM_NAME = "grecaptchaToken";

    /**
     * 谷歌校验 API
     */
    private final static String SITE_VERIFY = "https://www.recaptcha.net/recaptcha/api/siteverify";

    @Override
    public boolean action(GoogleCaptchaCheck annotation, HttpServletRequest request) {
        String token = request.getParameter(PARAM_NAME);

        if (!StringUtils.hasText(token))
            throw new SecurityException("非法攻击！客户端缺少必要的参数");

        Map<String, Object> map = Post.api(SITE_VERIFY, String.format("secret=%s&response=%s", accessSecret, token.trim()));

        if (map == null)
            throw new IllegalAccessError("谷歌验证码服务失效，请联系技术人员");

        if ((boolean) map.get("success")) {// 判断用户输入的验证码是否通过
            if (map.get("score") != null) {
                // 评分0 到 1。1：确认为人类，0：确认为机器人
                double score = (double) map.get("score");

                if (score < 0.5)
                    throw new SecurityException("验证码不通过，非法请求");
            }

            return true;
        } else {
            if ("timeout-or-duplicate".equals(map.get("error-codes")))
                throw new NullPointerException("验证码已经过期，请刷新");

            throw new SecurityException("验证码不正确");
        }
    }
}
