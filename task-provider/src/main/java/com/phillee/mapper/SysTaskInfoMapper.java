package com.phillee.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.phillee.vo.SysTaskInfo;

/**
* @author phillee
* @description 针对表【sys_task_info】的数据库操作Mapper
* @createDate 2023-03-02 15:46:52
* @Entity phillee.vo.SysTaskInfo
*/
public interface SysTaskInfoMapper extends BaseMapper<SysTaskInfo> {


    SysTaskInfo selectByTaskIndex(String taskIndex);
}
