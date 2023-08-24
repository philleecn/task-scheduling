package com.phillee.vo;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName sys_task_log
 */
@TableName(value ="sys_task_log")
@Data
@Builder
public class SysTaskLog implements Serializable {
    private String serverName;

    private String taskIndex;

    private String taskName;

    private String taskBean;

    private String scheduleId;

    private Integer success;

    private Date scheduleTime;

    private String exceptionMessage;

    private String exceptionStack;

    private static final long serialVersionUID = 1L;
}