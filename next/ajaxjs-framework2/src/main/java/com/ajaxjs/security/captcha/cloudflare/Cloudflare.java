package com.ajaxjs.security.captcha.cloudflare;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Data
@Slf4j
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.captcha.cloudflare.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.captcha.cloudflare")
public class Cloudflare extends InterceptorAction<CloudflareCheck> {
    String secretKey;

    @Override
    public boolean action(CloudflareCheck annotation, HttpServletRequest req) {
        String token = req.getParameter("cf-turnstile-response");

        if (ObjectHelper.isEmptyText(token))
            throw new IllegalArgumentException("Missing the parameter of token");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        Map<String, Object> response;

        try {
            response = getRestTemplate().postForObject(CF_URL, requestEntity, Map.class);
        } catch (Throwable e) {
            log.warn("CloudFlare Turnstile errors!", e);
            throw new SecurityException("Captcha checked failed!");
        }

        if (response == null) {
            log.warn("CloudFlare Turnstile returned Nothing!");
            return false;
        } else {
            boolean isOk = response.containsKey("success") && (boolean) response.get("success");

            if (!isOk) {
                log.info("Captcha checked failed! The reason: {}", response);
                throw new SecurityException("Captcha checked failed!");
            } else
                return true;
        }
    }

    private static RestTemplate restTemplate;

    static synchronized RestTemplate getRestTemplate() {
        RestTemplate http = DiContextUtil.getBean(RestTemplate.class);

        if (http == null) {
            if (restTemplate == null)
                restTemplate = new RestTemplate();

            http = restTemplate;
        }

        return http;
    }

    private final static String CF_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";
}
