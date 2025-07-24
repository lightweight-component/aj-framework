package com.ajaxjs.business.json.simple2;


import com.ajaxjs.business.json.simple2.tokenizer.ReaderChar;
import com.ajaxjs.business.json.simple2.tokenizer.TokenList;
import com.ajaxjs.business.json.simple2.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.StringReader;

/**
 * 撸一个JSON解析器
 * 考虑了数字的指数
 * <a href="https://blog.csdn.net/Dome_/article/details/87257256">...</a>
 * <a href="https://yanliang.cool/blog/JSONParser/">...</a>
 */
public class JSONParser {
    private final Tokenizer tokenizer = new Tokenizer();

    private final Parser parser = new Parser();

    public Object fromJSON(String json) throws IOException {
        TokenList tokens = tokenizer.getTokenStream(new ReaderChar(new StringReader(json)));

        return parser.parse(tokens);
    }
}
