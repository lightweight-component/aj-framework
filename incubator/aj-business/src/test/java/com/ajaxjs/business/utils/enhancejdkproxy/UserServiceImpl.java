package com.ajaxjs.business.utils.enhancejdkproxy;

/**
 * @author xujian
 * 2021-05-26 11:32
 **/
public class UserServiceImpl implements UserService {
    @Override
    public String print() {
        System.out.println("----UserService的业务方法");
        return "我是业务方法返回值";
    }
}
