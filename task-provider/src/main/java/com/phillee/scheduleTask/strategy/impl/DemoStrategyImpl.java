package com.phillee.scheduleTask.strategy.impl;

import com.phillee.scheduleTask.strategy.DemoStrategy;
import com.phillee.vo.SysTaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description: 策略一实现类
 * @Author: PhilLee
 * @Date: 2023/8/24 15:13
 */
@Slf4j
@Service("demoStrategyImpl")
public class DemoStrategyImpl implements DemoStrategy {
    @Override
    public String test(SysTaskInfo taskInfo) {
        return "策略一，执行";
    }
}
