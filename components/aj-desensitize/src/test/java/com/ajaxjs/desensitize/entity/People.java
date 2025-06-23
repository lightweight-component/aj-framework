package com.ajaxjs.desensitize.entity;

import com.ajaxjs.desensitize.DesensitizeType;
import com.ajaxjs.desensitize.annotation.DesensitizeComplexProperty;
import com.ajaxjs.desensitize.annotation.DesensitizeModel;
import com.ajaxjs.desensitize.annotation.DesensitizeNullProperty;
import com.ajaxjs.desensitize.annotation.DesensitizeProperty;
import lombok.Data;

@DesensitizeModel
@Data
public class People {
    @DesensitizeProperty
    private String username;
    private String password;
    @DesensitizeComplexProperty(keys = {"email", "phone"}, value = "value", types = {DesensitizeType.EMAIL, DesensitizeType.PHONE})
    private String key;
    private String value;
    @DesensitizeNullProperty
    private int age;
    @DesensitizeNullProperty
    private byte b;
    @DesensitizeNullProperty
    private short s;
    @DesensitizeNullProperty
    private long l;
    @DesensitizeNullProperty
    private double d;
    @DesensitizeNullProperty
    private float f;
    @DesensitizeNullProperty
    private char c;
    @DesensitizeNullProperty
    private String str;
}
