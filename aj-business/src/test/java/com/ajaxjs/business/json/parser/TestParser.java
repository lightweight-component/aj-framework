package com.ajaxjs.business.json.parser;


import org.junit.jupiter.api.Test;

public class TestParser {
    @Test
    public void test() {
        String json = "{\"true\": true, \"false\": false, \"null\": null, \"object\": {}, \"number\": 0, \"array\": [], \"string\": \"\\u6211\\u662F\\u5730\\u7403\\uD83C\\uDF0D\"}";
        Parser parser = new Parser(json);
        JsonObject object = parser.parse().asJsonObject();
        System.out.println(object); // {number=0.0, null=null, string=我是地球🌍, array=[], true=true, false=false, object={}}
    }
}
