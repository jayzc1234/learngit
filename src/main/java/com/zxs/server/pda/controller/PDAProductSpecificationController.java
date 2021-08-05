package com.zxs.server.pda.controller;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.ProductSpecificationDO;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.IProductSpecificationService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.PDA_VALID_PATH;

/**
 * <p>
 * 产品规格 前端控制器
 * </p>
 *
 * @author 
 * @since 2019-12-13
 */
@RestController
@RequestMapping(PDA_VALID_PATH +"/product/specification")
@Api(tags = "规格管理")
public class PDAProductSpecificationController {
    @Autowired
    private IProductSpecificationService iProductSpecificationService;

    @GetMapping("/product-id")
    @ApiOperation("根据产品获取规格列表")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult<AbstractPageService.PageResults<List<ProductSpecificationDO>>> getSpecificationpageList(String productId){
        return new RichResult<>(iProductSpecificationService.getProductProductSpecificationPage(productId));
    }

}

