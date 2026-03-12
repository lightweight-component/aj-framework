package com.ajaxjs.framework.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Emp {
    private String eid;
    private String ename;
    private String job;
    private Double salary;
}
