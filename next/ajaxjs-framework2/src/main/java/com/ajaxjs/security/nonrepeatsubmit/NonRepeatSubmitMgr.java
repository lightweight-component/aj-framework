package com.ajaxjs.security.nonrepeatsubmit;

import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
public class NonRepeatSubmitMgr {
    static final String TOKEN_NAME = "NS_TOKEN";

    Consumer<String> createToken;

    Function<String, Boolean> checkTokenIfExists;

    Consumer<String> makeTokenUsed;

    public String initToken() {
        String token = UUID.randomUUID().toString();
        createToken.accept(token);

        return token;
    }

    static String extractToken(HttpServletRequest req) {
        String token = req.getHeader(TOKEN_NAME);

        if (ObjectHelper.hasText(token))
            return token;

        token = req.getParameter(TOKEN_NAME);

        if (ObjectHelper.hasText(token))
            return token;

        // TODO gets from Cookie

        return null;
    }

    /* By AUTO */
    Supplier<String> obtainUserId;

    BiConsumer<String, Integer> makeExpiresToken;

    public void handle(HttpServletRequest request, Method method) throws IllegalAccessException {
        NonRepeatSubmit annotation = method.getAnnotation(NonRepeatSubmit.class);

        if (annotation.type() == NonRepeatSubmit.Type.AUTO) {
            String userId = obtainUserId.get();

            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();
            String uri = request.getRequestURI();
//            String paramString = Arrays.toString(params);

            String token = HashHelper.md5(className + methodName + uri);

            if (checkTokenIfExists.apply(token))
                throw new IllegalAccessException("Submit repeated, it's NOT allow!");
            else
                makeExpiresToken.accept(token, annotation.ttl());

        } else if (annotation.type() == NonRepeatSubmit.Type.TOKEN) {
            String token = extractToken(request);

            if (ObjectHelper.isEmptyText(token))
                throw new IllegalArgumentException("The NS_token is empty.");

            if (!checkTokenIfExists.apply(token))
                throw new IllegalAccessException("Invalid or used token: " + token);

            // 标记为已使用
            makeTokenUsed.accept(token);
        }
    }
}
