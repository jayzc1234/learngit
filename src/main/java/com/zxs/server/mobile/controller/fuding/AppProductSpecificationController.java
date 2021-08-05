package com.zxs.server.mobile.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.IProductSpecificationService;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 * 产品规格 前端控制器
 * </p>
 *
 * @author 
 * @since 2019-12-13
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH +"/product/specification")
@Api(tags = "规格管理")
public class AppProductSpecificationController {
    @Autowired
    private IProductSpecificationService iProductSpecificationService;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("产品规格记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<ProductSpecificationVO>> getProductSpecificationpageList(ProductSpecificationSearchModel model){
        return iProductSpecificationService.getProductSpecificationList(model);
    }

}

