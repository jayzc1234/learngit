package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductAndSaleDataExcelDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageProductAndSaleDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductAndSaleDataResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 生产数据统计表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */
@Slf4j
@RestController
@RequestMapping(VALID_PATH+"/product/sale/statistics")
@Api(value = "生产销售数据统计管理", tags = "生产销售数据统计管理")
public class ProductionManageProductAndSaleDataStatisticsController {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageProductAndSaleDataStatisticsService service;

    @GetMapping("/lineChart")
    @ApiOperation(value = "生产销售数据折线图", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<ProductAndSaleDataResponseVO> add(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.lineChart(dateIntervalDTO));
    }

    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportProductSaleStatistics")
    @ApiOperation(value = "导出生产销售统计excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportProductSaleStatistics(ProductAndSaleDataExcelDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            service.exportProductSaleStatistics(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("导出生产销售统计",e);
            CommonUtil.throwSupercodeException(500,"导出生产销售统计");
        }
    }


}