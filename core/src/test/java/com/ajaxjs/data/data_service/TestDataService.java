package com.ajaxjs.data.data_service;

import com.ajaxjs.data.BaseTest;
import com.ajaxjs.data.crud.CRUD_Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;


public class TestDataService extends BaseTest {
    static class ExtDataService extends DataService {
    }

    @Autowired
    CRUD_Service crud;

    @Test
    public void testDataService() {
        DataService service = new ExtDataService();

        assertNotNull(service);
    }
}
