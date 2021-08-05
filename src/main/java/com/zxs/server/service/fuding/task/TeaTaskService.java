package com.zxs.server.service.fuding.task;

/**
 * @Author:chengaunyu
 * @Date:2019/11/20
 */
public interface TeaTaskService {
    /**
     * 茶青统计任务方法
     * @param dateTime   yyyy-MM-dd
     * @param orgId 组织id
     */
    void teaGreenTask(String orgId, String dateTime);
}
