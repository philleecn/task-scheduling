package com.phillee.scheduleTask;

import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.pattern.parser.PatternParser;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.phillee.mapper.SysTaskInfoMapper;
import com.phillee.vo.SysTaskInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Description: 任务调度入口
 * @Author: PhilLee
 * @Date: 2023/3/2 16:10
 */
@Slf4j
@Component
public class ScheduleTaskDispatch implements ApplicationListener<ApplicationEvent> {

    @Resource
    private SysTaskInfoMapper taskInfoMapper;

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        
        // 载入任务调度
        loadScheduleTask();

        // 刷新修改后的任务调度
        refreshSchedule();

        // 启动任务
        CronUtil.start(true);
    }

    private void refreshSchedule() {
        CronUtil.schedule("0,5,10 59 23 * * *", (Task) () -> new Thread(() -> {
            {
                log.info("开始刷新任务配置");
                CronUtil.stop();
                loadScheduleTask();
                refreshSchedule();
                CronUtil.start(true);
                log.info("刷新任务配置成功");
            }
        }).start());
    }

    private void loadScheduleTask() {
        List<SysTaskInfo> taskInfoList = taskInfoMapper
                .selectList(new LambdaQueryWrapper<SysTaskInfo>()
                        .eq(SysTaskInfo::getTaskEnable, "1"));

        if (CollectionUtils.isEmpty(taskInfoList)) return;

        CronUtil.setMatchSecond(true);
        for (SysTaskInfo taskInfo : taskInfoList) {
            // 校验cron
            try {
                PatternParser.parse(taskInfo.getTaskCron());
                log.info("任务调度：{}, cron：{}", taskInfo.getTaskName(), taskInfo.getTaskCron());
            } catch (Exception e) {
                log.info("cron表达式校验失败：{}", taskInfo.getTaskCron());
                throw new RuntimeException("cron表达式校验失败：" + taskInfo.getTaskCron());
            }

            // 加载任务
            String scheduleId = CronUtil.schedule(taskInfo.getTaskCron(), (Task) () -> {
                String bean = taskInfo.getExecuteBean();
                String beanName;

                int i = StrUtil.indexOf(bean, '(');
                if (i > 0) {
                    beanName = StrUtil.sub(bean, 0, i);
                } else beanName = bean;

                AbstractScheduleTaskTemplate task = SpringUtil.getBean(beanName);
                task.execute(taskInfo.getTaskIndex());
            });

            log.info("当前任务调度taskName: {} -> ScheduleID: {}", taskInfo.getTaskIndex(), scheduleId);
        }
    }
}
