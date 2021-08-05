package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.OrderConditionLineChartDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageOrderDataByTypeService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderConditionLineVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderDataByTypeListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-15
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/orderCondition")
@Api(value = "订单情况数据", tags = "订单情况数据")
public class ProductionManageOrderConditionMController extends CommonUtil {

    @Autowired
    private ProductionManageOrderDataByTypeService service;

    @GetMapping("/curve")
    @ApiOperation(value = "订单情况曲线图", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<OrderConditionLineVO> payback(OrderConditionLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.orderConditionCurve(dateIntervalDTO));
    }

    @GetMapping("/list")
    @ApiOperation(value = "列表", notes = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageOrderDataByTypeListVO>>> list(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(dateIntervalDTO),null);
    }


}
