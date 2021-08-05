package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.PlantWarehouseDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPlantBatchResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPlantWarehouseDataStatisticsResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * @author shixiongfei
 * @date 2019-09-22
 * @since
 */
@RestController
@RequestMapping(VALID_PATH+"/statistics/plant-warehouse")
@Api(tags = "种植仓储数据统计管理")
@Slf4j
public class PlantWarehouseDataStatisticsController {

    @Autowired
    private PlantWarehouseDataStatisticsService service;

    @GetMapping("/list")
    @ApiOperation(value = "获取种植仓储数据统计列表", notes = "获取种植仓储数据统计列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "plantBatchId", paramType = "query", defaultValue = "64b379cd47c843458378f479a115c322", value = "种植批次id", required = true)
    })
    public RestResult<SearchPlantWarehouseDataStatisticsResponseVO> list(@RequestParam("plantBatchId") String plantBatchId) throws SuperCodeException {
        return RestResult.success(service.list(plantBatchId));
    }

    @GetMapping("/plant-batch-message/list")
    @ApiOperation(value = "获取种植仓储数据种植批次列表", notes = "获取种植仓储数据种植批次列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "search", paramType = "query", defaultValue = "64b379cd47c843458378f479a115c322", value = "搜索值(非必填)", required = false)
    })
    public RestResult<List<SearchPlantBatchResponseVO>> listByPlantBatchId(@RequestParam(value = "search", required = false) String search) {
        return RestResult.success(service.listByPlantBatchId(search));
    }

    @GetMapping("/plant-batch-message/default")
    @ApiOperation(value = "获取种植仓储数据默认的种植批次信息", notes = "获取种植仓储数据默认的种植批次信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchPlantBatchResponseVO> getDefault() throws SuperCodeException {
        return RestResult.success(service.getDefault());
    }

    @PostMapping("/export")
    @ApiOperation(value = "种植仓储数据统计导出", notes = "种植仓储数据统计导出")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "plantBatchId", paramType = "query", defaultValue = "64b379cd47c843458378f479a115c322", value = "种植批次id", required = true)
    })
    public void export(String plantBatchId, HttpServletResponse response) throws SuperCodeException {

        service.export(plantBatchId, response);
    }
}