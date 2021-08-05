package com.zxs.server.controller.gugeng.synchronous_data;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.server.job.gugeng.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.synchronous_data.SynchronousDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * 数据同步controller，不对外暴露接口
 * 仅用于数据同步，所有的数据同步不可嵌入到业务controller中
 *
 * @author shixiongfei
 * @date 2019-09-26
 * @since v1.8
 */
@Slf4j
@RestController
@Api(tags = "数据同步管理")
@RequestMapping(VALID_PATH +"/data-synchronous")
public class SynchronousDataController {

    @Autowired
    private SynchronousDataService service;

    @Autowired
    private GreenhouseHarvestWeightStatisticsJob greenhouseHarvestWeightStatisticsJob;

    @Autowired
    private ReportLossStatisticsJob reportLossStatisticsJob;

    @Autowired
    private ProductRecordDataStatisticsJob productRecordDataStatisticsJob;

    @Autowired
    private ShippingDataStatisticsJob shippingDataStatisticsJob;

    @Autowired
    private SaleTargetDataStatisticsJob saleTargetDataStatisticsJob;

    @Autowired
    private ProductProductionStatisticsJob productProductionStatisticsJob;

    @Autowired
    private ProductionYieldDataStatisticsJob productionYieldDataStatisticsJob;

    @GetMapping("/plant-stock-sync")
    @ApiOperation(value = "种植存量数据同步", notes = "种植存量数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult plantStockStatisticsData() {
        log.info("种植存量数据同步开始");
        service.plantStockStatisticsData();
        log.info("种植存量数据同步完成");
        return RestResult.success("种植存量数据同步完成");
    }

    @GetMapping("/greenhouse-sync")
    @ApiOperation(value = "区域数据同步", notes = "区域数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult greenhouseJob() {
        greenhouseHarvestWeightStatisticsJob.statisticsData();
        return RestResult.success("区域数据同步执行完成");
    }

    @GetMapping("/stock-loss-sync")
    @ApiOperation(value = "库存报损数据同步", notes = "库存报损数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult stockLossJob() {
        reportLossStatisticsJob.statisticsData();
        return RestResult.success("库存报损数据同步步执行完成", null);
    }

    @GetMapping("/product-record-data-sync")
    @ApiOperation(value = "产品档案数据同步器（只可调用一次）", notes = "产品档案数据同步器（只可调用一次）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<String> productRecordDataSync() {
        productRecordDataStatisticsJob.statisticsData();
        return RestResult.success("产品档案数据同步执行完成");
    }

    @GetMapping("/shipping-data-job")
    @ApiOperation(value = "发货数据统计同步执行器（仅用于测试使用）", notes = "发货数据统计同步执行器（仅用于测试使用）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult shippingDataJob() {
        shippingDataStatisticsJob.statisticsData();
        return RestResult.success("发货数据同步执行完成", null);
    }

    @GetMapping("/sale-target-data-job")
    @ApiOperation(value = "销售目标数据统计同步执行器（仅用于测试使用）", notes = "销售目标数据统计同步执行器（仅用于测试使用）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult saleTargetDataJob() {
        saleTargetDataStatisticsJob.statisticsData();
        return RestResult.success("销售目标数据同步执行完成", null);
    }

    @GetMapping("/product-production-job")
    @ApiOperation(value = "产品产量数据统计同步执行器（仅用于测试使用）", notes = "产品产量数据统计同步执行器（仅用于测试使用）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult productProductionJob() {
        productProductionStatisticsJob.statisticsData();
        return RestResult.success("产品产量数据同步执行完成", null);
    }

    @GetMapping("/production-yield-data-job")
    @ApiOperation(value = "生产产量 + 产量对比数据统计同步执行器（仅用于测试使用）", notes = "生产产量 + 产量对比数据统计同步执行器（仅用于测试使用）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult productionYieldDataJob() {
        productionYieldDataStatisticsJob.statisticsData();
        return RestResult.success("生产产量 + 产量对比数据同步执行完成", null);
    }

}