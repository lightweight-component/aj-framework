package com.ajaxjs.util.date;

import java.sql.Timestamp;
import java.time.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for converting between different date and time types in Java.
 * This class supports transformations between legacy Java date types (Date, Calendar)
 * and modern Java 8+ date/time API types (LocalDate, LocalDateTime, ZonedDateTime, etc.),
 * as well as SQL date types (java.sql.Date, Timestamp).
 *
 * <p>Provides a flexible conversion mechanism that handles various input date types
 * and can convert them to any supported target date type, with optional timezone support.
 *
 * <p>The class uses a builder pattern approach where you instantiate the converter
 * with an input date type and then call the {@link #to(Class, ZoneId)} method to
 * convert to the desired output type.
 */
public class DateTypeConvert {
    private long timestamp;

    /**
     * Creates a DateTypeConvert instance with a timestamp in milliseconds.
     *
     * @param timestamp the timestamp in milliseconds since the epoch (January 1, 1970, 00:00:00 GMT)
     */
    public DateTypeConvert(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Creates a DateTypeConvert instance with a timestamp in seconds, converting it to milliseconds.
     *
     * @param timestamp the timestamp in seconds since the epoch (January 1, 1970, 00:00:00 GMT)
     */
    public DateTypeConvert(int timestamp) {
        this.timestamp = Long.parseLong(timestamp + "000");
    }

    /**
     * Although it's a legacy, it's still widely used in many applications.
     * If it's used as Data Value, okay with it.
     */
    private Date input;

    /**
     * Creates a DateTypeConvert instance with a legacy java.util.Date object.
     *
     * @param input the Date object to convert
     */
    public DateTypeConvert(Date input) {
        this.input = input;
    }

    /**
     * SQL-specific date class
     */
    private java.sql.Date sqlDate;

    /**
     * Creates a DateTypeConvert instance with an SQL Date object.
     *
     * @param sqlDate the java.sql.Date object to convert
     */
    public DateTypeConvert(java.sql.Date sqlDate) {
        this.sqlDate = sqlDate;
    }

    /**
     * SQL timestamp class
     */
    private Timestamp sqlTimestamp;

    /**
     * Creates a DateTypeConvert instance with an SQL Timestamp object.
     *
     * @param sqlTimestamp the Timestamp object to convert
     */
    public DateTypeConvert(Timestamp sqlTimestamp) {
        Calendar cal = Calendar.getInstance();
        this.sqlTimestamp = sqlTimestamp;
    }

    /**
     * Date without time or timezone (e.g., 2025-10-23)
     */
    private LocalDate localDate;

    /**
     * Creates a DateTypeConvert instance with a LocalDate object.
     *
     * @param localDate the LocalDate object to convert (represents a date without time or timezone)
     */
    public DateTypeConvert(LocalDate localDate) {
        this.localDate = localDate;
    }

    /**
     * Time without date or timezone (e.g., 14:30:00)
     */
    private LocalTime localTime;

    /**
     * Creates a DateTypeConvert instance with a LocalTime object.
     *
     * @param localTime the LocalTime object to convert (represents a time without date or timezone)
     */
    public DateTypeConvert(LocalTime localTime) {
        this.localTime = localTime;
    }

    /**
     * Date and time without timezone
     */
    private LocalDateTime localDateTime;

    /**
     * Creates a DateTypeConvert instance with a LocalDateTime object.
     *
     * @param localDateTime the LocalDateTime object to convert (represents date and time without a timezone)
     */
    public DateTypeConvert(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    /**
     * Date and time with timezone
     */
    private ZonedDateTime zonedDateTime;

    /**
     * Creates a DateTypeConvert instance with a ZonedDateTime object.
     *
     * @param zonedDateTime the ZonedDateTime object to convert (represents date and time with timezone)
     */
    public DateTypeConvert(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    /**
     * Date and time with offset from UTC
     */
    private OffsetDateTime offsetDateTime;

    /**
     * Creates a DateTypeConvert instance with an OffsetDateTime object.
     *
     * @param offsetDateTime the OffsetDateTime object to convert (represents date and time with offset from UTC)
     */
    public DateTypeConvert(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    /**
     * Time with offset from UTC
     */
    private OffsetTime offsetTime;

    /**
     * Creates a DateTypeConvert instance with an OffsetTime object.
     *
     * @param offsetTime the OffsetTime object to convert (represents time with offset from UTC)
     */
    public DateTypeConvert(OffsetTime offsetTime) {
        this.offsetTime = offsetTime;
    }

    /**
     * Timestamp in UTC (nanosecond precision)
     */
    private Instant instant;

    /**
     * Creates a DateTypeConvert instance with an Instant object.
     *
     * @param instant the Instant object to convert (represents a timestamp in UTC with nanosecond precision)
     */
    public DateTypeConvert(Instant instant) {
        this.instant = instant;
    }

    /**
     * Creates a DateTypeConvert instance from a Calendar object.
     *
     * @param calendar the Calendar object to convert
     */
    public DateTypeConvert(Calendar calendar) {
        instant = calendar.toInstant();
    }

    /**
     * Creates a DateTypeConvert instance from a string representation of a date.
     * The string will be parsed into a Date object using DateTools.object2Date().
     *
     * @param str the string representation of a date
     */
    public DateTypeConvert(String str) {
        this(DateTools.object2Date(str));
    }

    /**
     * Converts the input date/time to the specified target date type.
     *
     * <p>This method handles the conversion from any supported input date type to any
     * supported target date type, using the provided or default time zone.
     *
     * @param clz    The target date type class (e.g., LocalDate.class, ZonedDateTime.class)
     * @param zoneId The time zone to use for conversion. If null, the system default time zone is used.
     * @param <T>    The generic type parameter representing the target date type
     * @return The converted date/time object of the specified type
     * @throws UnsupportedOperationException if no input date/time is set or if the target type is unsupported
     */
    @SuppressWarnings("unchecked")
    public <T> T to(Class<T> clz, ZoneId zoneId) {
        ZoneId zone = zoneId != null ? zoneId : ZoneId.systemDefault();
        Instant baseInstant;

        if (input != null) {/* SB way, actually u can set any value to Instant by constructor or setter method */
            baseInstant = input.toInstant();
        } else if (sqlDate != null) {
            baseInstant = sqlDate.toLocalDate().atStartOfDay(zone).toInstant();
        } else if (sqlTimestamp != null) {
            baseInstant = sqlTimestamp.toInstant();
        } else if (localDate != null) {
            baseInstant = localDate.atStartOfDay(zone).toInstant();
        } else if (localDateTime != null) {
            baseInstant = localDateTime.atZone(zone).toInstant();
        } else if (zonedDateTime != null) {
            baseInstant = zonedDateTime.toInstant();
        } else if (offsetDateTime != null) {
            baseInstant = offsetDateTime.toInstant();
        } else if (offsetTime != null) {
            baseInstant = offsetTime.atDate(LocalDate.now()).toInstant();
        } else if (instant != null) {
            baseInstant = instant;
        } else if (timestamp != 0L) {
            baseInstant = Instant.ofEpochMilli(timestamp);
        } else
            throw new UnsupportedOperationException("No input date/time set.");

        // Convert baseInstant to target
        if (clz == Instant.class) {
            return (T) baseInstant;
        } else if (clz == Date.class) {
            return (T) Date.from(baseInstant);
        } else if (clz == java.sql.Date.class) {
            return (T) java.sql.Date.valueOf(baseInstant.atZone(zone).toLocalDate());
        } else if (clz == Timestamp.class) {
            return (T) Timestamp.from(baseInstant);
        } else if (clz == LocalDate.class) {
            return (T) baseInstant.atZone(zone).toLocalDate();
        } else if (clz == LocalTime.class) {
            return (T) baseInstant.atZone(zone).toLocalTime();
        } else if (clz == LocalDateTime.class) {
            return (T) baseInstant.atZone(zone).toLocalDateTime();
        } else if (clz == ZonedDateTime.class) {
            return (T) baseInstant.atZone(zone);
        } else if (clz == OffsetDateTime.class) {
            return (T) baseInstant.atOffset(zone.getRules().getOffset(baseInstant));
        } else if (clz == OffsetTime.class) {
            return (T) baseInstant.atZone(zone).toOffsetDateTime().toOffsetTime();
        } else if (clz == Calendar.class) {
            Calendar calendar = Calendar.getInstance();
            baseInstant.atZone(zone);
            calendar.setTimeInMillis(baseInstant.toEpochMilli());

            return (T) calendar;
        }

        throw new UnsupportedOperationException("Unsupported target type: " + clz.getName());
    }
}