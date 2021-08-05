package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchDataStatisticsDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManagePlantBatchDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PlantingBatchDataStatisticsListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 生产数据统计表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */
@RestController
@RequestMapping(VALID_PATH+"/plantBatch/data/statistics")
@Api(value = "种植批次数据", tags = "种植批次数据")
public class ProductionManagePlantBatchDataStatisticsController {

    @Autowired
    private ProductionManagePlantBatchDataStatisticsService service;

    @Autowired
    private CommonUtil commonUtil;

    @GetMapping("/listPlantBatchData")
    @ApiOperation(value = "列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<PlantingBatchDataStatisticsListVO>>> list(PlantingBatchDataStatisticsDTO plantingBatchDataStatisticsDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<PlantingBatchDataStatisticsListVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<PlantingBatchDataStatisticsListVO>> pageResults= service.page(plantingBatchDataStatisticsDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }


    /**
     * 信息导出
     */
    @ApiOperation(value = "导出Excel", notes = "")
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(PlantingBatchDataStatisticsDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, commonUtil.getExportNumber(), "批次数据", response);
    }
}