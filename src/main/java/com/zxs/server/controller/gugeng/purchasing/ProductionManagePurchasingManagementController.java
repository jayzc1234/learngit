package com.zxs.server.controller.gugeng.purchasing;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.*;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.purchasing.ProductionManagePurchasingManagementService;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchDetermineSupAndProductResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchPurchasingDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchPurchasingManagementResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  采购管理前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-11
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManagePurchasingManagement")
@Api(value = "采购管理controller", tags = "采购管理")
public class ProductionManagePurchasingManagementController {

    @Autowired
    private ProductionManagePurchasingManagementService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增采购申请", notes = "新增采购申请")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddPurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑采购管理", notes = "编辑采购管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdatePurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        service.update(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取采购管理详情信息", notes = "获取采购管理详情信息")
    public RestResult<SearchPurchasingDetailResponseVO> getDetailById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getDetailById(id));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取采购管理列表", notes = "获取采购管理列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchPurchasingManagementResponseVO>>> list(SearchPurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "移除采购管理信息", notes = "通过id移除采购管理信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult remove(@PathVariable("id") Long id) throws SuperCodeException {
        service.remove(id);
        return RestResult.success();
    }

    @PutMapping("/update-order-date")
    @ApiOperation(value = "更新下单日期", notes = "更新下单日期")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateOrderDate(@RequestBody @Validated UpdatePurchasingManagementOrderDateRequestDTO requestDTO) throws SuperCodeException {
        service.updateOrderDate(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/update-except-arrival-date")
    @ApiOperation(value = "更新预计到货日期(到货提醒)", notes = "更新预计到货日期（到货提醒）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateExceptArrivalDate(@RequestBody @Validated UpdateExceptArrivalDateRequestDTO requestDTO) throws SuperCodeException {
        service.updateExceptArrivalDate(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/confirm-receipt")
    @ApiOperation(value = "确认收货", notes = "确认收货")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult confirmReceipt(@RequestBody @Validated UpdateConfirmReceiptRequestDTO requestDTO) throws SuperCodeException {
        service.confirmReceipt(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/save-supplier-and-price")
    @ApiOperation(value = "添加供应商及价格", notes = "添加供应商及价格")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult saveOrUpdate(@RequestBody @Validated AddPurchasingDetermineProductRequestDTO requestDTO) throws SuperCodeException {
        service.addOrUpdatePurchasingProduct(requestDTO);
        return RestResult.success();
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "采购管理列表导出", notes = "采购管理列表导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchPurchasingManagementRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }

    @GetMapping("/get-supplier-by-id")
    @ApiOperation(value = "获取确定供应商及价格信息", notes = "通过主键id获取确定供应商及价格信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchDetermineSupAndProductResponseVO> getSupplierAndPriceById(@RequestParam("id") Long id) {
        return RestResult.success(service.getSupplierAndPriceById(id));
    }


    @PostMapping("/export-pdf")
    @ApiOperation(value = "采购管理导出pdf", notes = "采购管理导出pdf")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPdf(@RequestParam("id") Long id, HttpServletResponse response) throws SuperCodeException {
        service.exportPdf(id, response);
    }

}