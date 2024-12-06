package com.ajaxjs.service.tools;

import com.ajaxjs.service.IService;

public interface IIdCard extends IService {
    boolean checkIdCard(String idCardNo);
}
