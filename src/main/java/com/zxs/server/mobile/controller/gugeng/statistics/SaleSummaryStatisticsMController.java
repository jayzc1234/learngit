package com.zxs.server.mobile.controller.gugeng.statistics;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
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
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleOrderDayStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SaleAndOrderNumVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

@RestController
@RequestMapping(VALID_MOBILE_PATH+"/statistics/salesummary")
@Api(tags = "销售总数据统计")
@Slf4j
public class SaleSummaryStatisticsMController {

    @Autowired
    private SaleSummaryStatisticsService statisticsService;

    @GetMapping("/order/amountAndNum")
    @ApiOperation(value = "销售额，订单数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<SaleAndOrderNumVO> add(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return statisticsService.amountAndNum(dateIntervalDTO);
    }

    @GetMapping("/order/listAmountAndNum")
    @ApiOperation(value = "每日销售数据列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageSaleOrderDayStatisticsVO>>> listAmountAndNum(SaleUserLineChartDTO dateIntervalDTO)throws Exception {
        return RestResult.success(statisticsService.listAmountAndNum(dateIntervalDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportOrderAmountAndNum")
    @ApiOperation(value = "导出每日销售数据列表excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportOrderAmountAndNum(SaleUserLineChartDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportOrderAmountAndNum(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("导出每日销售数据列表失败",e);
            CommonUtil.throwSupercodeException(500,"导出每日销售数据列表失败");
        }
    }


}
