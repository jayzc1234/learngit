package com.zxs.server.controller.gugeng.recruit;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.recruit.SearchRecruitDetailResponseVO;
import com.jgw.supercodeplatform.productmanagement.vo.recruit.SearchRecruitManageResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.recruit.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.recruit.ProductionManageRecruitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageRecruit")
@Api(value = "用工管理控制器", tags = "用工管理")
public class ProductionManageRecruitController {

    @Autowired
    private ProductionManageRecruitService service;


    @PostMapping("/save")
    @ApiOperation(value = "新增用工申请", notes = "新增用工申请")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddRecruitRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑用工申请", notes = "编辑用工申请")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdateRecruitRequestDTO requestDTO) throws SuperCodeException {
        service.update(requestDTO);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取用工管理列表", notes = "获取用工管理列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchRecruitManageResponseVO>>> list(SearchRecruitManageRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }


    @PutMapping("/execute-recruit")
    @ApiOperation(value = "执行招聘", notes = "执行招聘，修改用工状态为招聘中")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult executeRecruit(@RequestBody @Validated UpdateRecruitInExecuteRequestDTO requestDTO) throws SuperCodeException {
        service.executeRecruit(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/completed-recruit")
    @ApiOperation(value = "招聘完成", notes = "招聘完成，修改用工状态为招聘完成")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult completedRecruit(@RequestBody @Validated UpdateRecruitInCompleteRequestDTO requestDTO) throws SuperCodeException {
        service.completedRecruit(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/detail/{id}")
    @ApiOperation(value = "获取用工管理详情信息", notes = "获取用工管理详情信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchRecruitDetailResponseVO> getDetail(@PathVariable("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "主键id不可为空");
        return RestResult.success(service.getDetail(id));
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "删除用工信息", notes = "删除用工信息，通过主键id来完成删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult remove(@PathVariable("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "主键id不可为空");
        service.remove(id);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出用工信息列表", notes = "导出用工信息列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchRecruitManageRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }

    @GetMapping("/export-pdf")
    @ApiOperation(value = "导出用工信息pdf", notes = "导出用工信息pdf")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPdf(@RequestParam("id") Long id, HttpServletResponse response) throws SuperCodeException {
        service.exportPdf(id, response);
    }
}