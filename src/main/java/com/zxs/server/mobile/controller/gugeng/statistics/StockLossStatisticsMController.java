package com.zxs.server.mobile.controller.gugeng.statistics;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossPageRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsReportLossService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchStockLossStatisticsResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * @author shixiongfei
 * @date 2019-09-18
 * @since
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/statistics/stock-loss")
@Api(tags = "库存报损分析")
@Slf4j
public class StockLossStatisticsMController {

    @Autowired
    private ProductionManageStatisticsReportLossService service;

    @PostMapping("/statistics/list")
    @ApiOperation(value = "获取库存报损数据统计", notes = "获取库存报损数据统计")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchStockLossStatisticsResponseVO> listStatistics(@RequestBody @Validated SearchStockLossPageRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.listStockLossLineChart(requestDTO));
    }

    @PostMapping("/list")
    @ApiOperation(value = "获取库存报损列表", notes = "通过时间段来获取库存报损的列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchStockLossPageResponseVO>>> list(@RequestBody @Validated SearchStockLossPageRequestDTO requestDTO) throws SuperCodeException {
        CustomAssert.isFalse(LocalDate.parse(requestDTO.getStartQueryDate()).isAfter(LocalDate.parse(requestDTO.getEndQueryDate())),
                "开始日期不可大于结束日期");
        return RestResult.success(service.listStockLossStatistics(requestDTO));
    }
}