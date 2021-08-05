package com.zxs.server.controller.gugeng.purchasing;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddPurchaseProductListRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchProductFromSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchPurchaseProductsRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.UpdatePurchaseProductRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.purchasing.ProductionManagePurchaseProductsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchDetermineProductResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchProductAndSupplierDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchProductFromSupplierResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchPurchaseProductsResponseVO;
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
@RequestMapping(VALID_PATH+"/productionManagePurchaseProducts")
@Api(value = "采购商品维护controller", tags = "采购商品维护管理")
public class ProductionManagePurchaseProductsController {

    @Autowired
    private ProductionManagePurchaseProductsService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增采购商品", notes = "新增采购商品")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddPurchaseProductListRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑采购商品", notes = "编辑采购商品")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdatePurchaseProductRequestDTO requestDTO) throws SuperCodeException {
        service.update(requestDTO);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取采购商品列表", notes = "获取采购商品列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchPurchaseProductsResponseVO>>> list(SearchPurchaseProductsRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "移除采购商品", notes = "通过id移除采购商品")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult remove(@PathVariable("id") Long id) throws SuperCodeException {
        service.remove(id);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "采购商品列表导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(SearchPurchaseProductsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }

    @NeedAdvancedSearch
    @PostMapping("/export-by-supplier")
    @ApiOperation(value = "通过指定供应商导出采购商品列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportFromSupplier(SearchProductFromSupplierRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.exportFromSupplier(requestDTO, response);
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取采购商品详情", notes = "获取采购商品详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchProductAndSupplierDetailResponseVO> getDetailById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getDetailById(id));
    }

    @GetMapping("/list-from-supplier")
    @ApiOperation(value = "通过供应商来获取采购商品列表", notes = "通过供应商来获取采购商品列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchProductFromSupplierResponseVO>>> listFromSupplier(SearchProductFromSupplierRequestDTO requestDTO) throws SuperCodeException {
        CustomAssert.numberIsLegal(requestDTO.getSupplierId(), "供应商id不可为空");
        return RestResult.success(service.listFromSupplier(requestDTO));
    }

    @GetMapping("/list-by-id-and-name")
    @ApiOperation(value = "获取供应商id和商品名称获取采购商品信息", notes = "获取供应商id和商品名称获取采购商品信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<SearchDetermineProductResponseVO>> listBySupplierIdAndProductName(
            @RequestParam("supplierId") Long supplierId, @RequestParam("productName") String productName) throws SuperCodeException {
        return RestResult.success(service.listBySupplierIdAndProductName(supplierId, productName));
    }

}