package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsNonProductProductionDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductProductionDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDPageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPPDResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  产品产量管理前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-29
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/productionManageStatisticsProductProductionData")
@Api(value = "产品产量数据controller", tags = "产品产量管理")
public class ProductionManageStatisticsProductProductionDataMController {

    @Autowired
    private ProductionManageStatisticsProductProductionDataService service;

    @Autowired
    private ProductionManageStatisticsNonProductProductionDataService nonService;

    @GetMapping("/histogram")
    @ApiOperation(value = "获取产品产量数据柱状图", notes = "获取产品产量数据柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchPPDResponseVO> histogram(SearchPPDRequestDTO requestDTO) {
        boolean isTrue = StringUtils.isNotBlank(requestDTO.getProductLevelCode()) && StringUtils.isBlank(requestDTO.getProductId());
        if (isTrue) { return RestResult.error("请在选择产品等级前，先选择产品"); }

        // 校验是否存在产品等级
        SearchPPDResponseVO responseVO;
        if (StringUtils.isNotBlank(requestDTO.getProductLevelCode())) {
            responseVO = service.histogram(requestDTO);
        } else {
            responseVO = nonService.histogram(requestDTO);
        }
        return RestResult.success(responseVO);
    }


    @GetMapping("/list")
    @ApiOperation(value = "获取产品产量数据列表", notes = "获取产品产量数据列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchPPDPageResponseVO>>> list(SearchPPDRequestDTO requestDTO) throws SuperCodeException {
        boolean isTrue = StringUtils.isNotBlank(requestDTO.getProductLevelCode()) && StringUtils.isBlank(requestDTO.getProductId());
        if (isTrue) { return RestResult.error("请在选择产品等级前，先选择产品"); }

        // 校验是否存在产品等级
        if (StringUtils.isNotBlank(requestDTO.getProductLevelCode())) {
            return RestResult.success(service.list(requestDTO));
        } else {
            return RestResult.success(nonService.list(requestDTO));
        }
    }
}
