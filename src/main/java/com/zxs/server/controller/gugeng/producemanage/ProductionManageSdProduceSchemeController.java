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
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProductionManageSdProduceSchemeNodeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageSdProduceScheme;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageSdProduceSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
@RequestMapping(VALID_PATH+"/sd/produce/scheme")
@Api(tags = "标准化生产计划方案")
public class ProductionManageSdProduceSchemeController extends CommonUtil {

	@Autowired
	private ProductionManageSdProduceSchemeService service;
	
	@PostMapping("/add")
	@ApiOperation(value = "标准化计划方案新增", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> add(@RequestBody ProductionManageSdProduceSchemeDTO sdProduceSchemeDTO) throws SuperCodeException, ParseException{
		service.add(sdProduceSchemeDTO);
		return RestResult.success();
	}
	
	@DeleteMapping("/delete")
    @ApiOperation(value = "删除标准化方案", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "方案id主键", required = true)
    })
	public RestResult<String> delete(@RequestParam Long id) throws SuperCodeException{
		service.delete(id);
		return RestResult.success(); 
	}
	
	@PostMapping("/update")
	@ApiOperation(value = "标准化计划方案编辑", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> update(@RequestBody ProductionManageSdProduceSchemeDTO sdProduceSchemeDTO) throws SuperCodeException, ParseException{
		service.update(sdProduceSchemeDTO);
		return RestResult.success();
	}
	
	/**
	 * 标准化方案列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page",method = {RequestMethod.POST,RequestMethod.GET})
	@ApiOperation(value = "列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageSdProduceScheme>>> list(ProductionManageSdProduceSchemeListDTO pSchemeListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductionManageSdProduceScheme>>> restResult = new RestResult<>();
		PageResults<List<ProductionManageSdProduceScheme>> pageResults = service.page(pSchemeListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}
	
	/**
	 * 标准化方案节点列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/detail")
	@ApiOperation(value = "方案详细信息", notes = "")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
		@ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "方案主键", required = true),
		})
	public RestResult<ProductionManageSdProduceScheme> detail(@RequestParam Long id)
			throws Exception {
		ProductionManageSdProduceScheme prSdProduceScheme= service.selectById(id);
		return RestResult.successDefault(prSdProduceScheme);
	}
	
	
	/**
	 * 导出Excel
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@EnableRateLimiter
	@PostMapping(value = "/exportExcel")
	@ApiOperation(value = "导出Excel", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void exportExcel(ProductionManageSdProduceSchemeNodeListDTO pSchemeNodeListDTO, HttpServletResponse response)
			throws Exception {
		service.exportSchemeExcelService(pSchemeNodeListDTO, response);
	}
	/**
	 * 信息导出
	 */
	@EnableRateLimiter
	@NeedAdvancedSearch
	@ApiOperation(value = "标准化方案列表导出Excel", notes = "")
	@PostMapping("/exportList")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void export(ProductionManageSdProduceSchemeListDTO listDTO, HttpServletResponse response) throws Exception {
		service.exportExcel(listDTO, getExportNumber(), "标准化生产方案信息", response);
	}

	/**
	 * 导出excel
	 */
	@EnableRateLimiter
	@NeedAdvancedSearch
	@PostMapping("/list/exportExcel")
	@ApiOperation(value = "导出生产方案列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void exportSchemeListExcel(@RequestBody ProductionManageSdProduceSchemeListDTO pSchemeListDTO, HttpServletResponse response) throws SuperCodeException {
		service.exportSchemeListExcel(pSchemeListDTO, response);
	}
}

