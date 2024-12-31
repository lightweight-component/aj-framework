package com.ajaxjs.service.tools;

import com.ajaxjs.service.IService;
import org.apache.dubbo.remoting.http12.HttpMethods;
import org.apache.dubbo.remoting.http12.rest.Mapping;

public interface IIdCard extends IService {
    @Mapping(path = "/hi", method = HttpMethods.GET)
    boolean checkIdCard(String idCardNo);
}
