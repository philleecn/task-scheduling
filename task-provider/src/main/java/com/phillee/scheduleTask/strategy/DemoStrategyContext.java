package com.phillee.scheduleTask.strategy;

import com.phillee.vo.SysTaskInfo;

/**
 * @Description: 策略环境类
 * @Author: PhilLee
 * @Date: 2023/8/24 15:15
 */
public class DemoStrategyContext {

    private final DemoStrategy demoStrategy;

    public DemoStrategyContext(DemoStrategy demoStrategy) {
        this.demoStrategy = demoStrategy;
    }

    public String executeStrategy(SysTaskInfo taskInfo) {
        return demoStrategy.test(taskInfo);
    }
}
