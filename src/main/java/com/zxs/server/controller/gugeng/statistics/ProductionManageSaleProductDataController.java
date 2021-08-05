package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageSaleProductDataListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageSaleProductDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-15
 */
@RestController
@RequestMapping(VALID_PATH+"/saleProductData")
@Api(value = "", tags = "")
public class ProductionManageSaleProductDataController {

    @Autowired
    private ProductionManageSaleProductDataService service;

    @GetMapping("/list")
    @ApiOperation(value = "订单产品数据列表", notes = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(ProductionManageSaleProductDataListDTO saleProductDataListDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(saleProductDataListDTO),null);
    }

}
