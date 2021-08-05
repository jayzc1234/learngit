package com.zxs.server.controller.gugeng.salemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductManageOrderProductListDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderProductService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ClientSaleOrderStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOrderProductListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/sale/order/product")
@Api(tags = "销售订单产品")
public class ProductManageOrderProductController {

	@Autowired
	private ProductManageOrderProductService service;
	
	@Autowired
	private CommonUtil commonUtil;
	/**
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page",method = {RequestMethod.GET, RequestMethod.POST})
	@ApiOperation(value = "销售订单产品列表(客户详情订单)", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductionManageOrderProductListVO>>> page(ProductManageOrderProductListDTO pOrderProductListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductionManageOrderProductListVO>>> restResult = new RestResult<>();
		PageResults<List<ProductionManageOrderProductListVO>> pageResults = service.page(pOrderProductListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
		return restResult;
	}
	
	
	
	/**
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	@ApiOperation(value = "客户订单统计", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<ClientSaleOrderStatisticsVO> statistics(Long clientId, String productId )
			throws Exception {
		RestResult<ClientSaleOrderStatisticsVO> restResult = new RestResult<>();
		ClientSaleOrderStatisticsVO cStatisticsVO = service.statistics(clientId,productId);
		restResult.setState(200);
		restResult.setResults(cStatisticsVO);
		return restResult;
	}
	
	/**
	 * 导出excel
	 */
	@EnableRateLimiter
	@NeedAdvancedSearch
	@PostMapping("/exportExcel")
	@ApiOperation(value = "导出订单管理excel")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void exportExcel(ProductManageOrderProductListDTO productListDTO, HttpServletResponse response) throws Exception {
		service.exportExcel(productListDTO, commonUtil.getExportNumber(), "客户历史订单", response);
	}
}
