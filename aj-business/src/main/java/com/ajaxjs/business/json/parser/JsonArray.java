package com.ajaxjs.business.json.parser;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class JsonArray extends ArrayList<JsonValue> implements JsonValue {
    @Override
    public String toJson() {
        return stream().map(JsonValue::toJson).collect(Collectors.joining(",", "[", "]"));
    }
}
