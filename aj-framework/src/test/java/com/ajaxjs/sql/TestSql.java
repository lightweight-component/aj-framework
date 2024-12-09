package com.ajaxjs.sql;

import com.ajaxjs.sql.model.Create;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestSql {
    @Test
    public void testQuery() {
        Sql sql = new Sql();
        sql.sql("SELECT * FROM user WHERE ID = ?");
    }

    @Test
    public void testCreate() {
        Sql sql = new Sql();
//        Create create = sql.sql("SELECT * FROM user WHERE ID = ?").setIdType(int.class).create();
//
//        assertTrue(create.isOk());
//        System.out.println(create.getNewlyId());
    }
}
