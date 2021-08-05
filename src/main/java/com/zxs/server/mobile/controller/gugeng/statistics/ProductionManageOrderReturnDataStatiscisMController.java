package com.zxs.server.mobile.controller.gugeng.statistics;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageOrderReturnDataStatiscisService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderReturnDataCurveLineVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-22
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/returnDataStatiscis")
@Api(value = "退货数据统计", tags = "退货数据统计")
public class ProductionManageOrderReturnDataStatiscisMController extends CommonUtil {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageOrderReturnDataStatiscisService service;

    @GetMapping("/curve")
    @ApiOperation(value = "订单客户数曲线", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)})
    public RestResult<OrderReturnDataCurveLineVO> curve(DateIntervalDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        return RestResult.success(service.curve(dateIntervalDTO));
    }

}
