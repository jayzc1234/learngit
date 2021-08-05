package com.zxs.server.controller.gugeng.statistics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionPlantingYieldService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductPlantingYieldStatisticVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/planting-yield")
@Api(tags = "产量数据统计-月度")
public class ProductionPlantingYieldController {

    @Autowired
    private ProductionPlantingYieldService plantingYieldService;

    @GetMapping("/list")
    @ApiOperation(value = "获取数据", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<ProductPlantingYieldStatisticVO> list() {
        return RestResult.ok(plantingYieldService.list());
    }


}
