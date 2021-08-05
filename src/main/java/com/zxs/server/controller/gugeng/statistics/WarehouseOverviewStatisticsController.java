package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchHSLRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsReportLossService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsShippingDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.WarehouseOverviewStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.WarehouseSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.InstorageAndOutboundWeightVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchHSLLineChartResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSDLCResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/warehouseoverview")
@Api(tags = "仓储数据概览")
@Slf4j
public class WarehouseOverviewStatisticsController {

    @Autowired
    private WarehouseSummaryStatisticsService warehouseSummaryStatisticsService;

    @Autowired
    private ProductionManageStatisticsShippingDataService shippingDataService;

    @Autowired
    private ProductionManageStatisticsReportLossService reportLossService;

    @Autowired
    private WarehouseOverviewStatisticsService overviewStatisticsService;

    @GetMapping("/stock/instorageAndOutboundWeight")
    @ApiOperation(value = "出库入库，库存重量", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<InstorageAndOutboundWeightVO> instorageAndOutboundWeight(String queryDate, String productId) throws Exception {
        if (StringUtils.isBlank(queryDate)) {
            throw new SuperCodeExtException("日期不可为空");
        }
        SaleUserLineChartDTO dateIntervalDTO=warehouseSummaryStatisticsService.getQueryDTO(queryDate);
        dateIntervalDTO.setProductId(productId);
        return new RestResult(200, "success", warehouseSummaryStatisticsService.instorageAndOutboundWeight(dateIntervalDTO));
    }

    @GetMapping("/hsl-line-chart")
    @ApiOperation(value = "采收报损及分拣报损数据统计折线图", notes = "采收报损及分拣报损数据统计折线图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchHSLLineChartResponseVO> listHSLLineChart(String queryDate, String productId) throws Exception {
        if (StringUtils.isBlank(queryDate)) {
            throw new SuperCodeExtException("日期不可为空");
        }
        SearchHSLRequestDTO requestDTO=new SearchHSLRequestDTO();
        BeanUtils.copyProperties(warehouseSummaryStatisticsService.getQueryDTO(queryDate), requestDTO);
        SearchHSLLineChartResponseVO responseVO= new SearchHSLLineChartResponseVO();
        responseVO.setValues(new ArrayList<>());
        // reportLossService.listHSLLineChart(requestDTO);
        LineChartVO chartVO= overviewStatisticsService.listStockLoss(queryDate, responseVO);
        //responseVO.getValues().add(chartVO);
        return RestResult.success(responseVO);
    }

    @GetMapping("/list-line-chart")
    @ApiOperation(value = "发货数据统计折线图", notes = "发货数据统计折线图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSDLCResponseVO> listLineChart(String queryDate, String productId) throws SuperCodeException {
        if (StringUtils.isBlank(queryDate)) {
            throw new SuperCodeExtException("日期不可为空");
        }
        SearchSDRequestDTO requestDTO=new SearchSDRequestDTO();
        BeanUtils.copyProperties(warehouseSummaryStatisticsService.getQueryDTO(queryDate), requestDTO);
        return RestResult.success(shippingDataService.listLineChart(requestDTO));
    }

}
