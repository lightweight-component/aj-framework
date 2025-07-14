/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.ajaxjs.devtools.sysmonitor.model;

import lombok.Data;

/**
 * CPU 信息
 */
@Data
public class CpuInfo  {
    /**
     * 物理处理器数量
     */
    private int physicalProcessorCount;

    /**
     * 逻辑处理器数量
     */
    private int logicalProcessorCount;

    /**
     * 系统使用率
     */
    private double systemPercent;

    /**
     * 用户使用率
     */
    private double userPercent;

    /**
     * 当前等待率
     */
    private double waitPercent;

    /**
     * 当前使用率
     */
    private double usePercent;
}
