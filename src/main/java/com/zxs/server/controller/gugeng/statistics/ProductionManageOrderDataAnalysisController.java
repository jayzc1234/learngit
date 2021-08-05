package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageWholeOrderAnalysisService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderDeliveryTypeRankingVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PersonOrderConditionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
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
@RequestMapping(VALID_PATH+"/order/data/statistics")
@Api(value = "订单数据分析", tags = "订单数据分析")
@Slf4j
public class ProductionManageOrderDataAnalysisController {

    @Autowired
    private ProductionManageWholeOrderAnalysisService service;

    @GetMapping("/wholeOrderCondition")
    @ApiOperation(value = "整体订单情况", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<PersonOrderConditionVO>>> wholeOrderCondition(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        AbstractPageService.PageResults<List<PersonOrderConditionVO>> pageResults=new AbstractPageService.PageResults<>();
        List<PersonOrderConditionVO> list=new ArrayList<>();
        list.add(service.wholeOrderCondition(dateIntervalDTO));
        pageResults.setList(list);
        pageResults.setPagination(new Page(1,1,1));
        return RestResult.success(pageResults);
    }

    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportWholeOrderCondition")
    @ApiOperation(value = "导出整体订单情况excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportWholeOrderCondition(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            service.exportWholeOrderCondition(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("个人订单情况失败",e);
            CommonUtil.throwSupercodeException(500,"个人订单情况失败");
        }
    }

    @GetMapping("/order/deliveryType/rank")
    @ApiOperation(value = "订单发货类型排行", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>>> orderdeliveryTypeRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>> pageResults= service.orderdeliveryTypeRank(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }
    @GetMapping("/order/deliveryType/proportion")
    @ApiOperation(value = "发货类型比例接口", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "type", paramType = "query", defaultValue = "1", value = "1订单数，2订单客户数，3销售额，4实收额", required = true)})
    public RestResult<LineChartVO> orderdeliveryTypeProportion(DateIntervalListDTO dateIntervalDTO, @RequestParam(required = true) Integer type) throws SuperCodeException{
      return service.orderdeliveryTypeProportion(dateIntervalDTO,type);
    }


    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportOrderdeliveryType")
    @ApiOperation(value = "导出订单发货类型excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportOrderdeliveryType(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            service.exportOrderdeliveryType(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("导出订单发货类型",e);
            CommonUtil.throwSupercodeException(500,"导出订单发货类型");
        }
    }

}