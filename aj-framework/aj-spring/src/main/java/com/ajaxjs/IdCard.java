package com.ajaxjs;

import com.ajaxjs.service.tools.IIdCard;

public class IdCard implements IIdCard {
    @Override
    public boolean check(String idCardNo) {
        System.out.println("check");
        return false;
    }
}
