package util.date;

import com.ajaxjs.util.date.DateTypeConvert;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDateTypeConvert {
    private final ZoneId zone = ZoneId.systemDefault();

    @Test
    void testDateToLocalDate() {
        Date date = new Date();
        LocalDate expected = date.toInstant().atZone(zone).toLocalDate();

        LocalDate result = new DateTypeConvert(date).to(LocalDate.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testLocalDateToDate() {
        LocalDate localDate = LocalDate.of(2025, 10, 23);
        Date expected = Date.from(localDate.atStartOfDay(zone).toInstant());

        Date result = new DateTypeConvert(localDate).to(Date.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testTimestampToInstant() {
        long timestamp = System.currentTimeMillis();
        Instant expected = Instant.ofEpochMilli(timestamp);

        Instant result = new DateTypeConvert(timestamp).to(Instant.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testInstantToLocalDateTime() {
        Instant instant = Instant.now();
        LocalDateTime expected = instant.atZone(zone).toLocalDateTime();

        LocalDateTime result = new DateTypeConvert(instant).to(LocalDateTime.class, null);
        assertEquals(expected, result);
    }

    @Test
    void testSqlDateToOffsetDateTime() {
        java.sql.Date sqlDate = java.sql.Date.valueOf("2025-10-23");
        OffsetDateTime expected = sqlDate.toLocalDate()
                .atStartOfDay(zone)
                .toInstant()
                .atOffset(zone.getRules().getOffset(sqlDate.toLocalDate().atStartOfDay()));

        OffsetDateTime result = new DateTypeConvert(sqlDate).to(OffsetDateTime.class, null);
        assertEquals(expected.toLocalDate(), result.toLocalDate()); // compare date only
    }
}
