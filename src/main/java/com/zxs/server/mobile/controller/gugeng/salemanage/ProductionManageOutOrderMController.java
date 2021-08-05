package com.zxs.server.mobile.controller.gugeng.salemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOutOrderDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOutOrderListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOutOrder;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageOutOrderService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOutOrderListVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/out/order")
@Api(tags = "外采订单管理")
public class ProductionManageOutOrderMController {

	@Autowired
	private ProductionManageOutOrderService service;


	@PostMapping("/add")
	@ApiOperation(value = "外采订单新增", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> add(@Valid @RequestBody ProductionManageOutOrderDTO pOrderDTO) throws SuperCodeException, ParseException{
		service.add(pOrderDTO);
		return RestResult.success();
	}

	@GetMapping("/detail")
	@ApiOperation(value = "外采订单详情", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<ProductionManageOutOrderListVO> detail(@RequestParam("id") Long id ) throws SuperCodeException, ParseException{
		ProductionManageOutOrder outOrder=service.getById(id);
		if (null==outOrder) {
			return RestResult.fail("订单不存在", null);
		}

		ProductionManageOutOrderListVO outOrderListVO=new ProductionManageOutOrderListVO();
		BeanUtils.copyProperties(outOrder,outOrderListVO);
		outOrderListVO.setOrderType(Optional.ofNullable(outOrder.getOrderType()+"").orElse("1"));
		return RestResult.success(outOrderListVO);
	}
	
	@PostMapping("/update")
	@ApiOperation(value = "外采订单更新", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> update(@Valid @RequestBody ProductionManageOutOrderDTO pOrderDTO) throws SuperCodeException, ParseException{
		service.update(pOrderDTO);
		return RestResult.success();
	}

	/**
	 * 外采订单列表
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	@ApiOperation(value = "外采订单列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageOutOrderListVO>>> list(ProductionManageOutOrderListDTO orderListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductionManageOutOrderListVO>>> restResult = new RestResult<>();
 		PageResults<List<ProductionManageOutOrderListVO>> pageResults = service.page(orderListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}
	/**
	 * 下拉列表
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/dropPage", method = RequestMethod.GET)
	@ApiOperation(value = "下拉列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageOutOrderListVO>>> dropPage(ProductionManageOutOrderListDTO clientListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductionManageOutOrderListVO>>> restResult = new RestResult<>();
		PageResults<List<ProductionManageOutOrderListVO>> pageResults = service.page(clientListDTO);
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
	@EnableRateLimiter
	@NeedAdvancedSearch
	@PostMapping(value = "/exportExcel")
	@ApiOperation(value = "导出Excel", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void exportExcel(@Valid ProductionManageOutOrderListDTO orderListDTO , HttpServletResponse response) throws SuperCodeException {
		service.exportOutOrderExcel(orderListDTO, response);
	}
}

