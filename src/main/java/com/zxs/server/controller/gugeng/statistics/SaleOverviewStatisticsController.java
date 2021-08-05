package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleProductChartDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSTDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.BaseSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsSaleTargetDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleOverviewStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.CustomerNumResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.LineChartListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SaleAndOrderNumVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDHResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/saleoverview")
@Api(tags = "销售数据概览")
@Slf4j
public class SaleOverviewStatisticsController {

    @Autowired
    private SaleOverviewStatisticsService statisticsService;

    @Autowired
    private SaleSummaryStatisticsService saleSummaryStatisticsService;

    @Autowired
    private ProductionManageStatisticsSaleTargetDataService service;

    /*@GetMapping("/selectSalesGoals")
    @ApiOperation(value = "销售目标", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<List<LineChartVO>> selectSalesGoals(String queryDate) throws Exception {
        return new RestResult(200, "success", statisticsService.selectSalesGoals(queryDate));
    }*/

    @GetMapping("/histogram")
    @ApiOperation(value = "销售目标数据柱状图", notes = "销售目标数据柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSTDHResponseVO> listHistogram(String queryDate) {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        startDate=startDate.minusMonths(2);

        SearchSTDRequestDTO dateIntervalDTO=new SearchSTDRequestDTO();
        dateIntervalDTO.setStartQueryDate(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateIntervalDTO.setEndQueryDate(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        SearchSTDHResponseVO responseVO= service.listHistogram(dateIntervalDTO);
        BigDecimal totalSaleTarget = responseVO.getTargetSaleAmounts().get(responseVO.getTargetSaleAmounts().size()-1);
        responseVO.setTotalSaleTarget(totalSaleTarget);
        return RestResult.success(responseVO);
    }

    @GetMapping("/selectSaleOrderList")
    @ApiOperation(value = "订单情况", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<LineChartListVO> selectSaleOrderList(String queryDate) throws Exception {
        return new RestResult(200, "success", new LineChartListVO(statisticsService.selectSaleOrderList(queryDate)));
    }

    @GetMapping("/selectCustomerNumList")
    @ApiOperation(value = "客户数据", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<CustomerNumResponseVO> selectCustomerNumList(String queryDate) throws Exception {
        return new RestResult(200, "success", statisticsService.selectCustomerNumList(queryDate));
    }

    @GetMapping("/selectSaleProduct")
    @ApiOperation(value = "销售产品", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<LineChartVO> selectSaleProduct(SaleProductChartDTO dto) throws Exception {
        return new RestResult(200, "success", statisticsService.selectSaleProduct(dto));
    }

    @GetMapping("/amountAndNum")
    @ApiOperation(value = "销售额实收额", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<SaleAndOrderNumVO> add(String queryDate) throws SuperCodeException, ParseException {
        RestResult<SaleAndOrderNumVO> result= saleSummaryStatisticsService.amountAndNum(saleSummaryStatisticsService.getQueryDTO(queryDate));
        result.getResults().getValues().remove(1);
        return result;
    }

}
