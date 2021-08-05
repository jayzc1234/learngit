package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProduceSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageProduceWeightDayStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.RealWeightAndLossesWeightVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/producesummary")
@Api(tags = "生产总数据统计")
@Slf4j
public class ProduceSummaryStatisticsController {

    @Autowired
    private ProduceSummaryStatisticsService produceSummaryStatisticsService;

    @GetMapping("/product/realWeightAndLossesWeight")
    @ApiOperation(value = "产量，采收报损", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<RealWeightAndLossesWeightVO> realWeightAndLossesWeight(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        return new RestResult(200, "success", produceSummaryStatisticsService.realWeightAndLossesWeight(dateIntervalDTO));
    }

    @GetMapping("/product/listRealWeightAndLossesWeight")
    @ApiOperation(value = "产量与采收报损数据列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageProduceWeightDayStatisticsVO>>> listRealWeightAndLossesWeight(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        return RestResult.success(produceSummaryStatisticsService.listRealWeightAndLossesWeight(dateIntervalDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportRealWeightAndLossesWeight")
    @ApiOperation(value = "导出产量与采收报损数据列表excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportRealWeightAndLossesWeight(SaleUserLineChartDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            produceSummaryStatisticsService.exportRealWeightAndLossesWeight(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("产量与采收报损数据失败",e);
            CommonUtil.throwSupercodeException(500,"导出产量与采收报损数据列表失败");
        }
    }

    @GetMapping("/dataSync")
    @ApiOperation(value = "数据同步", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Void> statistics() throws Exception {
        produceSummaryStatisticsService.statisticsProduceData();
        return RestResult.success();
    }

}
