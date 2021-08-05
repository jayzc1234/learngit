package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockFlowDetailDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffStockService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockBatchDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockFlowDetailListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffStockListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
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
@RequestMapping(VALID_PATH+"/stuff/stock")
@Api(value = "材料库存", tags = "材料库存")
public class ProductionManageStuffStockController extends CommonUtil {

    @Autowired
    private ProductionManageStuffStockService service;

    @GetMapping("/detail")
    @ApiOperation(value = "详情", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<ProductionManageStuffStockDetailVO> getById(@RequestParam("publicStuffId") String publicStuffId, @RequestParam("stuffSpecificationId") Long stuffSpecificationId) throws SuperCodeException {
        ProductionManageStuffStockDetailVO stuffStockDetailVO=service.detail(publicStuffId,stuffSpecificationId);
        return RestResult.success(stuffStockDetailVO);
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "库存列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffStockListVO>>> list(ProductionManageStuffStockListDTO stuffStockListDTO) throws SuperCodeException {
        return RestResult.success(CommonUtil.iPageToPageResults(service.pageList(stuffStockListDTO),null));
    }

    @GetMapping("/batchDetailList")
    @ApiOperation(value = "批次详情", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffStockBatchDetailListVO>>> batchDetailList(ProductionManageStuffStockFlowDetailDTO specificationListDTO) throws SuperCodeException {
        AbstractPageService.PageResults<List<ProductionManageStuffStockBatchDetailListVO>>pageResults=service.batchDetailList(specificationListDTO);
        return RestResult.success(pageResults);
    }

    @GetMapping("/flowDetailList")
    @ApiOperation(value = "流水详情", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffStockFlowDetailListVO>>> flowDetailList(ProductionManageStuffStockFlowDetailDTO specificationListDTO) throws SuperCodeException {
        AbstractPageService.PageResults<List<ProductionManageStuffStockFlowDetailListVO>>pageResults=service.flowDetailList(specificationListDTO);
        return RestResult.success(pageResults);
    }

    @GetMapping("/boundCode")
    @ApiOperation(value = "获取出入库编号", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<String> boundCode() throws SuperCodeException, ParseException {
        return RestResult.success(service.getBoundCode());
    }

    @PutMapping("/warningInventory")
    @ApiOperation(value = "库存预警", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<String> warningInventory(@ApiParam(name = "id",value = "库存主键id") @RequestParam("id") Long id, @ApiParam(name = "num",value = "最小库存数")@RequestParam("num") Double num) throws SuperCodeException, ParseException {
        service.warningInventory(id,num);
        return RestResult.success();
    }


    @GetMapping("/outboundCheck")
    @ApiOperation(value = "出库库存校验", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult outboundCheck(@RequestParam("publicStuffId") String publicStuffId, @RequestParam("stuffSpecificationId") Long stuffSpecificationId, @RequestParam(value = "stuffBatch",required = false) String stuffBatch, @RequestParam("outboundNum") BigDecimal outboundNum) throws SuperCodeException, ParseException {
        service.outboundCheck(publicStuffId,stuffSpecificationId,stuffBatch,outboundNum);
        return RestResult.success();
    }

    /**
     * 信息导出
     */
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageStuffStockListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "维修管理",ProductionManageStuffStockListVO.class, response);
    }
}
