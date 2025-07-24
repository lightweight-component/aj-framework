package com.ajaxjs.business.json.parser;

/**
 * 支持unicode字符(包括emoji)
 * <a href="https://github.com/nintha/json-parser-java">...</a>
 */
public class Parser {
    private final Tokenizer tokenizer;

    public Parser(String json) {
        this.tokenizer = new Tokenizer(json);
    }

    public JsonValue parse() {
        return parseToken(step());
    }

    private Token step() {
        Token token = tokenizer.next();
        if (token == null) throw new RuntimeException("Unexpected end of JSON");

//        System.out.println("token=" + token);
        return token;
    }

    private JsonValue parseToken(Token token) {
        switch (token.type()) {
            case NULL:
                return new JsonNull();
            case TEXT:
                return new JsonString(((Token.Text) token).value);
            case NUMBER:
                return new JsonNumber(((Token.Num) token).value);
            case BOOLEAN:
                return new JsonBoolean(((Token.Bool) token).value);
            case BRACKET_ON:
                return parseArray();
            case BRACE_ON:
                return parseObject();
            default:
                throw new RuntimeException("Unexpected token: " + token.type());
        }
    }

    private JsonArray parseArray() {
        JsonArray array = new JsonArray();

        Token token = step();
        if (token.type() == Token.Type.BRACKET_OFF) return array;
        else array.add(parseToken(token));

        loop:
        while (true) {
            token = step();
            switch (token.type()) {
                case COMMA: {
                    array.add(parse());
                    break;
                }
                case BRACKET_OFF:
                    break loop;
                default:
                    throw new RuntimeException("Unexpected token: " + token.type());
            }
        }
        return array;
    }

    private JsonObject parseObject() {
        JsonObject object = new JsonObject();
        Token token = step();

        switch (token.type()) {
            case BRACE_OFF:
                return object;
            case TEXT: {
                String key = ((Token.Text) token).value;
                Token colon = step();

                if (colon.type() != Token.Type.COLON) throw new RuntimeException("Unexpected token: " + colon.type());

                JsonValue value = parse();
                object.put(key, value);

                break;
            }
            default:
                throw new RuntimeException("Unexpected token: " + token.type());
        }

        loop:
        while (true) {
            token = step();

            switch (token.type()) {
                case BRACE_OFF:
                    break loop;
                case COMMA: {
                    Token keyToken = step();
                    String key;

                    if (keyToken.type() == Token.Type.TEXT) key = ((Token.Text) keyToken).value;
                    else throw new RuntimeException("Unexpected token: " + keyToken.type());

                    Token colon = step();
                    if (colon.type() != Token.Type.COLON)
                        throw new RuntimeException("Unexpected token: " + colon.type());

                    JsonValue value = parse();
                    object.put(key, value);
                    break;
                }
                default:
                    throw new RuntimeException("Unexpected token: " + token.type());
            }
        }

        return object;
    }

}
