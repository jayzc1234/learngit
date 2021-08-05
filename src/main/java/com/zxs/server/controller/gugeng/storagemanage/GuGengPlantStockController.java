package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlantStockRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.GuGengPlantStockService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchBatchPSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductPSResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 种植存量表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-09
 */
@RestController
@RequestMapping(VALID_PATH+"/guGengPlantStock")
@Api(value = "种植存量controller", tags = "种植存量管理")
public class GuGengPlantStockController {

    @Autowired
    private GuGengPlantStockService service;


    @NeedAdvancedSearch
    @RequestMapping("/list-batch")
    @ApiOperation(value = "获取种植存量-批次列表", notes = "获取种植存量-批次列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchBatchPSResponseVO>>> listBatch(SearchPlantStockRequestDTO requestDTO) {
        return RestResult.success(service.listBatch(requestDTO));
    }

    @NeedAdvancedSearch
    @RequestMapping("/list-product")
    @ApiOperation(value = "获取种植存量-产品列表", notes = "获取种植存量-产品列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchProductPSResponseVO>>> listProduct(SearchPlantStockRequestDTO requestDTO) {
        return RestResult.success(service.listProduct(requestDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export-batch")
    @ApiOperation(value = "批次-种植存量导出", notes = "批次-种植存量导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportBatch(SearchPlantStockRequestDTO requestDTO, HttpServletResponse response) {
        service.exportBatch(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export-product")
    @ApiOperation(value = "产品-种植存量导出", notes = "产品-种植存量导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportProduct(SearchPlantStockRequestDTO requestDTO, HttpServletResponse response) {
        service.exportProduct(requestDTO, response);
    }

}