package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateSeedPlanRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageSeedPlanService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SeedPlanResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  育苗计划前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-14
 */
@RestController
@RequestMapping(VALID_PATH+"/seed-plan")
@Api(value = "育苗计划controller", tags = "育苗计划")
public class ProductionManageSeedPlanController {

    @Autowired
    private ProductionManageSeedPlanService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增育苗计划", notes = "新增育苗计划")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑育苗计划", notes = "编辑育苗计划")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdateSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        service.update(requestDTO);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取育苗计划列表", notes = "获取育苗计划列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SeedPlanResponseVO>>> list(SearchSeedPlanRequestDTO requestDTO) throws SuperCodeException {
        PageResults<List<SeedPlanResponseVO>> pageResult = service.list(requestDTO);
        return RestResult.success(pageResult);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除育苗计划", notes = "通过id指定的育苗计划")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult delete(@RequestParam("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "育苗计划主键id不可为空");
        service.delete(id);
        return RestResult.success();
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportExcel")
    @ApiOperation(value = "采收计划导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(SearchSeedPlanRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }
}