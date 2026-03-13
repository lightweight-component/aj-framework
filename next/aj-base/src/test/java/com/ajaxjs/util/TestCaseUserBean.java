package com.ajaxjs.util;

import lombok.Data;

/**
 * 用户的测试用例
 *
 * @author sp42 frank@ajaxjs.com
 */
@Data
public class TestCaseUserBean {
    public String directField = "directField";

    private long id;
    private String name;
    private int age;
    private boolean sex;
    private String[] children;
    private int[] luckyNumbers;
}
