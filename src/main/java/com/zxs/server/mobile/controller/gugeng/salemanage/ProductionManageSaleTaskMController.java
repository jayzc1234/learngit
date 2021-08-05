package com.zxs.server.mobile.controller.gugeng.salemanage;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskComparisonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * @author shixiongfei
 * @date 2019-11-05
 * @since
 */
@Slf4j
@RestController
@Api(value = "小程序销售任务管理", tags = "小程序销售任务管理")
@RequestMapping(value = VALID_MOBILE_PATH+"/sale-task")
public class ProductionManageSaleTaskMController {

    @Autowired
    private ProductionManageSaleTaskComparisonService comparisonService;

    @Autowired
    private ProductionManageSaleTaskService saleTaskService;

    @NeedAdvancedSearch
    @GetMapping("/month/sale-task-comparison")
    @ApiOperation(value = "月度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSTCResponseVO>>> monthSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.monthSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @GetMapping("/quarter/sale-task-comparison")
    @ApiOperation(value = "季度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSTCResponseVO>>> quarterSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.quarterSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @GetMapping("/year/sale-task-comparison")
    @ApiOperation(value = "年度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchSTCResponseVO>>> yearSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.yearSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @GetMapping("/department/list")
    @ApiOperation(value = "获取部门销售任务列表信息", notes = "任务类型为1：部门类型")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<DepartmentSaleTaskResponseVO>>> listForDepartment(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        return saleTaskService.listForDepartment(requestVO);
    }

    @NeedAdvancedSearch
    @GetMapping("/personal/list")
    @ApiOperation(value = "获取个人销售任务列表信息", notes = "任务类型为0：个人类型")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public  RestResult<AbstractPageService.PageResults<List<PersonalSaleTaskResponseVO>>> listForPersonal(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        return saleTaskService.listForPersonal(requestVO);
    }

    @PostMapping("/department/add")
    @ApiOperation(value = "制定部门任务", notes = "制定部门销售任务/新建部门销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult createDepartment(@Validated @RequestBody MakingDepartmentSaleTaskRequestDTO requestVO) throws SuperCodeException {
        saleTaskService.createDepartment(requestVO);
        return RestResult.success();
    }

    @PostMapping("/personal/add")
    @ApiOperation(value = "制定个人任务", notes = "制定个人销售任务/新建个人销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult createPersonal(@Validated @RequestBody MakingPersonalSaleTaskRequestDTO requestVO) throws SuperCodeException {
        saleTaskService.createPersonal(requestVO);
        return RestResult.success();
    }

    @PostMapping("/department/update")
    @ApiOperation(value = "编辑部门任务",  notes = "修改部门销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateDepartment(@RequestBody @Validated UpdateDepartmentSaleTaskRequestDTO requestVO) throws SuperCodeException {
        saleTaskService.updateDepartment(requestVO);
        return RestResult.success();
    }

    @PostMapping("/personal/update")
    @ApiOperation(value = "编辑个人任务",  notes = "修改个人销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updatePersonal(@RequestBody @Validated UpdatePersonalSaleTaskRequestDTO requestVO) throws SuperCodeException {
        saleTaskService.updatePersonal(requestVO);
        return RestResult.success();
    }

    @GetMapping("/personal/detail")
    @ApiOperation(value = "获取个人销售任务信息", notes = "通过任务编号获取指定的个人任务信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PersonalSaleTaskResponseVO> getPersonalById(@RequestParam("id") Long id) throws SuperCodeException {
        if (Objects.isNull(id) || id < 1) {
            throw new SuperCodeException("个人任务编号不可为空", 400);
        }

        return RestResult.success(saleTaskService.getPersonalById(id));
    }

    @GetMapping("/department/detail")
    @ApiOperation(value = "获取部门销售任务信息", notes = "通过任务编号获取指定的部门任务信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<DepartmentSaleTaskResponseVO> getDepartmentById(@RequestParam("id") Long id) throws SuperCodeException {
        if (Objects.isNull(id) || id < 1) {
            throw new SuperCodeException("部门任务编号不可为空", 400);
        }

        return RestResult.success(saleTaskService.getDepartmentById(id));
    }

    @NeedAdvancedSearch
    @GetMapping("/department/quarter/statistics")
    @ApiOperation(value = "获取部门季度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<DepartmentSaleTaskStatisticsVO>>> listDepartmentQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listDepartmentQuarterlyStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @GetMapping("/department/year/statistics")
    @ApiOperation(value = "获取部门年度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<DepartmentSaleTaskStatisticsVO>>> listDepartmentYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listDepartmentYearStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @GetMapping("/personal/quarter/statistics")
    @ApiOperation(value = "获取个人季度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<PersonalSaleTaskStatisticsVO>>> listPersonalQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listPersonalQuarterlyStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @GetMapping("/personal/year/statistics")
    @ApiOperation(value = "获取个人年度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<PersonalSaleTaskStatisticsVO>>> listPersonalYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listPersonalYearStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @GetMapping("/department/month/list")
    @ApiOperation(value = "获取部门月度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<DepartmentSaleTaskMonthResponseVO>>> listDepartmentMonthStatistics(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listForDepartmentMonth(requestVO));
    }

    @PostMapping("/department/update-remark")
    @ApiOperation(value = "编辑部门销售任务反馈")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateRemark(@RequestBody @Validated UpdateSaleTaskRemarkRequestDTO requestDTO) throws SuperCodeException {
        saleTaskService.updateRemark(requestDTO);
        return RestResult.success();
    }
}