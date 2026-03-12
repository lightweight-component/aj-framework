package com.ajaxjs.util.date;

import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Format date value, finally to String
 * A build-in cache system for date formatters.
 */
@RequiredArgsConstructor
public class Formatter {
    /**
     * The date to be formatted
     */
    private final TemporalAccessor temporal;

    /**
     * Create a formatter for a date
     *
     * @param date The date to be formatted, it'll transform to LocalDateTime
     */
    public Formatter(Date date) {
        this.temporal = DateTools.toLocalDateTime(date);
    }

    /**
     * Format the date value.
     * The result's like "yyyy-MM-dd HH:mm:ss"
     *
     * @return The formatted date string
     */
    public String format() {
        return getDateTimeFormatter().format(temporal);
    }

    /**
     * Format the date value by specified format.
     *
     * @param format The format string
     * @return The formatted date string
     */
    public String format(String format) {
        return getDateFormatter(format).format(temporal);
    }

    /* Some common formats */

    /**
     * Format the date value by specified format.
     * The result's like "yyyy-MM-dd"
     */
    public static final String DATE = "yyyy-MM-dd";

    /**
     * Format the date value by specified format.
     * The result's like "yyyy-MM-dd HH:mm:ss"
     */
    public static final String DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * Format the date value by specified format.
     * The result's like "yyyy-MM-dd HH:mm"
     */
    public static final String DATETIME_SHORT = "yyyy-MM-dd HH:mm";

    /**
     * All formatters will be cached in this map.
     */
    private static final Map<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    /**
     * Get a date formatter by format string
     * If this formatter is not cached, it will be created and cached.
     *
     * @param format Date format string
     * @return Date formatter
     */
    public static DateTimeFormatter getDateFormatter(String format) {
        return FORMATTER_CACHE.computeIfAbsent(format, DateTimeFormatter::ofPattern);
    }

    /**
     * Get a date formatter for date.
     * It just returns the year-month-day formatter.
     *
     * @return A year-month-day date.
     */
    public static DateTimeFormatter getDateFormatter() {
        return getDateFormatter(DATE);
    }

    /**
     * Get a date formatter for date and time.
     * It returns the year-month-day-hour-minute-second formatter, very detailed.
     *
     * @return A year-month-day-hour-minute-second date.
     */
    public static DateTimeFormatter getDateTimeFormatter() {
        return getDateFormatter(DATETIME);
    }

    /**
     * Get a date formatter for date and time.
     * It returns the year-month-day-hour-minute formatter, for common usage, that's enough.
     *
     * @return A year-month-day-hour-minute date.
     */
    public static DateTimeFormatter getDateTimeShortFormatter() {
        return getDateFormatter(DATETIME_SHORT);
    }

    /**
     * GMT common used for signature.
     */
    public final static DateTimeFormatter GMT_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    /**
     * ISO8601 common used for S3 storage.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public final static DateTimeFormatter ISO8601_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);
}