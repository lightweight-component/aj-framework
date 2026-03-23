package com.ajaxjs.util.date;

import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.date.Formatter;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TestDateTools {
    @Test
    void object2Date_NullObject_ReturnsNull() {
        Date date = DateTools.object2Date(null);
        assertNull(date);
    }

    @Test
    void object2Date_DateObject_ReturnsSameDate() {
        Date date = new Date();
        Date result = DateTools.object2Date(date);
        assertNotNull(result);
        assertEquals(date, result);
    }

    @Test
    void testObject2Date() {
        long time = 1681584645000L; // GMT: Saturday, April 15, 2023 11:30:45.000
        Date date = DateTools.object2Date(time);

        assertEquals("2023-04-16 02:50:45", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));

        int i = 1681584645;
        date = DateTools.object2Date(i);
        assertEquals("2023-04-16 02:50:45", DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));

        String dateTimeStr = "2023-04-15 11:30:45";
        date = DateTools.object2Date(dateTimeStr);
        assertEquals(dateTimeStr, DateTools.toLocalDateTime(date).format(Formatter.getDateTimeFormatter()));
    }

    @Test
    void testFormat(){
        Date date = DateTools.object2Date("2026-01-23");
    }
}
