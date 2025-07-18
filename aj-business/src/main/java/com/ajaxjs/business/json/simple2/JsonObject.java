package com.ajaxjs.business.json.simple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObject {
    private final Map<String, Object> map = new HashMap<>();

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Object get(String key) {
        return map.get(key);
    }

    public List<Map.Entry<String, Object>> getAllKeyValue() {
        return new ArrayList<>(map.entrySet());
    }

    public JsonObject getJsonObject(String key) {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Invalid key");

        Object obj = map.get(key);

        if (!(obj instanceof JsonObject))
            throw new JsonTypeException("Type of value is not JsonObject");

        return (JsonObject) obj;
    }

    public JsonArray getJsonArray(String key) {
        if (!map.containsKey(key))
            throw new IllegalArgumentException("Invalid key");

        Object obj = map.get(key);

        if (!(obj instanceof JsonArray))
            throw new JsonTypeException("Type of value is not JsonArray");

        return (JsonArray) obj;
    }

    @Override
    public String toString() {
        return FormatUtil.beautify(this);
    }
}
