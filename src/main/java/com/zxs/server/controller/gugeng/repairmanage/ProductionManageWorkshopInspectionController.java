package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageWorkshopInspectionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductionManageWorkshopInspectionListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageWorkshopInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-12
 */
@RestController
@RequestMapping(VALID_PATH+"/workshopinspection")
@Api(value = "分拣车间卫生检查", tags = "分拣车间卫生检查")
public class ProductionManageWorkshopInspectionController  extends CommonUtil {

    @Autowired
    private ProductionManageWorkshopInspectionService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated ProductionManageWorkshopInspectionDTO inspectionDTO) throws SuperCodeException {
        service.add(inspectionDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated ProductionManageWorkshopInspectionDTO inspectionDTO) throws SuperCodeException {
        service.update(inspectionDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "查看", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult getById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getById(id));
    }

    @DeleteMapping("/deleteOne")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "删除", notes = "")
    public RestResult deleteOne(@RequestParam("id") Long id) throws SuperCodeException {
        service.delete(id);
        return RestResult.success();
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(ProductionManageWorkshopInspectionListDTO inspectionManageListDTO) throws SuperCodeException {
        inspectionManageListDTO.setPageSize(1);
        return RestResult.success(service.list(inspectionManageListDTO));
    }

    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageWorkshopInspectionListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, getExportNumber(), "分拣车间卫生检查",  response);
    }
}
