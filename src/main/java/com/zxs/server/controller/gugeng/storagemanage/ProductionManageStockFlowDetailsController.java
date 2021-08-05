package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductionManageStockFlowDetailsListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockFlowRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageStockFlowDetailsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockFlowResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 库存流水详情表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStockFlowDetails")
@Api(value = "库存流水管理", tags = "库存流水管理")
public class ProductionManageStockFlowDetailsController {
    @Autowired
    private ProductionManageStockFlowDetailsService service;

    @NeedAdvancedSearch
    @GetMapping("/list")
    @ApiOperation(value = "库存流水列表", notes = "库存流水列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<ProductionManageStockFlowDetails>>> list(ProductionManageStockFlowDetailsListDTO stockFlowDetailsListDTO) throws SuperCodeException {
        PageResults<List<ProductionManageStockFlowDetails>> list = service.list(stockFlowDetailsListDTO);
        // 初始化数值
        List<ProductionManageStockFlowDetails> result = list.getList();
        if (CollectionUtils.isNotEmpty(result)) {
            result.forEach(detail -> {
                detail.setOutInNum(Optional.ofNullable(detail.getOutInNum()).orElse(0));
                detail.setOutInBoxNum(Optional.ofNullable(detail.getOutInBoxNum()).orElse(0));
                detail.setOutInWeight(Optional.ofNullable(detail.getOutInWeight()).orElse(BigDecimal.ZERO));
            });
        }
        return RestResult.success(list);
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list-all",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "全部库存流水信息列表", notes = "全部库存流水信息列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchStockFlowResponseVO>>> listAll(SearchStockFlowRequestDTO requestDTO) {
        return RestResult.success(service.listAll(requestDTO));
    }

    @NeedAdvancedSearch
    @PostMapping("/list-export")
    @ApiOperation(value = "库存流水信息导出", notes = "库存流水信息导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchStockFlowRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }
}