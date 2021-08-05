package com.zxs.server.mobile.controller.gugeng.salemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.ExcelImportDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageClientDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageClientListDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageClientService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageClientListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageClientDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

@RestController
@RequestMapping(VALID_MOBILE_PATH+"/client")
@Api(tags = "客户管理")
public class ProductManageClientMController extends CommonUtil {

	@Autowired
	private ProductManageClientService service;

	@PostMapping("/add")
	@ApiOperation(value = "客户新增", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> add(@Valid @RequestBody ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
		service.add(clientDTO);
		return RestResult.success();
	}

	@DeleteMapping("/delete")
    @ApiOperation(value = "删除客户", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "客户主键", required = true)
    })
	public RestResult<String> delete(@RequestParam Long id) throws SuperCodeException{
		service.delete(id);
		return RestResult.success(); 
	}
	
	@PostMapping("/update")
	@ApiOperation(value = "客户编辑", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> update(@RequestBody ProductManageClientDTO clientDTO) throws SuperCodeException, ParseException {
		service.update(clientDTO);
		return RestResult.success();
	}

	/**
	 * 客户详情
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	@ApiOperation(value = "客户详情", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
			@ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "客户主键", required = true), })
	public RestResult<ProductionManageClientDetailVO> detail(@RequestParam Long id) throws Exception {
		RestResult<ProductionManageClientDetailVO> restResult = new RestResult<>();
		ProductionManageClientDetailVO vo = service.selectDetailById(id);
		restResult.setState(200);
		restResult.setResults(vo);
		return restResult;
	}
	/**
	 * 客户列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	@ApiOperation(value = "客户列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductManageClientListVO>>> list(ProductManageClientListDTO clientListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductManageClientListVO>>> restResult = new RestResult<>();
		PageResults<List<ProductManageClientListVO>> pageResults = service.mobilePage(clientListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}

	/**
	 * 客户下拉列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dropPage", method = RequestMethod.GET)
	@ApiOperation(value = "客户下拉列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductManageClientListVO>>> dropPage(ProductManageClientListDTO clientListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductManageClientListVO>>> restResult = new RestResult<>();
		PageResults<List<ProductManageClientListVO>> pageResults = service.dropPage(clientListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}

	/**
	 * 客户信息导出
	 */
	@EnableRateLimiter
	@NeedAdvancedSearch
	@PostMapping("/export")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void clientExport(ProductManageClientListDTO clientListDTO, HttpServletResponse response) throws Exception {
		service.exportExcel(clientListDTO, getExportNumber(), "客户信息", response);
	}

	/**
	 * 客户信息导入
	 */
	@PostMapping("/import")
	@ApiOperation(value = "客户信息导入", notes = "客户信息导入")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult clientImport(@RequestBody ExcelImportDTO importDTO) throws SuperCodeException {
		service.clientImport(importDTO);
		return RestResult.success();
	}
}
