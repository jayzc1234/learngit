package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageInspectionManageDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageInspectionManageListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageInspectionManageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageInspectionManageListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-30
 */
@RestController
@RequestMapping(VALID_PATH+"/inspectionManage")
@Api(value = "巡检管理", tags = "巡检管理")
public class ProductionManageInspectionManageController extends CommonUtil {

    @Autowired
    private ProductionManageInspectionManageService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody ProductionManageInspectionManageDTO inspectionManageDTO) throws SuperCodeException, ParseException {
        service.add(inspectionManageDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody ProductionManageInspectionManageDTO inspectionManageDTO) throws SuperCodeException, ParseException {
        service.update(inspectionManageDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "详情", notes = "")
    public RestResult getById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getById(id));
    }

    @PutMapping("/inspection")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "执行", notes = "")
    public RestResult inspection(@RequestParam("id") Long id) throws SuperCodeException {
        service.inspection(id);
        return RestResult.success();
    }

    @PutMapping("/doneInspection")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "完成执行", notes = "")
    public RestResult doneInspection(@RequestBody ProductionManageInspectionManageDTO inspectionManageDTO) throws SuperCodeException {
        service.doneInspection(inspectionManageDTO);
        return RestResult.success();
    }

    @DeleteMapping("/deleteOne")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "删除", notes = "")
    public RestResult deleteOne(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteOne(id);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageInspectionManageListVO>>> list(ProductionManageInspectionManageListDTO inspectionManageListDTO) throws SuperCodeException {
        return RestResult.success(CommonUtil.iPageToPageResults(service.pageList(inspectionManageListDTO),null));
    }

    /**
     * 信息导出
     */
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageInspectionManageListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "巡检管理",ProductionManageInspectionManageListVO.class, response);
    }
}
