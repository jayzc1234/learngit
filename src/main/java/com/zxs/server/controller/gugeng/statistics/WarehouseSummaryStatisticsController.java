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
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.WarehouseSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.InstorageAndOutboundWeightVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageStorageStockDayStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/warehousesummary")
@Api(tags = "仓储总数据统计")
@Slf4j
public class WarehouseSummaryStatisticsController {

    @Autowired
    private WarehouseSummaryStatisticsService warehouseSummaryStatisticsService;

    @GetMapping("/stock/instorageAndOutboundWeight")
    @ApiOperation(value = "出库入库，库存重量", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<InstorageAndOutboundWeightVO> instorageAndOutboundWeight(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        return new RestResult(200, "success", warehouseSummaryStatisticsService.instorageAndOutboundWeight(dateIntervalDTO));
    }

    @GetMapping("/stock/listInstorageAndOutboundWeight")
    @ApiOperation(value = "出库入库与库存重量数据列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStorageStockDayStatisticsVO>>> listInstorageAndOutboundWeight(SaleUserLineChartDTO dateIntervalDTO) throws Exception {
        return RestResult.success(warehouseSummaryStatisticsService.listInstorageAndOutboundWeight(dateIntervalDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportInstorageAndOutboundWeight")
    @ApiOperation(value = "导出出库入库与库存重量数据列表excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportInstorageAndOutboundWeight(SaleUserLineChartDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            warehouseSummaryStatisticsService.exportInstorageAndOutboundWeight(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("出库入库与库存重量数据失败",e);
            CommonUtil.throwSupercodeException(500,"导出出库入库与库存重量数据列表失败");
        }
    }

}
