package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageOrderPayBackDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductManageOrderUnPayBackListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderPayBackListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SaleAndOrderNumVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zc
 * @since 2019-10-15
 */
@RestController
@RequestMapping(VALID_PATH+"/saleProductData")
@Api(value = "订单回款数据", tags = "订单回款数据")
public class ProductionManageOrderPayBackDataController extends CommonUtil {

    @Autowired
    private ProductionManageOrderPayBackDataService service;

    @GetMapping("/order/payback/curve")
    @ApiOperation(value = "订单汇款数据曲线图", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<SaleAndOrderNumVO> payback(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.paybackLine(dateIntervalDTO));
    }

    @GetMapping("/order/payback/list")
    @ApiOperation(value = "订单回款数据", notes = "订单回款数据")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(dateIntervalDTO),null);
    }

    @GetMapping("/order/unPayback/list")
    @ApiOperation(value = "待回款订单排名", notes = "待回款订单排名")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult unPayback(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.unPaybackPageList(dateIntervalDTO),null);
    }

    /**
     * 信息导出
     */
    @ApiOperation(value = "导出订单回款列表", notes = "导出订单回款列表")
    @PostMapping("/exportPayBackList")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPayBackList(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportExcelReflect(dateIntervalDTO, getExportNumber(),"订单回款列表", ProductionManageOrderPayBackListVO.class,"pageList",response);
    }

    /**
     * 信息导出
     */
    @ApiOperation(value = "导出待回款订单列表", notes = "导出待回款订单列表")
    @PostMapping("/exportUnPaybackList")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportUnPaybackList(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportExcelReflect(dateIntervalDTO, getExportNumber(),"退货列表", ProductManageOrderUnPayBackListVO.class,"unPaybackPageList",response);
    }
}
