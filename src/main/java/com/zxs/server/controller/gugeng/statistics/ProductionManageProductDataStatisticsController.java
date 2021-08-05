package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchDateRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageProductDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.GreenhouseStatisticsResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductDataResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 生产数据统计表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */
@RestController
@RequestMapping(VALID_PATH+"/product-data-statistics")
@Api(value = "生产数据统计管理", tags = "生产数据统计管理")
public class ProductionManageProductDataStatisticsController {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageProductDataStatisticsService service;

    @GetMapping("/list")
    @ApiOperation(value = "获取生产数据统计", notes = "获取生产数据统计")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<ProductDataResponseVO> list(DateIntervalDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getStartQueryDate()) || StringUtils.isBlank(requestDTO.getEndQueryDate())) {
            throw new SuperCodeException("开始时间或结束时间不可为空");
        }
        return RestResult.success(service.list(requestDTO));
    }

    @NeedAdvancedSearch
    @ApiOperation(value = "获取区域排名列表", notes = "获取区域排名列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @GetMapping("/greenhouse-ranking/list")
    public RestResult<PageResults<List<GreenhouseStatisticsResponseVO>>> listGreenhouseRanking(SearchDateRequestDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getStartQueryDate()) || StringUtils.isBlank(requestDTO.getEndQueryDate())) {
            throw new SuperCodeException("开始时间或结束时间不可为空");
        }
        return RestResult.success(service.listGreenhouseRanking(requestDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/greenhouse-ranking/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void greenhouseRankExport(SearchDateRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getStartQueryDate()) || StringUtils.isBlank(requestDTO.getEndQueryDate())) {
            throw new SuperCodeException("开始时间或结束时间不可为空");
        }
        service.greenhouseRankExport(requestDTO, response);
    }
}