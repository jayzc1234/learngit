package com.zxs.server.controller.gugeng.statistics;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSTDRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsSaleTargetDataService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDHResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSTDResponseVO;
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
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStatisticsSaleTargetData")
@Api(value = "销售目标数据controller", tags = "销售目标数据管理")
public class ProductionManageStatisticsSaleTargetDataController {

    @Autowired
    private ProductionManageStatisticsSaleTargetDataService service;

    @GetMapping("/list")
    @ApiOperation(value = "销售目标数据列表", notes = "销售目标数据列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSTDResponseVO>>> list(SearchSTDRequestDTO requestDTO) throws SuperCodeException {
        // 校验选择销售人员的同时是否选择了部门
        boolean isTrue = StringUtils.isNotBlank(requestDTO.getSalesPersonnelId()) && StringUtils.isBlank(requestDTO.getDepartmentId());
        if (isTrue) {
            throw new SuperCodeExtException("选择销售人员之前,必须先选择部门");
        }
        return RestResult.success(service.list(requestDTO));
    }

    @GetMapping("/histogram")
    @ApiOperation(value = "销售目标数据柱状图", notes = "销售目标数据柱状图")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchSTDHResponseVO> listHistogram(SearchSTDRequestDTO requestDTO) {
        boolean isTrue = StringUtils.isNotBlank(requestDTO.getSalesPersonnelId()) && StringUtils.isBlank(requestDTO.getDepartmentId());
        if (isTrue) {
            throw new SuperCodeExtException("选择销售人员之前,必须先选择部门");
        }
        return RestResult.success(service.listHistogram(requestDTO));
    }

    @PostMapping("/export")
    @ApiOperation(value = "销售目标数据导出", notes = "销售目标数据导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchSTDRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        service.export(requestDTO, response);
    }
}