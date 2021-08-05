package com.zxs.server.controller.gugeng.hydra.statistic;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.hydra.statistic.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.hydra.statistic.GuGengVistorStatisticServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.statistic.VisitorClientDataCurveLineVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 *  前端控制器
 * </p>
 * @author shixiongfei
 * @since 2019-12-09
 */
@RestController
@RequestMapping(VALID_PATH+"/visitor/statistic")
@Api(value = "运营统计-来访人员数据", tags = "运营统计-来访人员数据")
public class GuGengVistorStatisticController {

    @Autowired
    private GuGengVistorStatisticServiceImpl service;

    @Autowired
    private CommonUtil commonUtil;

    @GetMapping("/client")
    @ApiOperation(value = "来访客户", notes = "来访客户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<VisitorClientDataCurveLineVO> visitorClientStatistic(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException, ParseException {

        return RestResult.success(service.visitorClientStatistic(dateIntervalDTO));
    }

    @GetMapping("/project")
    @ApiOperation(value = "来访项目", notes = "来访项目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<VisitorClientDataCurveLineVO> visitorProjectStatistic(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException, ParseException {

        return RestResult.success(service.visitorProjectStatistic(dateIntervalDTO));
    }

    @GetMapping("/cash")
    @ApiOperation(value = "来访金额", notes = "来访金额")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<VisitorClientDataCurveLineVO> visitorCashStatistic(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException, ParseException {

        return RestResult.success(service.visitorCashStatistic(dateIntervalDTO));
    }


    @GetMapping("/dataSync")
    @ApiOperation(value = "数据同步", notes = "数据同步")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<VisitorClientDataCurveLineVO> dataSync() throws SuperCodeException, ParseException {
        service.dataSync();
        return RestResult.success(null);
    }
    /**
     * 信息导出
     */
    @ApiOperation(value = "导出", notes = "导出")
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(DateIntervalListDTO dateIntervalDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(dateIntervalDTO, commonUtil.getExportNumber(),"订单回款列表", VisitorClientDataCurveLineVO.class,response);
    }

}

