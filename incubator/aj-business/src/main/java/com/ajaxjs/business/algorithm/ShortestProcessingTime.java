package com.ajaxjs.business.algorithm;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * 甘特图-最短工时算法
 * 一个最短工时的算法，p1,p2,p3,p4为工作，分为10个作业，数字运算采用BigDecimal运算，防止精度丢失。
 */
public class ShortestProcessingTime {
    // p1 p2 p3 p4为工位，数组内的值为作业需要的时间
    static BigDecimal[] p1 = {BigDecimal.valueOf(7.988652), BigDecimal.valueOf(8.770062), BigDecimal.valueOf(12.952448), BigDecimal.valueOf(11.160148), BigDecimal.valueOf(16.0201), BigDecimal.valueOf(9.87088), BigDecimal.valueOf(13.284036), BigDecimal.valueOf(19.07412), BigDecimal.valueOf(20.058414), BigDecimal.valueOf(15.014602)};
    static BigDecimal[] p2 = {BigDecimal.valueOf(9.936646), BigDecimal.valueOf(13.722012), BigDecimal.valueOf(7.010982), BigDecimal.valueOf(16.496146), BigDecimal.valueOf(8.82748), BigDecimal.valueOf(19.907534), BigDecimal.valueOf(14.962134), BigDecimal.valueOf(20.094358), BigDecimal.valueOf(15.97263), BigDecimal.valueOf(12.03512)};
    static BigDecimal[] p3 = {BigDecimal.valueOf(5.935868), BigDecimal.valueOf(13.005208), BigDecimal.valueOf(9.04917), BigDecimal.valueOf(12.013664), BigDecimal.valueOf(18.114384), BigDecimal.valueOf(17.928158), BigDecimal.valueOf(19.846022), BigDecimal.valueOf(14.148532), BigDecimal.valueOf(14.970352), BigDecimal.valueOf(7.041914)};
    static BigDecimal[] p4 = {BigDecimal.valueOf(18.128398), BigDecimal.valueOf(11.24949), BigDecimal.valueOf(16.052406), BigDecimal.valueOf(19.087604), BigDecimal.valueOf(16.831394), BigDecimal.valueOf(18.942358), BigDecimal.valueOf(20.012922), BigDecimal.valueOf(13.883878), BigDecimal.valueOf(15.116388), BigDecimal.valueOf(8.949654)};

    static BigDecimal[][] sum = {p2, p3, p4};

    public static BigDecimal sum(BigDecimal[] a, int n) {
        return (n < 1) ? p1[0] : sum(a, n - 1).add(a[n]);
    }

    public static void main(String[] args) {
        // 用于存储最长时间的数组
        BigDecimal[] end = new BigDecimal[p1.length];
        for (int i = 0; i < end.length; i++)
            end[i] = sum(p1, i);

        for (BigDecimal[] ints : sum) {
            for (int i = 0; i < ints.length; i++) {
                if (i == 0)
                    end[i] = end[i].add(ints[i]);
                else
                    end[i] = (end[i - 1].compareTo(end[i]) > 0 ? end[i - 1] : end[i]).add(ints[i]);
            }
        }

        // 取出数组中最大的数就是最短工时
        System.out.println("最短工时:" + Arrays.stream(end).max(BigDecimal::compareTo).get());
    }
}
