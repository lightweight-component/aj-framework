package com.ajaxjs.util.httpremote.call;

import org.junit.jupiter.api.Test;

class TestCall {
    @Test
    void testCall() {
        DoRequest request = CallHandler.create(DoRequest.class);
        System.out.println(request);
        String result = request.get();
        System.out.println(result);

//        Map<String, Object> map = request.getJson();
//        System.out.println(map);
//
//        map = request.getJsonWithHeaders();
//        System.out.println(map);
    }

}
