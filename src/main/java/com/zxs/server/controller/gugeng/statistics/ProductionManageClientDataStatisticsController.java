package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageClientDataStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageClientOrderDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ClientDataCurveLineVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageClientOrderDataStatisticsListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleClientNumStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *客户数据
 * @author zc
 * @since 2019-10-21
 * 1.6版本
 *
 */
@RestController
@RequestMapping(VALID_PATH+"/client/data")
@Api(value = "客户数据", tags = "客户数据")
public class ProductionManageClientDataStatisticsController {

    @Autowired
    private ProductionManageClientDataStatisticsService service;

    @Autowired
    private ProductionManageClientOrderDataStatisticsService clientOrderDataStatisticsService;

    @GetMapping("/order/client/num")
    @ApiOperation(value = "订单客户数曲线", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<ClientDataCurveLineVO> orderClientNum(DateIntervalDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.orderClientNum(dateIntervalDTO));
    }

    @GetMapping("/order/potential/client/num")
    @ApiOperation(value = "潜在客户数曲线", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<ClientDataCurveLineVO> potentialClientNum(DateIntervalDTO  dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.potentialClientNum(dateIntervalDTO));
    }

    @GetMapping("/potentialClientList")
    @ApiOperation(value = "潜在客户转化率列表", notes = "潜在客户转化率列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageSaleClientNumStatisticsVO>>> potentialClientList(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.potentialClientList(dateIntervalDTO),null);
    }

    @GetMapping("/orderClientList")
    @ApiOperation(value = "订单客户列表", notes = "订单客户列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public  RestResult<AbstractPageService.PageResults<List<ProductionManageClientOrderDataStatisticsListVO>>> orderClientList(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.orderClientList(dateIntervalDTO),null);
    }

    /**
     * 信息导出
     */
    @ApiOperation(value = "导出潜在客户", notes = "导出潜在客户")
    @PostMapping("/exportPotentialClient")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPotentialClient(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportPotentialClient(dateIntervalDTO, response);
    }

    /**
     * 信息导出
     */
    @ApiOperation(value = "导出订单客户", notes = "导出订单客户")
    @PostMapping("/exportOrderClient")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportOrderClient(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportOrderClient(dateIntervalDTO, response);
    }

    @GetMapping("/timedTaskDataSync")
    @ApiOperation(value = "客户数据同步", notes = "客户数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Void> list() throws SuperCodeException {
        clientOrderDataStatisticsService.timedTaskDataSync();
        return RestResult.success();
    }
}
