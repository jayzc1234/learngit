package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageHarvestDamageListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestDamage;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.config.excel.RateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageHarvestDamageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zc
 * @since 2019-06-14
 */
@RestController
@RequestMapping(VALID_PATH+"/harvest/damage")
@Api(tags = "采收报损")
public class ProductionManageHarvestDamageController {

	@Autowired
	private ProductionManageHarvestDamageService service;

	@Autowired
	private CommonUtil commonUtil;
	
	@PostMapping("/add")
	@ApiOperation(value = "采收报损新增", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> add(@RequestBody ProductionManageHarvestDamage pManageHarvestDamage) throws SuperCodeException, ParseException{
		service.add(pManageHarvestDamage);
		return RestResult.success();
	}
	
	@PostMapping("/update")
	@ApiOperation(value = "采收报损编辑", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> update(@RequestBody ProductionManageHarvestDamage pManageHarvestDamage) throws SuperCodeException, ParseException{
		service.update(pManageHarvestDamage);
		return RestResult.success();
	}
	
	
	/**
	 * 采收报损列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page",method = {RequestMethod.POST,RequestMethod.GET})
	@ApiOperation(value = "采收报损列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageHarvestDamage>>> list(ProductionManageHarvestDamageListDTO pHarvestDamageListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductionManageHarvestDamage>>> restResult = new RestResult<>();
		PageResults<List<ProductionManageHarvestDamage>> pageResults = service.page(pHarvestDamageListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}

	/**
	 * 导出Excel
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value = "/exportExcel")
	@ApiOperation(value = "导出Excel", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	@EnableRateLimiter
	@NeedAdvancedSearch
	public void exportExcel(@Valid ProductionManageHarvestDamageListDTO pHarvestDamageListDTO, HttpServletResponse response)
			throws SuperCodeException {
		service.exportDamageExcel(pHarvestDamageListDTO, response);
	}


	@GetMapping(value = "/hi")
	@ApiOperation(value = "限流测试接口", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	@EnableRateLimiter
	public RestResult  test( ) throws Exception {
		return RestResult.success();
	}
	@Autowired
	private RateLimiter rateLimiter;

}

