package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchOutboundRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchOutboundResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 前端控制器
 * </p>
 * 包装出库统计
 *
 * @author shixiongfei
 * @since 2019-07-24
 */
@RestController
@RequestMapping(VALID_PATH+"/outbound")
@Api(value = "出库信息管理", tags = "出库信息管理")
public class ProductionManageOutboundController extends CommonUtil {

    @Autowired
    private ProductionManageOutboundService service;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @NeedAdvancedSearch
    @ApiOperation(value = "包装出库列表", notes = "包装出库列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchOutboundResponseVO>>> list(SearchOutboundRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @PutMapping("/allDone")
    @NeedAdvancedSearch
    @ApiOperation(value = "全部发货", notes = "全部发货")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<String> allDone(@RequestParam("orderId") Long orderId) throws SuperCodeException {
        service.allDone(orderId);
        return RestResult.success();
    }

    /**
     * 信息导出
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchOutboundRequestDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, getExportNumber(), "包装出库信息", response);
    }

    @GetMapping("/valid-need-second-delivery")
    @NeedAdvancedSearch
    @ApiOperation(value = "校验是否需要二次发货", notes = "校验是否需要二次发货")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Boolean> validNeedSecondDelivery(@RequestParam("orderId") Long orderId) {
        return RestResult.success(service.validNeedSecondDelivery(orderId));
    }
}