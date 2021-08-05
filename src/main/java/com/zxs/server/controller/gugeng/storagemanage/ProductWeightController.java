package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddProductWeightRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductWeightPageDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.UpdateProductWeightRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageWeight;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageWeightService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchWeightResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
@RestController
@RequestMapping(VALID_PATH+"/storagemanage/productWeight")
@Api(tags = "产品称重")
public class ProductWeightController extends CommonUtil {

    @Autowired
    private ProductionManageWeightService service;

    @PostMapping("/save")
    @ApiOperation(value = "产品称重: 保存", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddProductWeightRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "产品称重: 编辑", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody UpdateProductWeightRequestDTO requestDTO) throws SuperCodeException {
         service.update(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "产品称重: 详情", notes = "")
    public RestResult<ProductionManageWeight> getById(@RequestParam("id") String id) throws SuperCodeException {
        ProductionManageWeight byId = service.getById(id);
        return RestResult.success(byId);
    }


    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "产品称重: 分页", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchWeightResponseVO>>> list(ProductWeightPageDTO param) throws SuperCodeException {
        return RestResult.success(service.page(param));
    }
    
    
	/**
	 * 信息导出
	 */
	@EnableRateLimiter
	@NeedAdvancedSearch
	@PostMapping("/export")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void export(ProductWeightPageDTO listDTO, HttpServletResponse response) throws Exception {
		service.exportExcel(listDTO, getExportNumber(), "分拣报损信息", response);
	}

    @NeedAdvancedSearch
    @GetMapping("/list-half-year-msg")
    @ApiOperation(value = "获取半年的称重数值信息", notes = "获取半年的称重数值信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult listByHalfYearMsg() {
        return RestResult.success(service.listByHalfYearMsg());
    }
}