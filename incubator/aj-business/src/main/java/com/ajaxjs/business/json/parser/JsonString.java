package com.ajaxjs.business.json.parser;

public class JsonString implements JsonValue {
    private String value = "";

    public JsonString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public String toJson() {
        return "\"" + value + "\"";
    }
}
