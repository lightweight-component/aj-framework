package com.ajaxjs.business.json.simple5.tokenizer;

import lombok.Data;


@Data
public class Token {
    private TokenType type;
    private String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
