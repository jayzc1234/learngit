package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSortInstorageStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchWarehouseDataStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.WarehouseDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSortInstorageStatisticsResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchWarehouseDataStatisticsResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * 仓储数据统计控制器
 * @author shixiongfei
 * @date 2019-09-21
 * @since V1.3
 */
@RestController
@RequestMapping(VALID_PATH+"/statistics/warehouse-data")
@Api(tags = "仓储数据统计")
@Slf4j
public class WarehouseDataStatisticsController {

    @Autowired
    private WarehouseDataStatisticsService service;

    @GetMapping("/statistics/list")
    @ApiOperation(value = "获取仓储数据统计", notes = "获取仓储数据统计")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchWarehouseDataStatisticsResponseVO> listStatistics(SearchWarehouseDataStatisticsRequestDTO requestDTO) {
        return RestResult.success(service.listStatistics(requestDTO));
    }

    @GetMapping("/statistics/listSortInstorageStatistics")
    @ApiOperation(value = "获取分拣入库统计列表", notes = "获取分拣入库统计列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSortInstorageStatisticsResponseVO>>> listSortInstorageStatistics(SearchSortInstorageStatisticsRequestDTO requestDTO)throws Exception {
        return RestResult.success(service.listSortInstorageStatistics(requestDTO));
    }

    @PostMapping(value = "/statistics/exportSortInstorageStatistics")
    @ApiOperation(value = "导出分拣入库统计列表", notes = "导出分拣入库统计列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportSortInstorageStatistics(SearchSortInstorageStatisticsRequestDTO requestDTO, HttpServletResponse response)
            throws Exception {
        service.exportSortInstorageStatistics(requestDTO, response);
    }


}