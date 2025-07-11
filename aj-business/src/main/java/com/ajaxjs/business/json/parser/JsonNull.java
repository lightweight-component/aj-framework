package com.ajaxjs.business.json.parser;

public class JsonNull implements JsonValue {
    @Override
    public String toString() {
        return "null";
    }

    @Override
    public String toJson() {
        return toString();
    }
}
