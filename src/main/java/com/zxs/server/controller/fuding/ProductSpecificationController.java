package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.ProductSpecificationBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.ProductSpecificationDO;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.IProductSpecificationService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationAddVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationUpdateVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationVO;
import net.app315.nail.common.utils.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 产品规格 前端控制器
 * </p>
 *
 * @author 
 * @since 2019-12-13
 */
@RestController
@RequestMapping(VALID_PATH +"/product/specification")
@Api(tags = "规格管理")
public class ProductSpecificationController {
    @Autowired
    private IProductSpecificationService iProductSpecificationService;

    @PostMapping
    @ApiOperation("添加产品规格")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void addProductSpecification(@RequestBody ProductSpecificationAddVO model ){
           ProductSpecificationBO params = new ProductSpecificationBO();
           BeanUtils.copyProperties(model,params);
           iProductSpecificationService.addProductSpecification(params);
    }

    @PutMapping
    @ApiOperation("修改产品规格")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void updateProductSpecification(@RequestBody ProductSpecificationUpdateVO model){
        ProductSpecificationBO params = new ProductSpecificationBO();
        BeanUtils.copyProperties(model,params);
        iProductSpecificationService.updateProductSpecification(params);
    }

    @GetMapping
    @ApiOperation("获取产品规格详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header") ,
            @ApiImplicitParam(name = "specificationId", value = "产品规格id", required = true)
    })
    public ProductSpecificationVO getProductSpecification(String specificationId){
         ProductSpecificationBO searchResult =  iProductSpecificationService.getProductSpecification(specificationId);
         if(searchResult == null){
            return null;
         }
         ProductSpecificationVO result = new ProductSpecificationVO();
         BeanUtils.copyProperties(searchResult,result);
         return result;
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("产品规格记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<ProductSpecificationVO>> getProductSpecificationpageList(ProductSpecificationSearchModel model){
        return iProductSpecificationService.getProductSpecificationList(model);
    }


    @RequestMapping(value = "/list/product",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("根据产品id获取规格分页接口")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<ProductSpecificationDO>>> product(@RequestParam(required = false) String productId, DaoSearch daoSearch){
        return iProductSpecificationService.specificationByProduct(productId,daoSearch);
    }

    @GetMapping("/product-id")
    @ApiOperation("根据产品获取规格列表")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public List<ProductSpecificationVO> getSpecificationpageList(String productId){
        return CopyUtil.copyList(iProductSpecificationService.getProductProductSpecification(productId), ProductSpecificationVO::new);
    }

    @PostMapping("/status")
    @ApiOperation("禁用/启用")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void updateStatus(@RequestBody ProductSpecificationUpdateVO model){
        iProductSpecificationService.updateTeaFarmerStatus(model.getSpecificationId());
    }

    @PostMapping(value = "/export")
    @ApiOperation("导出规格")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(ProductSpecificationSearchModel model, HttpServletResponse response){
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + DateUtil.getCurrent(DateUtil.ALL_PATTERN) + ".xls");
        iProductSpecificationService.export(model,response);
    }

}

