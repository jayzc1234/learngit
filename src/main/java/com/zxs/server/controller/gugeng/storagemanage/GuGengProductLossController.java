package com.zxs.server.controller.gugeng.storagemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddPlReqDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlReqDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.UpdatePlReqDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.ProductLossReasonEnum;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.GuGengProductLossService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPlDetailRespVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPlRespVO;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.ProductLossErrorMsgConstants.*;

/**
 * 产品报损控制器
 *
 * @author shixiongfei
 * @date 2020-02-25
 * @since
 */
@RestController
@RequestMapping(VALID_PATH+"/product-loss")
@Api(value = "产品报损controller", tags = "产品报损管理")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuGengProductLossController {

    GuGengProductLossService productLossService;

    @PostMapping
    @ApiOperation("添加产品报损")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Void> add(@RequestBody AddPlReqDTO request) {
        CustomAssert.validSaveDataIsLegal(request.getReportLossBoxNum(), request.getReportLossNum(),
                request.getReportLossWeight(), INVENTORY_PARAM_WRITE_AT_LEAST_ONE);
        // 获取有效的报损原因
        request.setReportLossReason(ProductLossReasonEnum.getValidKey(request.getReportLossReason()));
        productLossService.add(request);
        return RestResult.success();
    }

    @RequestMapping(value = "/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation("产品报损列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchPlRespVO>>> list(SearchPlReqDTO request) {
        return RestResult.success(productLossService.list(request));
    }

    @GetMapping("/{id}")
    @ApiOperation("产品报损详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchPlDetailRespVO> getDetail(@PathVariable("id") Long id) {
        CustomAssert.numberIsNotLegal2Error(id, RESOURCE_KEY_FORMAT_ERROR);
        return RestResult.success(productLossService.getDetail(id));
    }

    @PutMapping("/{id}")
    @ApiOperation("报损确认")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Void> lossConfirm(@PathVariable("id") Long id, @RequestBody UpdatePlReqDTO request) {
        CustomAssert.numberIsNotLegal2Error(id, RESOURCE_KEY_FORMAT_ERROR);
        CustomAssert.validSaveDataIsLegal(request.getReportLossBoxNum(), request.getReportLossNum(),
                request.getReportLossWeight(), INVENTORY_PARAM_WRITE_AT_LEAST_ONE);
        // 获取有效的报损原因
        request.setReportLossReason(ProductLossReasonEnum.getValidKey(request.getReportLossReason()));
        productLossService.lossConfirm(id, request);
        return RestResult.success();
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "产品报损导出", notes = "产品报损导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Void> export(SearchPlReqDTO request, HttpServletResponse response) {
        productLossService.export(request, response);
        return RestResult.success();
    }


}