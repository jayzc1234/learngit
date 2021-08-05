package com.zxs.server.job.gugeng;

import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageProductDataStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 区域排名定时任务类（用于每天更新区域信息排名信息）
 * 采用在每天晚上的 00：00：00 时间进行任务执行
 * 采用此时间段的弊端：如果系统时间发生手动更改，
 * 会造成更新时间在错误的时间段更新，可能会造成部分数据丢失
 *
 * 如果定时任务更新失败，是否需要采用重试机制, 可在后期进行优化
 * @author shixiongfei
 * @date 2019-09-19
 * @since V1.3
 */
@Slf4j
@Component
public class GreenhouseRankingJob {

    @Autowired
    private ProductionManageProductDataStatisticsService productDataStatisticsService;

    /**
     * 统计当天的区域排名，每天的零点时进行统计
     *
     * {秒数} {分钟} {小时} {日期} {月份} {星期} {年份(可为空)}
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void statisticsGreenhouseRank() {
        // TODO 先赶数据分析的内容，这个优化内容需要在后续迭代

    }
}