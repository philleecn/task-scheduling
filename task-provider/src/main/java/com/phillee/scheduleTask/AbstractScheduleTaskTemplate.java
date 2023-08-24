package com.phillee.scheduleTask;

import cn.hutool.core.util.StrUtil;
import com.phillee.mapper.SysTaskInfoMapper;
import com.phillee.mapper.SysTaskLogMapper;
import com.phillee.vo.SysTaskInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RList;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 任务调度抽象类
 * @Author: PhilLee
 * @Date: 2023/3/2 15:15
 */
@Slf4j
public abstract class AbstractScheduleTaskTemplate {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SysTaskInfoMapper taskInfoMapper;

    @Resource
    private SysTaskLogMapper taskLogMapper;

    @Resource
    private DiscoveryClient discoveryClient;

    public String taskIndex;

    public SysTaskInfo taskInfo;

    public Exception exception;

    // 从数据库查询本任务是否开启
    protected boolean beforeTask() {
        log.info("任务调度，检测任务状态");
        SysTaskInfo taskInfo = taskInfoMapper.selectByTaskIndex(taskIndex);
        this.taskInfo = taskInfo;
        log.info("任务调度: {} 状态: {}", taskInfo.getTaskName(), taskInfo.getTaskEnable() == 1);
        return taskInfo.getTaskEnable() == 1;
    }

    // 抽象的任务调度逻辑，有子类实现
    protected abstract  void runTask(String... args) throws Exception;

    // 持久化日志，子类实现，也可以此处统一处理
    protected abstract void afterTask();

    public final void execute(String taskIndex) {
        this.taskIndex = taskIndex;

        if (!beforeTask()) {
            return;
        }

        String taskBean = taskInfo.getExecuteBean();
        String[] args = null;
        int i = StrUtil.indexOf(taskBean, '(');
        if (i > 0) {
            String params = StrUtil.sub(taskBean, i + 1, taskBean.length() - 1);
            List<String> split = StrUtil.split(params, ",");
            args = new String[split.size()];
            args = split.toArray(args);
        }


        RList<Object> waitList = redissonClient.getList(lockName() + "WAITLIST");

        RCountDownLatch count = redissonClient.getCountDownLatch(lockName() + "WAITLOCK");
        RPermitExpirableSemaphore lock = redissonClient.getPermitExpirableSemaphore(lockName() + "RUNLOCK");

        // 从注册中心获取当前节点数量
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("task-provider");
        int serverPoints = serviceInstances.size();
        log.info("任务调度：从注册中心获取的节点数量: {}", serverPoints);

        lock.trySetPermits(1);
        if (waitList.size() == 0) {
            count.trySetCount(serverPoints);
        }

        if (waitList.size() < serverPoints) {
            // 本机加入waitList
            waitList.add(new Object());
            count.countDown();

            try {
                count.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            count.countDown();
        }
        waitList.clear();

        String haveLock = null;
        try {

            log.info("{}", lockName());
            haveLock = lock.tryAcquire();
            if (StrUtil.isBlank(haveLock)) {
                log.info("任务调度：{}未拿到锁", taskIndex);
                return;
            }

            log.info("任务调度：{}拿到锁，执行任务", taskIndex);
            scheduledTask(taskIndex);
        } catch (Exception e) {
            log.info("任务调度异常：{}", e.getMessage());
        } finally {
            log.info("任务调度：{}, 解锁", taskIndex);
            if (StrUtil.isNotBlank(haveLock)) {
                lock.release(haveLock);
            }
        }

        afterTask();

    }

    private void scheduledTask(String... args) {
        try {
            this.runTask(args);
        } catch (Exception e) {
            this.exception = e;
            log.info("任务调度：{}, 发生异常 -> 记录异常", args);
            // saveLog();
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ignored) {

            }
        }
    }

    private String lockName() {
        return MessageFormat.format("SCHEDULETASK:LOCK:{0}", taskIndex);
    }

}
