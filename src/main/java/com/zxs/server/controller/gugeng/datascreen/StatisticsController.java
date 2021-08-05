package com.zxs.server.controller.gugeng.datascreen;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.datascreen.StatisticsRequestDto;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageStatisticsRecord;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageStatisticsRecordEx;
import net.app315.hydra.intelligent.planting.server.service.gugeng.datascreen.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@Api(tags = "数据屏")
@RequestMapping(VALID_PATH+"/datascreen/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping("/save")
    @ApiOperation(value = "保存数据")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated StatisticsRequestDto requestDTO) throws Exception {
        statisticsService.save(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(paramType="query",value = "页面id",name="pageId",required=true)
    })
    public RestResult<ProductionManageStatisticsRecordEx> getByPageId(String pageId) throws Exception {
        return RestResult.success(statisticsService.selectPageContent(pageId));
    }

  /*  @GetMapping("/getHarvestPlanList")
    @ApiOperation(value = "获取最新采收计划数据", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<HarvestPlanMonthVo>> selectHarvestPlanList() throws Exception{
        return new RestResult(200, "success", statisticsService.selectHarvestPlanList());
    }*/

    @GetMapping("/selectList")
    @ApiOperation(value = "获取列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    })
    public RestResult<List<ProductionManageStatisticsRecord>> selectList() throws Exception {
        return RestResult.success(statisticsService.selectList());
    }

}
