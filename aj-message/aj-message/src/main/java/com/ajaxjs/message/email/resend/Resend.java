package com.ajaxjs.message.email.resend;

import com.ajaxjs.message.email.Email;
import com.ajaxjs.message.email.ISendEmail;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.http_request.Post;

import java.util.Map;
import java.util.Objects;

public class Resend implements ISendEmail {
    static String RESEND_API = "https://api.resend.com/emails";

    String apiKey;

    @Override
    public boolean sendEmail(Email email) {
        Objects.requireNonNull(apiKey, "请设置 Resend API Key");

        Map<String, String> params = ObjectHelper.mapOf("from", email.getFrom(),
                "to", email.getTo(), "subject", email.getSubject());
        params.put("html", email.getContent());

        Map<String, Object> map = Post.apiJsonBody(RESEND_API, params, (head) -> {
            head.setRequestProperty("Content-Type", "application/json");
            head.setRequestProperty("Authorization", "Bearer " + apiKey);
        });

        return map != null && map.containsKey("id");
    }

    public Resend setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }
}
