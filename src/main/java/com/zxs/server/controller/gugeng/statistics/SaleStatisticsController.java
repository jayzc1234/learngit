package com.zxs.server.controller.gugeng.statistics;

import com.jgw.supercodeplatform.common.AbstractPageService;
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
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PersonOrderConditionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/sale")
@Api(tags = "销售数据统计")
@Slf4j
public class SaleStatisticsController {

    @Autowired
    private SaleStatisticsService statisticsService;

    @GetMapping("/order/amountAndNum")
    @ApiOperation(value = "销售额，订单数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<SaleAndOrderNumVO> add(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return statisticsService.amountAndNum(dateIntervalDTO);
    }

    @GetMapping("/order/person/salerank")
    @ApiOperation(value = "个人销售额排行", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<SaleOrderPersonRankingVO>>> salerank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<SaleOrderPersonRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<SaleOrderPersonRankingVO>> pageResults= statisticsService.salePersonRanking(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }
    @GetMapping("/person/order/condition")
    @ApiOperation(value = "个人订单情况", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<PersonOrderConditionVO>>> personOrderCondition(PersonOrderConditionDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<PersonOrderConditionVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<PersonOrderConditionVO>> pageResults= statisticsService.personOrderCondition(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }
    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportPersonOrderconditionExcel")
    @ApiOperation(value = "导出个人订单情况excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPersonOrderconditionExcel(PersonOrderConditionDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportPersonOrderconditionExcel(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("个人订单情况失败",e);
            CommonUtil.throwSupercodeException(500,"个人订单情况失败");
        }
    }

    @GetMapping("/person/order/condition/best")
    @ApiOperation(value = "个人订单情况最好的销售人员信息", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<Map<String,String>> personOrderConditionBestSaleUser(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<Map<String,String>> restResult=new RestResult<>();
        Map<String,String> saleuserMap=statisticsService.personOrderConditionBestSaleUser(dateIntervalListDTO);
        restResult.setResults(saleuserMap);
        restResult.setState(200);
        return restResult;
    }

    @GetMapping("/order/potential/client")
    @ApiOperation(value = "潜在客户排行", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<PotentialClientRankingVO>>> potentialclientRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<PotentialClientRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<PotentialClientRankingVO>> pageResults= statisticsService.potentialclientRank(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }

    @GetMapping("/order/status/rank")
    @ApiOperation(value = "订单状态汇总排行", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<OrderStatusRankingVO>>> orderStatusRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<OrderStatusRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<OrderStatusRankingVO>> pageResults= statisticsService.orderStatusRankPage(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }


    @GetMapping("/order/client/order")
    @ApiOperation(value = "订单客户", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<SaleClientRankingVO>>> clientOrder(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<SaleClientRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<SaleClientRankingVO>> pageResults= statisticsService.clientOrderRank(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }

    @GetMapping("/order/product")
    @ApiOperation(value = "产品销售排行", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<AbstractPageService.PageResults<List<SaleOrderProductRankingVO>>> orderProductRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        RestResult<AbstractPageService.PageResults<List<SaleOrderProductRankingVO>>> restResult=new RestResult<>();
        AbstractPageService.PageResults<List<SaleOrderProductRankingVO>> pageResults= statisticsService.orderProductRank(dateIntervalListDTO);
        restResult.setResults(pageResults);
        restResult.setState(200);
        return restResult;
    }

    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportPersonSalerank")
    @ApiOperation(value = "导出个人销售排行excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportExcel(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportPersonSalerank(dateIntervalListDTO, response);
        }catch (Exception e){
        	log.error("出个人排行失败",e);
            CommonUtil.throwSupercodeException(500,"导出个人排行失败");
        }
    }


    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportClientOrder")
    @ApiOperation(value = "导出订单客户xcel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportClientOrder(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportClientOrderRank(dateIntervalListDTO, response);
        }catch (Exception e){
        	log.error("导出订单客户xcel失败",e.getLocalizedMessage());
            CommonUtil.throwSupercodeException(500,"导出订单客户失败");
        }
    }

    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportProductRank")
    @ApiOperation(value = "导出产品销售排行excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportProductRank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportProductRank(dateIntervalListDTO, response);
        }catch (Exception e){
        	log.error("导出产品销售排行失败",e.getLocalizedMessage());
            CommonUtil.throwSupercodeException(500,"导出产品销售排行失败");
        }
    }


    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportPotentialclientRankExcel")
    @ApiOperation(value = "导出潜在客户excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportPotentialclientRankExcel(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportPotentialclientRankExcel(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("导出潜在客户失败",e);
            CommonUtil.throwSupercodeException(500,"导出潜在客户失败");
        }
    }

    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/exportOrderStatusRank")
    @ApiOperation(value = "导出订单状态汇总excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportOrderStatusRank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        try {
            statisticsService.exportOrderStatusRank(dateIntervalListDTO, response);
        }catch (Exception e){
            log.error("导出潜在客户失败",e);
            CommonUtil.throwSupercodeException(500,"导出潜在客户失败");
        }
    }
}
