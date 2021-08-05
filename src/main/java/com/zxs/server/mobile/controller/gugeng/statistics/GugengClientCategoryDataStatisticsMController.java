package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageOrderProductDataStatisticsDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.GuGengOrderClientCategoryDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.GuGengOrderClientCategoryListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderProductDataStatisticsListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-30
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/order/clientCategory/")
@Api(value = "客户分类数据", tags = "客户分类数据")
public class GugengClientCategoryDataStatisticsMController extends CommonUtil {

    @Autowired
    private GuGengOrderClientCategoryDataStatisticsService service;

    @GetMapping("/list")
    @ApiOperation(value = "列表", notes = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<GuGengOrderClientCategoryListVO>>> list(ProductionManageOrderProductDataStatisticsDTO statisticsDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(statisticsDTO),null);
    }

    @GetMapping("/pie")
    @ApiOperation(value = "饼状图", notes = "饼状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<Map<String, List<NameValueVO>>> pie(ProductionManageOrderProductDataStatisticsDTO statisticsDTO) throws SuperCodeException {
        return RestResult.success(service.pie(statisticsDTO));
    }
    /**
     * 信息导出
     */
    @ApiOperation(value = "导出", notes = "导出")
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageOrderProductDataStatisticsDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(dateIntervalDTO, getExportNumber(),"订单回款列表", ProductionManageOrderProductDataStatisticsListVO.class,response);
    }
}
