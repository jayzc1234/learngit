package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddPackingOutboundRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.UpdatePackingOutboundRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundPackageMessageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackageMessageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackingMsgResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.PackingOutboundErrorMsgConstants.INVENTORY_PARAM_WRITE_AT_LEAST_ONE;


/**
 * <p>
 * 出库包装信息表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@RestController
@RequestMapping(VALID_PATH+"/outbound-package-message")
@Api(value = "包装信息管理", tags = "包装信息管理")
public class ProductionManageOutboundPackageMessageController {

    @Autowired
    private ProductionManageOutboundPackageMessageService service;


    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取包装列表", notes = "获取包装列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchPackageMessageResponseVO>>> list(@RequestParam(value="outboundId",required = false) Long outboundId) throws SuperCodeException {
        if (Objects.isNull(outboundId) || outboundId < 1) {
            throw new SuperCodeException("出库信息主键id不可为空");
        }
        return RestResult.success(service.list(outboundId));
    }

    @GetMapping("/list-by-order-id")
    @ApiOperation(value = "通过订单id获取包装列表", notes = "通过订单id获取包装列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "orderId", paramType = "query", defaultValue = "1", value = "订单id", required = true)
    })
    public RestResult<PageResults<List<SearchPackageMessageResponseVO>>> listByOrderId(@RequestParam("orderId") Long orderId) throws SuperCodeException {
        if (Objects.isNull(orderId) || orderId < 1) {
            throw new SuperCodeException("订单主键id不可为空");
        }

        return RestResult.success(service.listByOrderId(orderId));
    }

    /**
     * 包装出库
     * @since v1.1.1
     * @date 2019-08-28
     * @param requestDTO
     * @return
     */
    @PostMapping("/code-less/save")
    @ApiOperation(value = "包装保存", notes = "包装保存")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult codeLessSave(@RequestBody @Validated AddPackingOutboundRequestDTO requestDTO) {
        requestDTO.getPackageMessageList().forEach(packageMessage -> CustomAssert.validSaveDataIsLegal(packageMessage.getPackingBoxNum(),
                packageMessage.getPackingNum(), packageMessage.getPackingWeight(), packageMessage.getPackingServings(), INVENTORY_PARAM_WRITE_AT_LEAST_ONE));
        service.codeLessAdd(requestDTO);
        return RestResult.success();
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑包装出库", notes = "编辑包装出库")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateOutbound(@RequestBody @Validated UpdatePackingOutboundRequestDTO requestDTO) {
        service.updateOutbound(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/list-with-not-outbound-msg")
    @ApiOperation(value = "获取未出库发货的包装信息列表", notes = "通过订单id获取未出库发货的包装信息列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<SearchPackingMsgResponseVO>> listByOrderIdWithNotOutboundMsg(@RequestParam("orderId") Long orderId) {
        return RestResult.success(service.listNotOutboundMsg(orderId));
    }
}