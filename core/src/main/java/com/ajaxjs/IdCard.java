package com.ajaxjs;

import com.ajaxjs.service.tools.IIdCard;

public class IdCard implements IIdCard {
    @Override
    public boolean checkIdCard(String idCardNo) {
        System.out.println("check");
        return false;
    }
}
