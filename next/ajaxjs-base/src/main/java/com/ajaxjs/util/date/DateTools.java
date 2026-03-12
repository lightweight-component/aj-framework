package com.ajaxjs.util.date;

import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RegExpUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Date Utils
 */
public class DateTools {
    /**
     * The year-month-day in RegExp String, to match like 2016-08-18
     */
    private final static String DATE_YEAR = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])";

    /**
     * The year-month-day-hour-minute-second in RegExp String, to match like 2016-08-18 11:20:05
     */
    private final static String DATE_TIME = DATE_YEAR + " ([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]";

    /**
     * The date in basic data value (long/int/string), transform to Date type.
     *
     * @param obj Any object that trying to transform
     * @return The date, or `null` if the object is null or the transforming failed.
     */
    public static Date object2Date(Object obj) {
        if (obj == null)
            return null;
        else if (obj instanceof Date)
            return (Date) obj;
        else if (obj instanceof Long)
            return new Date((Long) obj);
        else if (obj instanceof Integer)
            return object2Date(Long.parseLong(obj + "000")); /* 10 位长 int，后面补充三个零为13位 long 时间戳 */
        else if (obj instanceof String) {
            String str = obj.toString();

            if (ObjectHelper.isEmptyText(str))
                return null;

            LocalDateTime dateTime;
            if (RegExpUtils.match(DATE_TIME, str))
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeFormatter());
            else if (RegExpUtils.match(DATE_YEAR, str))
                dateTime = LocalDateTime.parse(str, Formatter.getDateFormatter()); // bug
            else
                dateTime = LocalDateTime.parse(str, Formatter.getDateTimeShortFormatter());

            return new DateTypeConvert(dateTime).to(Date.class, null);

            // 输入日期不合法，不能转为日期类型。请重新输入日期字符串格式类型，或考虑其他方法。
        } else if (obj instanceof LocalDateTime)
            return new DateTypeConvert((LocalDateTime) obj).to(Date.class, null);
        else if (obj instanceof LocalDate)
            return new DateTypeConvert((LocalDate) obj).to(Date.class, null);

        return null;
    }

    /**
     * Transform Date to LocalDateTime
     * It's a shorthand for new DateTypeConvert(date).to(LocalDateTime.class, null);
     *
     * @param date The input date
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Obtain current time by specified format.
     *
     * @param formatter The formatter object
     * @return The current time
     */
    public static String now(DateTimeFormatter formatter) {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Obtain current time by specified format.
     *
     * @param format The format string
     * @return The current time
     */
    public static String now(String format) {
        return now(Formatter.getDateFormatter(format));
    }

    /**
     * Obtain current time, which is formatted by default like "yyyy-MM-dd HH:mm:ss".
     *
     * @return The current time
     */
    public static String now() {
        return now(Formatter.getDateTimeFormatter());
    }

    /**
     * Obtain current time, which is formatted like "yyyy-MM-dd HH:mm".
     *
     * @return The current time
     */
    public static String nowShort() {
        return now(Formatter.getDateTimeShortFormatter());
    }

    /**
     * 请求的时间戳，格式必须符合 RFC1123 的日期格式
     *
     * @return The current time
     */
    public static String nowGMTDate() {
        return Formatter.GMT_FORMATTER.format(Instant.now());
    }

    /**
     * 请求的时间戳。按照 ISO8601 标准表示，并需要使用 UTC 时间，格式为 yyyy-MM-ddTHH:mm:ssZ
     *
     * @return The current time
     */
    public static String newISO8601Date() {
        return Formatter.ISO8601_FORMATTER.format(Instant.now());
    }
}