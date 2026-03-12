package com.ajaxjs.framework.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmpService {
    //编辑雇员
    public Emp edit(Emp emp) {
        return Emp.builder().build();
    }

    //根据id查询雇员信息
    @Cacheable(cacheNames = "emp")
    public Emp get(String eid) {
        return Emp.builder().ename("Tom").build();
    }

    //根据名称查询雇员信息
    @Cacheable(cacheNames = "emp")
    public Emp getEname(String ename) {
        return Emp.builder().ename("Jack").build();
    }
}

