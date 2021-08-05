package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionYieldDataRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.YieldComparisonRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductionYieldDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionYieldDataResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.YieldComparisonHistogramVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.YieldComparisonResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-16
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStatisticsProductionYieldData")
@Api(value = "生产产量数据controller", tags = "生产产量数据统计")
public class ProductionManageStatisticsProductionYieldDataController {

    @Autowired
    private ProductionManageStatisticsProductionYieldDataService service;

    @GetMapping("/list-by-date-interval")
    @ApiOperation(value = "生产产量数据列表(不含分页)", notes = "生产产量数据列表(不含分页)")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionYieldDataResponseVO>> listByDateInterval(ProductionYieldDataRequestDTO requestDTO) {
        if (StringUtils.isBlank(requestDTO.getQueryDate())) {
            throw new SuperCodeExtException("采收时间不可为空");
        }
        return RestResult.success(service.listByDateInterval(requestDTO));
    }

    @GetMapping("/list")
    @ApiOperation(value = "生产产量数据列表(含分页)", notes = "生产产量数据列表(含分页)")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionYieldDataResponseVO>>> list(ProductionYieldDataRequestDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getQueryDate())) {
            throw new SuperCodeExtException("采收时间不可为空");
        }
        return RestResult.success(service.list(requestDTO));
    }

    @PostMapping("/export")
    @ApiOperation(value = "生产产量数据导出", notes = "生产产量数据导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionYieldDataRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }

    @GetMapping("/list-yield-comparison-data-histogram")
    @ApiOperation(value = "产量对比数据统计柱状图", notes = "产量对比数据统计柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<YieldComparisonHistogramVO> listYieldComparisonDataHistogram(YieldComparisonRequestDTO requestDTO) {
        return RestResult.success(service.listYieldComparisonDataHistogram(requestDTO));
    }

    @GetMapping("/list-yield-comparison-data")
    @ApiOperation(value = "产量对比数据统计列表(含分页)", notes = "产量对比数据统计列表(含分页)")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<YieldComparisonResponseVO>>> listYieldComparisonData(YieldComparisonRequestDTO requestDTO) {
        return RestResult.success(service.listYieldComparisonData(requestDTO));
    }

    @PostMapping("/comparison-export")
    @ApiOperation(value = "产量对比数据导出", notes = "产量对比数据导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void comparisonExport(YieldComparisonRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.comparisonExport(requestDTO, response);
    }
}