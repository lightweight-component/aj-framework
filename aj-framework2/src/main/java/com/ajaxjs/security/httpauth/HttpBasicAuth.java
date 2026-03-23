package com.ajaxjs.security.httpauth;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Simply check the HttpBasicAuth
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.HttpBasicAuth.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.http-basic-auth")
public class HttpBasicAuth extends InterceptorAction<HttpBasicAuthCheck> {
    String username;

    String password;

    @Override
    public boolean action(HttpBasicAuthCheck annotation, HttpServletRequest req) {
        if (ObjectHelper.isEmptyText(username) || ObjectHelper.isEmptyText(password))
            throw new UnsupportedOperationException("System not finished setting the password or username.");

        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);  // 获取 Referer 头

        if (ObjectHelper.isEmptyText(authHeader) || !authHeader.startsWith("Basic ")) { // 提示用户输入账号密码
            responseLogin();
            return false;
        }

        String base64Credentials = authHeader.substring("Basic ".length());
        String credentials = new Base64Utils(base64Credentials).decodeAsString();
        String[] values = credentials.split(":", 2);
        String _username = values[0];
        String _password = values[1];

        if (username.equals(_username) && password.equals(_password))
            return true;
        else {
            responseLogin();
            return false;
        }
    }

    public static String create(String username, String password) {
        return "Basic " + new Base64Utils(username + ":" + password).encodeAsString();
    }

    private static void responseLogin() {
        HttpServletResponse response = DiContextUtil.getResponse();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setHeader("WWW-Authenticate", "Basic realm=\"User Login\"");

        try {
            response.getWriter().println("HTTP Status 401 - Unauthorized");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
