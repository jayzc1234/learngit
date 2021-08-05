package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddProducePlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProducePlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateProducePlanRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageProducePlanService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ObjectUniqueValueResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ProducePlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchProducePlanWithInformResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * 生产计划控制器
 * @author shixiongfei
 * @since 2019-06-13
 */
@RestController
@Api(value = "生产计划controller", tags = "生产计划")
@RequestMapping(VALID_PATH+"/produce-plan")
public class ProductionManageProducePlanController {

    @Autowired
    private ProductionManageProducePlanService producePlanService;


    //@NeedAdvancedSearch
    @ApiOperation(value = "获取生产计划列表", notes = "通过指定查询条件获取生产计划列表")
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult<PageResults<List<ProducePlanResponseVO>>> list(ProducePlanRequestDTO requestDTO) throws Exception {
        return RestResult.success(producePlanService.list(requestDTO));
    }

    @ApiOperation(value = "新增计划", notes = "新增生产计划")
    @PostMapping("/save")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult save(@Validated @RequestBody AddProducePlanRequestDTO requestDTO) throws SuperCodeException {
        producePlanService.save(requestDTO);
        return RestResult.success();
    }

    @ApiOperation(value = "编辑计划", notes = "编辑生产计划")
    @PostMapping("/update")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult update(@Validated @RequestBody UpdateProducePlanRequestDTO requestDTO) throws SuperCodeException {
        producePlanService.update(requestDTO);
        return RestResult.success();
    }

    @ApiOperation(value = "获取生产计划详情", notes = "通过计划编号来获取指定的生产计划详情信息")
    @GetMapping("/detail")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult<SearchProducePlanWithInformResponseVO> getByProductionNo(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(producePlanService.getByProductionNo(id));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportExcel")
    @ApiOperation(value = "生产计划导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(ProducePlanRequestDTO requestDTO, HttpServletResponse response) throws Exception {
        producePlanService.export(requestDTO, response);
    }

    /**
     * 移除生产计划（默认采用逻辑删除）
     * @author shixiongfei
     * @date 2019-09-05
     * @since V1.1.1
     * @return
     */
    @ApiOperation(value = "移除生产计划", notes = "通过id来移除指定的生产计划")
    @PostMapping("/delete")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult delete(@RequestParam("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "生产计划主键id不可为空");
        producePlanService.delete(id);
        return RestResult.success();
    }

    @ApiOperation(value = "获取生产计划对象字段列表", notes = "")
    @GetMapping("/field")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult<PageResults<List<ObjectUniqueValueResponseVO>>> listPlanfield(ProducePlanRequestDTO requestDTO) throws Exception {
        return RestResult.success(producePlanService.listPlanfield(requestDTO));
    }
}