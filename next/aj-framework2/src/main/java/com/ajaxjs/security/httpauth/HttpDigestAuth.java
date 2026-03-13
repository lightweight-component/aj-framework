package com.ajaxjs.security.httpauth;

import com.ajaxjs.security.InterceptorAction;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.HashHelper;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simply check the HttpDigestAuth
 */
@Data
@Component
@EqualsAndHashCode(callSuper = true)
@ConditionalOnProperty(name = "security.HttpDigestAuth.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "security.http-digest-auth")
public class HttpDigestAuth extends InterceptorAction<HttpDigestAuthCheck> {
    String username;

    String password;

    String REALM = "my-digest-realm";

    @Override
    public boolean action(HttpDigestAuthCheck annotation, HttpServletRequest req) {
        String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION);  // 获取 Referer 头

        if (ObjectHelper.isEmptyText(authHeader) || !authHeader.startsWith("Digest ")) {
            sendDigestChallenge();
            return false;
        }

        // 解析 Authorization 头
        Map<String, String> authParams = parseDigestHeader(authHeader.substring(7));

        if (!username.equals(authParams.get("username"))) {
            sendDigestChallenge();
            return false;
        }

        String method = req.getMethod();
        String uri = authParams.get("uri");
        String nonce = authParams.get("nonce");
        String nc = authParams.get("nc");
        String cnonce = authParams.get("cnonce");
        String qop = authParams.get("qop");
        String responseDigest = authParams.get("response");

        // HA1 = MD5(username:realm:password)
        String ha1 = HashHelper.md5(username + ":" + REALM + ":" + password);
        // HA2 = MD5(method:uri)
        String ha2 = HashHelper.md5(method + ":" + uri);
        // response = MD5(HA1:nonce:nc:cnonce:qop:HA2)
        String validResponse = HashHelper.md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + ha2);

        if (validResponse.equals(responseDigest))
            return true;
        else {
            sendDigestChallenge();
            return false;
        }
    }

    private void sendDigestChallenge() {
        HttpServletResponse response = DiContextUtil.getResponse();
        String nonce = new Base64Utils(UUID.randomUUID().toString()).encodeAsString();
        String header = String.format("Digest realm=\"%s\", qop=\"auth\", nonce=\"%s\", opaque=\"%s\"", REALM, nonce, HashHelper.md5(REALM));
        response.setHeader("WWW-Authenticate", header);
        response.setStatus(401);

        try {
            response.getWriter().write("Digest Auth Required");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Map<String, String> parseDigestHeader(String header) {
        Map<String, String> map = new HashMap<>();
        String[] parts = header.split(", ");

        for (String part : parts) {
            int eq = part.indexOf('=');

            if (eq > 0) {
                String key = part.substring(0, eq).trim();
                String value = part.substring(eq + 1).replaceAll("^\"|\"$", "");
                map.put(key, value);
            }
        }

        return map;
    }
}
