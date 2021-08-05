package com.zxs.server.mobile.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairmanage.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.repairmanage.ExportRepairApplyPdfService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageRepairManageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageRepairManageDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageRepairManageListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-29
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/repairManage")
@Api(value = "维修管理", tags = "维修管理")
public class ProductionManageRepairManageMController extends CommonUtil {

    @Autowired
    private ProductionManageRepairManageService service;

    @Autowired
    private ExportRepairApplyPdfService applyPdfService;

    @PostMapping("/callRepair")
    @ApiOperation(value = "维修申请", notes = "维修申请")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult callRepair(@RequestBody ProductionManageRepairApplyDTO repairApplyDTO) throws SuperCodeException, ParseException {
        service.callRepair(repairApplyDTO);
        return RestResult.success();
    }

    @PostMapping("/assignRepair")
    @ApiOperation(value = "维修指派")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult assignRepair(@RequestBody ProductionManageRepairAssignDTO assignDTO) throws SuperCodeException, ParseException {
        service.assignRepair(assignDTO);
        return RestResult.success();
    }

    @PostMapping("/runRepair")
    @ApiOperation(value = "执行维修")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult runRepair(@RequestBody ProductionManageRunRepairDTO runRepairDTO) throws SuperCodeException, ParseException {
        service.runRepair(runRepairDTO);
        return RestResult.success();
    }

    @PostMapping("/doneRepair")
    @ApiOperation(value = "维修完成")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult doneRepair(@RequestBody ProductionManageRepairDoneDTO repairDoneDTO) throws SuperCodeException, ParseException {
        service.doneRepair(repairDoneDTO);
        return RestResult.success();
    }

    @PostMapping("/doneConfirm")
    @ApiOperation(value = "完成确定")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult doneConfirm(@RequestBody ProductionManageRepairConfirmDoneManageDTO confirmDoneManageDTO) throws SuperCodeException, ParseException {
        service.doneConfirm(confirmDoneManageDTO);
        return RestResult.success();
    }

    @PostMapping("/commentRepair")
    @ApiOperation(value = "维修评价")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult commentRepair(@RequestBody ProductionManageRepairCommentDTO commentDTO) throws SuperCodeException, ParseException {
        service.commentRepair(commentDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody ProductionManageRepairApplyDTO repairApplyDTO) throws SuperCodeException {
        service.update(repairApplyDTO);
        return RestResult.success();
    }

    @DeleteMapping("/deleteOne")
    @ApiOperation(value = "删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult deleteOne(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteOne(id);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "查看")
    public RestResult<ProductionManageRepairManageDetailVO> getById(@RequestParam("id") Long id) throws SuperCodeException {
        ProductionManageRepairManageDetailVO repairManageDetailVO=service.detail(id);
        return RestResult.success(repairManageDetailVO);
    }

    @NeedAdvancedSearch
    @GetMapping("/list")
    @ApiOperation(value = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageRepairManageListVO>>> list(ProductionManageRepairManageListDTO repairManageListDTO) throws SuperCodeException {
        return RestResult.success(CommonUtil.iPageToPageResults(service.pageList(repairManageListDTO),null));
    }

    @PostMapping("/exportPdf")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "导出pdf", notes = "")
    public void exportPdf(@RequestParam("id") Long id, HttpServletResponse response) throws Exception {
        response.setHeader("Content-disposition", "attachment;filename=工程维修单.pdf");
        applyPdfService.exportPdf(id,response.getOutputStream());
    }

    /**
     * 信息导出
     */
    @NeedAdvancedSearch
    @PostMapping("/exportExcel")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageRepairManageListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, getExportNumber(), "维修管理", response);
    }
}
