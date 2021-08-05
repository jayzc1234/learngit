package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSTDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsSaleTargetDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDHResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/productionManageStatisticsSaleTargetData")
@Api(value = "销售目标数据controller", tags = "销售目标数据管理")
public class ProductionManageStatisticsSaleTargetDataMController {

    @Autowired
    private ProductionManageStatisticsSaleTargetDataService service;

    @GetMapping("/list")
    @ApiOperation(value = "销售目标数据列表", notes = "销售目标数据列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSTDResponseVO>>> list(SearchSTDRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @GetMapping("/histogram")
    @ApiOperation(value = "销售目标数据柱状图", notes = "销售目标数据柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSTDHResponseVO> listHistogram(SearchSTDRequestDTO requestDTO) {
        return RestResult.success(service.listHistogram(requestDTO));
    }
}