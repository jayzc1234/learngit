package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStockStatisticsService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * 库存数据统计
 * @author zc
 */
@RestController
@RequestMapping(VALID_PATH+"/statistics/stock")
@Api(tags = "库存数据统计")
public class ProductionManageStockStatisticsController {

    @Autowired
    private ProductionManageStockStatisticsService stockStatisticsService;

    @GetMapping("/order/amountAndNum")
    @ApiOperation(value = "近六个月的库存", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RichResult<ProductionManageStockStatisticsService.QueryTimeVO> add(String productId) throws SuperCodeException, ParseException {
        return stockStatisticsService.nearlySixMonthsStock(productId);
    }

}
