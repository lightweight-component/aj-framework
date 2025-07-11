package com.ajaxjs.business.json.simple2.tokenizer;

import lombok.Data;

/**
 * 存储对应类型的字面量
 */
@Data
public class Token {
    private TokenType tokenType;

    private String value;

    public Token(TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Token{" + "tokenType=" + tokenType + ", value='" + value + '\'' + '}';
    }
}
