package com.phillee.scheduleTask;

import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.phillee.mapper.SysTaskInfoMapper;
import com.phillee.mapper.SysTaskLogMapper;
import com.phillee.scheduleTask.strategy.DemoStrategyContext;
import com.phillee.scheduleTask.strategy.impl.DemoStrategyImpl;
import com.phillee.vo.SysTaskInfo;
import com.phillee.vo.SysTaskLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Description: 任务样例
 * @Author: PhilLee
 * @Date: 2023/3/2 16:11
 */
@Slf4j
@Scope("prototype")
@Component("demoTask")
public class DemoTask extends AbstractScheduleTaskTemplate {

    @Resource
    private SysTaskInfoMapper taskInfoMapper;

    @Resource
    private SysTaskLogMapper taskLogMapper;

    @Resource
    @Qualifier("demoStrategyImpl")
    private DemoStrategyImpl demoStrategy;

    @Override
    protected void runTask(String... args) throws Exception {
        List<SysTaskInfo> taskInfoList = taskInfoMapper.selectList(new LambdaQueryWrapper<SysTaskInfo>().eq(SysTaskInfo::getTaskIndex, args[0]));

        for (SysTaskInfo taskInfo : taskInfoList) {
            // 判断情况，选择对应策略执行
            if (1 == 1) {
                DemoStrategyContext context = new DemoStrategyContext(demoStrategy);
                String result = context.executeStrategy(taskInfo);
            }
        }
    }

    @Override
    protected void afterTask() {
        Exception innerException = super.exception;

        SysTaskLog taskLog = SysTaskLog.builder()
                .serverName(SystemUtil.getHostInfo().getAddress())
                .taskIndex(super.taskIndex)
                .taskName(super.taskInfo.getTaskName())
                .taskBean(super.taskInfo.getExecuteBean())
                .scheduleTime(new Date())
                .build();

        if (null == innerException) {
            taskLog.setSuccess(1);
        } else {
            taskLog.setSuccess(0);
            taskLog.setExceptionMessage(innerException.getMessage());
            taskLog.setExceptionStack(JSONUtil.toJsonStr(Arrays.copyOf(innerException.getStackTrace(), 5)));
        }

        taskLogMapper.insert(taskLog);

        // 清空本次异常
        super.exception = null;
    }
}
