package com.zxs.server.controller.gugeng.purchasing;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.UpdateSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.purchasing.ProductionManageProcurementSupplierService;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchSupplierResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  采购供应商管理前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageProcurementSupplier")
@Api(value = "采购供应商controller", tags = "采购供应商管理")
public class ProductionManageProcurementSupplierController {

    @Autowired
    private ProductionManageProcurementSupplierService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增采购供应商", notes = "新增采购供应商")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddSupplierRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑采购供应商", notes = "编辑采购供应商")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody @Validated UpdateSupplierRequestDTO requestDTO) throws SuperCodeException {
        service.update(requestDTO);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取采购供应商列表", notes = "获取采购供应商列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchSupplierResponseVO>>> list(SearchSupplierRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO,true));
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "下拉接口", notes = "下拉接口")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchSupplierResponseVO>>> dropDown(SearchSupplierRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO,false));
    }

    @DeleteMapping("/remove/{id}")
    @ApiOperation(value = "删除采购供应商", notes = "删除采购供应商")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult remove(@PathVariable("id") Long id) throws SuperCodeException {
        service.remove(id);
        return RestResult.success();
    }

    @GetMapping("/get-by-name")
    @ApiOperation(value = "获取采购供应商", notes = "通过采购供应商名称获取采购供应商信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<SearchSupplierResponseVO>> listBySupplierName(@RequestParam("supplierName") String supplierName) {
        return RestResult.success(service.listBySupplierName(supplierName));
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取采购供应商详情信息", notes = "通过id获取采购供应商详情信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSupplierResponseVO> getDetailById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getDetailById(id));
    }

    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "采购供应商列表导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(SearchSupplierRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }
}