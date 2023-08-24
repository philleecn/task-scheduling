package com.phillee.vo;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName sys_task_info
 */
@TableName(value ="sys_task_info")
@Data
public class SysTaskInfo implements Serializable {
    private String taskIndex;

    private String taskName;

    private String taskCron;

    private String executeBean;

    private Integer taskEnable;

    private Integer retryTime;

    private Integer isConcurrent;

    private static final long serialVersionUID = 1L;
}