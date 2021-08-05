package com.zxs.server.pda.controller;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductManageDeliveryInfoDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductManageDeliveryInfoListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductManageDeliveryInfo;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductManageDeliveryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.PDA_VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-04
 */
@RestController
@RequestMapping(PDA_VALID_PATH+"/deliveryInfo")
@Api(value = "配送信息维护接口", tags = "配送信息维护接口")
public class PDAProductManageDeliveryInfoController {

    @Autowired
    private ProductManageDeliveryInfoService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid @RequestBody ProductManageDeliveryInfoDTO deliveryInfoDTO) throws SuperCodeException {
        service.add(deliveryInfoDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody ProductManageDeliveryInfoDTO deliveryInfoDTO) throws SuperCodeException {
        service.update(deliveryInfoDTO);
        return RestResult.success();
    }

    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult delete(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteById(id);
        return RestResult.success();
    }
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductManageDeliveryInfo>>> list(@Valid ProductManageDeliveryInfoListDTO deliveryInfoListDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(deliveryInfoListDTO),null);
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "下拉接口", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductManageDeliveryInfo>>> dropDown(@Valid  ProductManageDeliveryInfoListDTO deliveryInfoListDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(deliveryInfoListDTO),null);
    }

}