package com.zxs.server.controller.gugeng.repairmanage;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductManageQualityControlInspectionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.ProductManageQualityControlInspectionListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageQualityControlInspection;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductManageQualityControlInspectionService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductManageQualityControlInspectionListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/qualitycontrolinspection")
@Api(value = "品控管理", tags = "品控管理")
public class ProductManageQualityControlInspectionController  extends CommonUtil {

    @Autowired
    private ProductManageQualityControlInspectionService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增品控巡检", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody ProductManageQualityControlInspectionDTO inspectionManageDTO) throws SuperCodeException, ParseException {
        service.add(inspectionManageDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "问题处理及品控确认", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody ProductManageQualityControlInspectionDTO inspectionManageDTO) throws SuperCodeException, ParseException {
        service.update(inspectionManageDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "查看详情", notes = "")
    public RestResult getById(@RequestParam("id") Long id) throws SuperCodeException {
        ProductManageQualityControlInspectionListVO vo=new ProductManageQualityControlInspectionListVO();
        ProductionManageQualityControlInspection inspection= service.getById(id);
        BeanUtils.copyProperties(inspection, vo);
        return RestResult.success(vo);
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductManageQualityControlInspectionListVO>>> list(ProductManageQualityControlInspectionListDTO inspectionManageListDTO) throws SuperCodeException {
        AbstractPageService.PageResults<List<ProductManageQualityControlInspectionListVO>>pageResults =service.list(inspectionManageListDTO);
        return RestResult.success(pageResults);
    }


    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductManageQualityControlInspectionListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "品控管理", ProductManageQualityControlInspectionListVO.class, response);
    }


}
