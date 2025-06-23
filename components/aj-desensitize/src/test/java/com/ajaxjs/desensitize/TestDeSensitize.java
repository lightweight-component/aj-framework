package com.ajaxjs.desensitize;

import com.ajaxjs.desensitize.entity.BaseResponse;
import com.ajaxjs.desensitize.entity.People;
import com.ajaxjs.desensitize.entity.PeopleMap;
import com.ajaxjs.desensitize.entity.PubResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDeSensitize {
    @Test
    public void jsonNullFieldTest() {
        People people = new People();
        people.setKey("email");
        people.setValue("1563919868@qq.com");
        people.setStr("测试null");
        Object obj = DeSensitize.acquire(people);
        if (obj instanceof Map) {
            Map s = (Map) obj;
            Assertions.assertNull(s.get("str"));
            Assertions.assertEquals(s.get("age"), 0);
            Assertions.assertEquals(s.get("b"), (byte) 0);
            Assertions.assertEquals(s.get("s"), (short) 0);
            Assertions.assertEquals(s.get("l"), 0L);
        }
    }

    @Test
    public void map() {
        PeopleMap peopleMap = new PeopleMap();
        peopleMap.setUsername("田晓霞");
        peopleMap.setPassword("123456");
        peopleMap.setLocalDateTime(LocalDateTime.now());
        PeopleMap.SubMap subMap = new PeopleMap.SubMap();
        subMap.setSub("subMap");
        peopleMap.getSubMapMap().put("subMap", subMap);
        Map<String, PeopleMap> dataMap = new HashMap<>();
        dataMap.put("test", peopleMap);
        Map<String, Object> map = (Map<String, Object>) DeSensitize.acquire(dataMap);
        Assertions.assertEquals(((Map<String, Object>) map.get("test")).get("username"), "田晓霞");
        Assertions.assertEquals(((Map<String, Object>) map.get("test")).get("password"), "123456");
    }

    @Test
    public void testFieldMap() {
        PeopleMap peopleMap = new PeopleMap();
        peopleMap.getParams().put("password", "12345");
        peopleMap.getParams().put("username", "田晓霞");
        peopleMap.getParams().put("phone", "15645562587");
        Object p = DeSensitize.acquire(peopleMap);
        Assertions.assertNotNull(p);
    }

    @Test
    public void testFieldMap1() {
        PeopleMap peopleMap = new PeopleMap();
        peopleMap.getAges().put(12, "12345");
        peopleMap.getAges().put(13, "田晓霞");
        peopleMap.getAges().put(14, "15645562587");
        Object p = DeSensitize.acquire(peopleMap);
        Assertions.assertNotNull(p);
    }

    @Test
    public void test11() {
        PubResponse response = new PubResponse();
        response.password = "32433";
        response.username = "条消息";
        response.email = "1393619859@qq.com";
        response.idCard = "321455188625645686";
        response.bankCard = "325648956125656666";
        response.phone = "18254452658";
        response.mobile = "1234567";
        PubResponse.Job job = new PubResponse.Job();
        job.email = "1393619859@qq.com";
        job.work = "呵呵哈哈哈";
        response.job = job;
        response.jobs = new PubResponse.Job[]{job};
        response.jobList = List.of(job);
        BaseResponse<PubResponse> r = BaseResponse.<PubResponse>newBuilder().withData(response).build();
        BaseResponse<PubResponse> response1 = (BaseResponse<PubResponse>) DeSensitize.acquire(r);
        Assertions.assertEquals(response1.getData().email, "1393619859@qq.com");
        Map<String, Object> response2 = (Map<String, Object>) DeSensitize.acquire(r, BaseResponse.class);
        Assertions.assertEquals(((Map<String, Object>) response2.get("data")).get("email"), "1***9@qq.com");
    }
}
