package com.ajaxjs.framework.holiday;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 节假日
 */
@Slf4j
@RequiredArgsConstructor
public class HolidaysApi implements InitializingBean {
    /**
     * 存储节假日
     */
    private static final Map<Integer, Map<String, Integer>> YEAR_DATA_MAP = new HashMap<>();

    /**
     * 获取日期类型
     *
     * @param localDate LocalDate
     * @return DaysType
     */
    public DayType getDayType(LocalDate localDate) {
        int year = localDate.getYear();
        Map<String, Integer> dataMap = YEAR_DATA_MAP.get(year);

        if (dataMap == null) {// 对于没有数据的，我们按正常的周六日来判断，
            log.error("没有对应年:[{}]的数据，请升级或者自行维护数据！", year);

            return isWeekDay(localDate);
        }

        // 日期信息
        int monthValue = localDate.getMonthValue(), dayOfMonth = localDate.getDayOfMonth();
        // 月份和日期
        String monthAndDay = String.format("%02d%02d", monthValue, dayOfMonth);
        Integer result = dataMap.get(monthAndDay);

        return result == null ? isWeekDay(localDate) : DayType.from(result);
    }

    /**
     * 判断是否工作日
     *
     * @param localDate LocalDate
     * @return DaysType
     */
    private static DayType isWeekDay(LocalDate localDate) {
        int week = localDate.getDayOfWeek().getValue();

        return week == 6 || week == 7 ? DayType.REST_DAY : DayType.WORKING_DAY;
    }

    /**
     * 获取日期类型
     *
     * @param localDateTime LocalDateTime
     * @return DaysType
     */
    public DayType getDayType(LocalDateTime localDateTime) {
        return getDayType(localDateTime.toLocalDate());
    }

    /**
     * 获取日期类型
     *
     * @param date Date
     * @return DaysType
     */
    public DayType getDayType(Date date) {
        return getDayType(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * 判断是否工作日
     *
     * @param localDate LocalDate
     * @return 是否工作日
     */
    public boolean isWeekday(LocalDate localDate) {
        return DayType.WORKING_DAY.equals(getDayType(localDate));
    }

    /**
     * 判断是否工作日
     *
     * @param localDateTime LocalDateTime
     * @return 是否工作日
     */
    public boolean isWeekday(LocalDateTime localDateTime) {
        return DayType.WORKING_DAY.equals(getDayType(localDateTime));
    }

    /**
     * 判断是否工作日
     *
     * @param date Date
     * @return 是否工作日
     */
    public boolean isWeekday(Date date) {
        return DayType.WORKING_DAY.equals(getDayType(date));
    }

    private final ResourceLoader resourceLoader;

    /**
     * 国务院发布的公告为准，随时调整及增加；<a href="http://www.gov.cn/zfwj/bgtfd.htm">...</a> 或 <a href="http://www.gov.cn/zhengce/xxgkzl.htm">...</a>
     * {
     *    "0101":2,
     *    "0102":1,
     *    "0103":1,
     *    "0926":0,
     *    "1009":0
     * }
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        int[] years = new int[]{2019, 2020, 2021, 2022, 2023, 2024, 2025};

        for (int year : years) {
            Resource resource = resourceLoader.getResource("classpath:data/" + year + "_data.json");

            try (InputStream inputStream = resource.getInputStream()) {
//                Map<String, Integer> dataMap = JsonUtil.readMap(inputStream, Byte.class);
//                YEAR_DATA_MAP.put(year, dataMap);
            }
        }
    }
}
