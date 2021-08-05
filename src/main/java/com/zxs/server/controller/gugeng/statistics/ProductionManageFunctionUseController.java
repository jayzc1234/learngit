package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageFunctionUseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageFunctionUseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.FunctionUseResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-28
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageFunctionUse")
@Api(tags = "用户使用统计")
public class ProductionManageFunctionUseController {


    @Autowired
    private ProductionManageFunctionUseService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增用户使用记录", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody ProductionManageFunctionUseDTO dto) throws SuperCodeException {
        service.add(dto);
        return RestResult.success();
    }

    @ApiOperation(value = "", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(Object obj) throws SuperCodeException {
        // TODO obj类型自行修改
        service.list(obj);
        return null;
    }

    @GetMapping("/pageFunctionUse")
    @ApiOperation(value = "分页获取功能使用频次", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<FunctionUseResponseVO>>> pageFunctionUse(SaleUserLineChartDTO requestDTO)  throws Exception  {
        return RestResult.success(service.page(requestDTO));
    }

    @GetMapping("/selectLineChartVO")
    @ApiOperation(value = "获取功能使用频次图表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<LineChartVO> listYieldComparisonDataHistogram(SaleUserLineChartDTO requestDTO)  throws Exception  {
        return new RestResult(200, "success", service.selectLineChartVO(requestDTO));
    }

}
