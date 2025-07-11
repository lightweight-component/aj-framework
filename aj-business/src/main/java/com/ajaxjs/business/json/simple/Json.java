package com.ajaxjs.business.json.simple;

/**
 * <a href="https://blog.csdn.net/xcr530551426/article/details/77801106">...</a>
 */
public class Json {
    private static void checkRemain(ObjectParser parser, String json) {
        if (parser.getPos() < json.length()) {
            for (int i = parser.getPos(); i < json.length(); i++) {
                char c = json.charAt(i);

                if (!ObjectParser.isSpace(c)) throw new JsonException("unexpected remain string：" + json.substring(i));
            }
        }
    }

    public static JsonObject parseJsonObject(String json) {
        ObjectParser parser = new ObjectParser(json);
        Object object = parser.nextObject();
        if (object == null || !(object instanceof JsonObject)) throw new JsonException("not a JsonObject:" + object);

        checkRemain(parser, json);

        return (JsonObject) object;
    }

    public static JsonArray parseArray(String json) {
        ObjectParser parser = new ObjectParser(json);
        Object object = parser.nextObject();
        if (object == null || !(object instanceof JsonArray)) throw new JsonException("not a JsonArray:" + object);

        checkRemain(parser, json);

        return (JsonArray) object;
    }

    public static Object nextObject(String json) {
        return new ObjectParser(json).nextObject();
    }
}
