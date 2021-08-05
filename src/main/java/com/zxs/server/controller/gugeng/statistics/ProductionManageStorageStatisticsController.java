package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStorageStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/storage")
@Api(tags = "仓储数据统计")
public class ProductionManageStorageStatisticsController {

    @Autowired
    private ProductionManageStorageStatisticsService storageStatisticsService;

    @GetMapping("/order/amountAndNum")
    @ApiOperation(value = "销售额，订单数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<LineChartVO> add(DateIntervalDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return storageStatisticsService.outInWeightStatistics(dateIntervalDTO);
    }

}
