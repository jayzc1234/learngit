package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddHarvestPlanAndPlanInformRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchHarvestPlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchProductionForecastRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateHarvestPlanAndPlanInformRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.HarvestPlanMonthVo;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.datascreen.StatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageHarvestPlanService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchHarvestPlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchHarvestPlanWithInformResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchProductionForecastResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 *  采收计划前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
@RestController
@Api(value = "采收计划controller", tags = "采收计划")
@RequestMapping(VALID_PATH+"/harvest-plan")
public class ProductionManageHarvestPlanController {

    @Autowired
    private ProductionManageHarvestPlanService harvestPlanService;

    @Autowired
    private StatisticsService statisticsService;

    @PostMapping("/save")
    @ApiOperation(value = "新增采收计划", notes = "新增采收计划")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddHarvestPlanAndPlanInformRequestDTO requestDTO) throws SuperCodeException {
        harvestPlanService.add(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑采收计划", notes = "编辑采收计划")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdateHarvestPlanAndPlanInformRequestDTO requestDTO) throws SuperCodeException {
        harvestPlanService.update(requestDTO);
        return RestResult.success();
    }

    //@NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取采收计划列表", notes = "获取采收计划列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchHarvestPlanResponseVO>>> list(SearchHarvestPlanRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(harvestPlanService.list(requestDTO));
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取采收计划详情", notes = "获取采收计划详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchHarvestPlanWithInformResponseVO> getByPrimaryKey(@RequestParam(value = "id") Long id) throws SuperCodeException {
        return RestResult.success(harvestPlanService.getByPrimaryKey(id));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/production-forecast/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取产量预测列表", notes = "获取产量预测列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchProductionForecastResponseVO>>> listForProductionForecast(SearchProductionForecastRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(harvestPlanService.listForProductionForecast(requestDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportExcel")
    @ApiOperation(value = "采收计划导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(SearchHarvestPlanRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        harvestPlanService.export(requestDTO, response);
    }

    @ApiOperation(value = "移除采收计划", notes = "通过id来移除指定的采收计划")
    @PostMapping("/delete")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult delete(@RequestParam("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "采收计划主键id不可为空");
        harvestPlanService.delete(id);
        return RestResult.success();
    }

    @ApiOperation(value = "获取生产计划列表-数据屏", notes = "")
    @GetMapping("/select-harvest-plan")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult<List<HarvestPlanMonthVo>> selectHarvestPlanList() throws Exception {
        return RestResult.success(statisticsService.selectHarvestPlanList());
    }
}