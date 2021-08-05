package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchGreenhouseStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsGreenhouseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.StatisticsGreenhouseLineChartResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.StatisticsGreenhouseResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  区域数据统计前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-20
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStatisticsGreenhouse")
@Api(value = "区域数据统计controller", tags = "区域数据统计管理")
public class ProductionManageStatisticsGreenhouseController {

    @Autowired
    private ProductionManageStatisticsGreenhouseService service;

    @GetMapping("/list")
    @ApiOperation(value = "获取区域数据统计", notes = "获取区域数据统计")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<StatisticsGreenhouseLineChartResponseVO> list(SearchGreenhouseStatisticsRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @GetMapping("/greenhouse-list")
    @ApiOperation(value = "获取区域数据统计列表", notes = "获取区域数据统计列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<StatisticsGreenhouseResponseVO>>> listGreenhouse(SearchGreenhouseStatisticsRequestDTO requestDTO) {
        return RestResult.success(service.listGreenhouse(requestDTO));
    }

    @PostMapping("/export")
    @ApiOperation(value = "区域数据统计列表导出", notes = "区域数据统计列表导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchGreenhouseStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }

    @GetMapping("/getLastestGreenhouse")
    @ApiOperation(value = "获取最新采收区域", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<NameValueVO<String,String>> getLastestGreenhouse() throws SuperCodeException {
        return RestResult.success(service.getLastestGreenhouse());
    }

    @GetMapping("/getDefaultProduct")
    @ApiOperation(value = "获取默认产品", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<NameValueVO<String,String>> getDefaultProduct(HttpServletRequest request) throws SuperCodeException {
        return RestResult.success(service.getDefaultProduct(request));
    }
}