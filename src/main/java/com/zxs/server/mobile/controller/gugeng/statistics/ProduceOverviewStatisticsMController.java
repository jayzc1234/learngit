package com.zxs.server.mobile.controller.gugeng.statistics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionYieldDataRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.YieldComparisonRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProduceSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsNonProductProductionDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductionYieldDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionYieldDataResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.RealWeightAndLossesWeightVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.YieldComparisonHistogramVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

@RestController
@RequestMapping(VALID_MOBILE_PATH+"/statistics/produceoverview")
@Api(tags = "生产数据概览")
@Slf4j
public class ProduceOverviewStatisticsMController {


    @Autowired
    private ProductionManageStatisticsProductionYieldDataService service;

    @Autowired
    private ProduceSummaryStatisticsService produceSummaryStatisticsService;

    @Autowired
    private ProductionManageStatisticsNonProductProductionDataService productionDataService;

    @GetMapping("/product/realWeightAndLossesWeight")
    @ApiOperation(value = "计划产量，产量，采收报损", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<RealWeightAndLossesWeightVO> realWeightAndLossesWeight(String queryDate) throws Exception {
        return new RestResult(200, "success", produceSummaryStatisticsService.realWeightAndLossesWeight(produceSummaryStatisticsService.getQueryDTO(queryDate)));
    }

    @NeedAdvancedSearch
    @GetMapping("/list-by-date-interval")
    @ApiOperation(value = "商品产品亩产量, 亩产量", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionYieldDataResponseVO>> listByDateInterval(String queryDate) {
        ProductionYieldDataRequestDTO requestDTO=new ProductionYieldDataRequestDTO();
        requestDTO.setQueryDate(queryDate);
        return RestResult.success(service.listByDateInterval(requestDTO));
    }

    @GetMapping("/list-yield-comparison-data-histogram")
    @ApiOperation(value = "生产产量数据统计柱状图", notes = "生产产量数据统计柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<YieldComparisonHistogramVO> listYieldComparisonDataHistogram(String queryDate, String greenhouseId, String greenhouseName) {
        YieldComparisonRequestDTO requestDTO=new YieldComparisonRequestDTO();
        BeanUtils.copyProperties(produceSummaryStatisticsService.getQueryDTO(queryDate), requestDTO);
        requestDTO.setGreenhouseName(greenhouseName);
        return RestResult.success(service.listYieldComparisonDataHistogram(requestDTO));
    }

    @GetMapping("/histogram")
    @ApiOperation(value = "获取产品产量数据柱状图", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchPPDResponseVO> histogram(String queryDate, String productId) {
        SearchPPDRequestDTO requestDTO=new SearchPPDRequestDTO();
        BeanUtils.copyProperties(produceSummaryStatisticsService.getQueryDTO(queryDate), requestDTO);
        requestDTO.setProductId(productId);
        return RestResult.success(productionDataService.histogram(requestDTO));
    }
}
