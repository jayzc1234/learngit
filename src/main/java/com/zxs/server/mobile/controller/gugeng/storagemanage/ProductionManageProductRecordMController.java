package com.zxs.server.mobile.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageProductRecordService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductStockStatisticsResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 * 产品档案-控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-29
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/product-record")
@Api(value = "产品档案管理", tags = "产品档案管理")
public class ProductionManageProductRecordMController extends CommonUtil {

    @Autowired
    private ProductionManageProductRecordService service;


    @GetMapping("/list/statistics")
    @ApiOperation(value = "获取产品档案统计信息", notes = "获取产品档案统计信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchProductStockStatisticsResponseVO> statistics(SearchProductRecordStatisticsRequestDTO requestDTO) throws SuperCodeException {
        // 校验时间是否正确
        CustomAssert.isFalse(LocalDate.parse(requestDTO.getStartQueryDate())
                .isAfter(LocalDate.parse(requestDTO.getEndQueryDate())), "开始日期不可大于结束日期");
        return RestResult.success(service.statistics(requestDTO));
    }
}