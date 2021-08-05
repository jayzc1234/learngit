package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsShippingDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSDLCResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSDResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  发货数据统计前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/productionManageStatisticsShippingData")
@Api(value = "发货数据统计controller", tags = "发货数据统计管理")
public class ProductionManageStatisticsShippingDataMController {

    @Autowired
    private ProductionManageStatisticsShippingDataService service;

    @GetMapping("/list")
    @ApiOperation(value = "发货数据统计列表", notes = "发货数据统计列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSDResponseVO>>> list(SearchSDRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @GetMapping("/list-line-chart")
    @ApiOperation(value = "发货数据统计折线图", notes = "发货数据统计折线图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSDLCResponseVO> listLineChart(SearchSDRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.listLineChart(requestDTO));
    }
}