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
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.config.excel.RateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.OrderImportBaseInfoCommonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageOrderProductReceivedService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageOrderProductReturnService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundPackageMessageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageOrderDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManageOrderListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductManagePdaOrderDetailVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

@RestController
@RequestMapping(VALID_MOBILE_PATH+"/sale/order")
@Api(tags = "销售订单管理")
public class ProductManageOrderMController {

	@Autowired
	private ProductManageOrderService service;

	@Autowired
	private ProductionManageOrderProductReturnService productReturnService;

	@Autowired
	private ProductionManageOrderProductReceivedService productReceivedService;

	@Autowired
	private ProductionManageOutboundPackageMessageService outboundPackageMessageService;

	@Autowired
	private OrderImportBaseInfoCommonService orderImportCommonService;

	@Autowired
	private RateLimiter rateLimiter;

	@Autowired
	private CommonUtil commonUtil;
	
	@PostMapping("/add")
	@ApiOperation(value = "订单新增", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> add(@Valid @RequestBody ProductManageOrderDTO pOrderDTO) throws SuperCodeException, ParseException{
		service.add(pOrderDTO);
		return RestResult.success();
	}
	
	@PostMapping("/update")
	@ApiOperation(value = "订单编辑", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> update(@Valid @RequestBody ProductManageOrderDTO pOrderDTO) throws SuperCodeException, ParseException{
		service.update(pOrderDTO);
		return RestResult.success();
	}

	@PostMapping("/returnGoods")
	@ApiOperation(value = "退货", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> returnGoods(@Valid @RequestBody ProductionManageOrderProductReturnDTO productReturnDTO) throws SuperCodeException, ParseException{
		productReturnService.returnGoods(productReturnDTO);
		return RestResult.success();
	}

	@PostMapping("/reject")
	@ApiOperation(value = "拒收接口", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> reject(@Valid @RequestBody ProductionManageOrderProductReceivedDTO productReceivedDTO) throws SuperCodeException, ParseException{
		productReceivedService.reject(productReceivedDTO);
		return RestResult.success();
	}

	@PostMapping("/confirm")
	@ApiOperation(value = "订单确认", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<String> confirm(@Valid @RequestBody ProductionManageOrderProductReceivedDTO productReceivedDTO ) throws SuperCodeException, ParseException{
		service.confirm(productReceivedDTO);
		return RestResult.success();
	}

	@PostMapping("/done")
	@ApiOperation(value = "订单完成", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
			@ApiImplicitParam(name = "orderIds", paramType = "query", defaultValue = "1,2", value = "t订单主键id集合", required = true)})
	public RestResult<String> done(@RequestParam("orderIds") List<Long> orderIds ) throws SuperCodeException, ParseException{
		if (CollectionUtils.isEmpty(orderIds)){
			CommonUtil.throwSuperCodeExtException(500,"订单id不可为空");
		}
		service.done(orderIds);
		return RestResult.success();
	}

	@PostMapping("/verify")
	@ApiOperation(value = "订单审核", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
			})
	public RestResult<String> verify(@Valid @RequestBody ProductManageOrderVerifyDTO productManageOrderVerifyDTO) throws SuperCodeException, ParseException{
		service.verify(productManageOrderVerifyDTO.getId(),productManageOrderVerifyDTO.getVerifyStatus(),productManageOrderVerifyDTO.getVerifyNotPassedReason());
		return RestResult.success();
	}

	@GetMapping("/detail")
	@ApiOperation(value = "订单详情", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<ProductManageOrderDetailVO> detail(@RequestParam Long id ) throws SuperCodeException, ParseException{
		ProductManageOrderDetailVO pOrderDetailVO=service.detail(id);
		return RestResult.successDefault(pOrderDetailVO);
	}


    @DeleteMapping("/deleteOrder")
    @ApiOperation(value = "订单删除", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
    })
    public RestResult<String> deleteOrder(@RequestParam("orderId") Long orderId) throws SuperCodeException, ParseException{
        service.deleteOrder(orderId);
	    return RestResult.success();
    }

	@GetMapping("/pda/detail")
	@ApiOperation(value = "pda单独订单详情", notes = "")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
	public RestResult<ProductManagePdaOrderDetailVO> pdadetail(@RequestParam Long id ) throws SuperCodeException, ParseException{
		ProductManagePdaOrderDetailVO pOrderDetailVO=service.pdadetail(id);
		return RestResult.successDefault(pOrderDetailVO);
	}
	
	/**
	 * 订单列表
	 * List<List<ProductionManageOutboundPackageMessage>> orderDetailPackageMessage
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/orderDetailPackageMessages", method = RequestMethod.GET)
	@ApiOperation(value = "订单发货包装信息", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<List<Map<String,List<ProductionManageOutboundPackageMessage>>>> orderDetailPackageMessage(Long orderId)
			throws Exception {
		RestResult<List<Map<String,List<ProductionManageOutboundPackageMessage>>>> restResult = new RestResult<>();
		List<Map<String,List<ProductionManageOutboundPackageMessage>>> lists = outboundPackageMessageService.orderDetailPackageMessage(orderId);
		restResult.setState(200);
		restResult.setResults(lists);
		return restResult;
	}

	/**
	 * 订单列表
	 *
	 * @param
	 * @return
	 * @throws Exception
	 */
	@NeedAdvancedSearch
	@RequestMapping(value = "/page", method = RequestMethod.GET)
	@ApiOperation(value = "订单列表", notes = "")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public RestResult<PageResults<List<ProductManageOrderListVO>>> page(ProductManageOrderListDTO orderListDTO)
			throws Exception {
		RestResult<PageResults<List<ProductManageOrderListVO>>> restResult = new RestResult<>();
		PageResults<List<ProductManageOrderListVO>> pageResults = service.page(orderListDTO);
		restResult.setState(200);
		restResult.setResults(pageResults);
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
	public void exportExcel(ProductManageOrderListDTO orderListDTO, HttpServletResponse response) throws Exception {
		service.exportOrderExcel(orderListDTO, response);
	}


	/**
	 * 导入excel
	 */
	@EnableRateLimiter
	@PostMapping("/orderImportTest")
	@ApiOperation(value = "订单导入-后端专用测试接口")
	@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	public void orderImport(@RequestParam("file") MultipartFile multipartFile) throws Exception {
		service.orderImportTest(multipartFile.getInputStream());
	}

	/**
	 * 订单导入
	 */
	@PostMapping("/orderImport")
	@ApiOperation(value = "订单导入-前端调用接口", notes = "订单导入-前端调用接口")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
	})
	public RestResult orderImport(@RequestBody ExcelImportDTO importDTO) throws SuperCodeException, IOException, ParseException {
		service.orderImportFromOss(importDTO);
		return RestResult.success();
	}

    @GetMapping("/dep-sync")
    @ApiOperation(value = "订单部门信息同步（v1.7新接口，用于部门信息同步）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult orderDepDatasync() {
        service.orderDepDatasync();
        return RestResult.success("订单部门信息同步完成");
    }
}
