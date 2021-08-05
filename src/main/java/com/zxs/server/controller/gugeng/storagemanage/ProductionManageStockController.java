package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddInventoryWarningRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductLevelRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductionManageStockListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageInventoryWarningService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageStockService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPLSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductionManageStockVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 库存表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@RestController
@RequestMapping(VALID_PATH+"/stock")
@Api(value = "产品库存信息管理", tags = "产品库存信息管理")
public class ProductionManageStockController extends CommonUtil {

    @Autowired
    private ProductionManageStockService service;

    @Autowired
    private ProductionManageInventoryWarningService warningService;

    @GetMapping("/detail")
    @ApiOperation(value = "库存详情", notes = "库存详情")
    public RestResult<ProductionManageStock> getById(@RequestParam String id) {
        return RestResult.success(service.getById(id));
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @NeedAdvancedSearch
    @ApiOperation(value = "库存列表", notes = "库存列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchProductionManageStockVO>>> list(ProductionManageStockListDTO stockListDTO) throws SuperCodeException {
        return RestResult.success(service.list(stockListDTO));
    }

    /**
     * 信息导出
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageStockListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, getExportNumber(), "库存信息", response);
    }

    @RequestMapping(value = "/product-level-list",method = {RequestMethod.POST,RequestMethod.GET})
    @NeedAdvancedSearch
    @ApiOperation(value = "产品等级库存列表", notes = "产品等级库存列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchPLSResponseVO>>> listWithProductLevel(ProductLevelRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.listWithProductLevel(requestDTO));
    }

    @PostMapping("/add-or-update-warning")
    @ApiOperation(value = "新增或编辑库存预警信息", notes = "新增或编辑库存预警信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult addOrUpdateWarning(@RequestBody @Validated AddInventoryWarningRequestDTO requestDTO) {
        // 校验产品等级是否为空
        if (StringUtils.isBlank(requestDTO.getProductLevelCode())) {
            requestDTO.setProductLevelCode(StringUtils.EMPTY);
        }
        warningService.addOrUpdateWarning(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/product-level-export")
    @NeedAdvancedSearch
    @ApiOperation(value = "产品等级库存导出", notes = "产品等级库存导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportWithProductLevel(ProductLevelRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.exportWithProductLevel(requestDTO, response);
    }
}