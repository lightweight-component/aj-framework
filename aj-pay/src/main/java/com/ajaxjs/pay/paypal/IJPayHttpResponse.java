package com.ajaxjs.pay.paypal;

import com.ajaxjs.util.ObjectHelper;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IJPayHttpResponse {
    private String body;

    private byte[] bodyByte;

    private int status;

    private Map<String, List<String>> headers;

    public String getHeader(String name) {
        List<String> values = this.headerList(name);

        return values == null || values.isEmpty() ? null : values.get(0);
    }

    private List<String> headerList(String name) {
        return ObjectHelper.isEmptyText(name) ? null : getHeaders().get(name.trim());
    }

    @Override
    public String toString() {
        return "IJPayHttpResponse{" +
                "body='" + body + '\'' +
                ", status=" + status +
                ", headers=" + headers +
                '}';
    }
}
