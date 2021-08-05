package com.zxs.server.controller.gugeng.salemanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageOrderProductReceivedService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOrderReceivedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zc
 * @since 2019-09-03
 */
@RestController
@RequestMapping(VALID_PATH+"/orderProductReceived")
@Api(value = "订单实收信息接口", tags = "订单实收信息接口")
public class ProductionManageOrderProductReceivedController {

    @Autowired
    private ProductionManageOrderProductReceivedService service;

    @GetMapping("/get")
    @ApiOperation(value = "根据订单id获取实收信息", notes = "")
    public RestResult<ProductionManageOrderReceivedVO> getByOrderId(@RequestParam("orderId") Long orderId) throws SuperCodeException {
        ProductionManageOrderReceivedVO orderReceivedVO=service.getByOrderId(orderId);
        return RestResult.success(orderReceivedVO);
    }





}

