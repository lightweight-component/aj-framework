package com.ajaxjs.business.json;

import com.ajaxjs.business.json.simple.Json;
import com.ajaxjs.business.json.simple.JsonObject;


public class TestSimple1 {

    public void main() {
        String json = "{\n" + "\"employees\": [\n" + "{ \"firstName\":\"Bill\" , \"lastName\":\"Gates\" },\n" + "{ \"firstName\":\"George\" , \"lastName\":\"Bush\" },\n" + "{ \"firstName\":\"Thomas\" , \"lastName\":\"Carter\" }\n" + "],\"id\":-10.5,\"success\":true\n" + "}";
        JsonObject jsonObject = Json.parseJsonObject(json);
        System.out.println(jsonObject);
    }
}
