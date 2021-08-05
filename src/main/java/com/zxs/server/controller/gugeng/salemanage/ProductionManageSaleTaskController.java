package com.zxs.server.controller.gugeng.salemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.job.gugeng.SaleTargetDataStatisticsJob;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskComparisonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@Slf4j
@RestController
@Api(value = "销售任务管理", tags = "销售任务管理")
@RequestMapping(value = VALID_PATH+"/sale-task")
public class ProductionManageSaleTaskController {

    @Autowired
    private ProductionManageSaleTaskService saleTaskService;

    @Autowired
    private ProductionManageSaleTaskComparisonService comparisonService;

    @Autowired
    private SaleTargetDataStatisticsJob job;

    @NeedAdvancedSearch
    @RequestMapping(value = "/department/list",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取部门销售任务列表信息", notes = "任务类型为1：部门类型")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<DepartmentSaleTaskResponseVO>>> listForDepartment(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        return saleTaskService.listForDepartment(requestVO);
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/personal/list",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取个人销售任务列表信息", notes = "任务类型为0：个人类型")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public  RestResult<PageResults<List<PersonalSaleTaskResponseVO>>> listForPersonal(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
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
    @RequestMapping(value = "/department/quarter/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取部门季度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<DepartmentSaleTaskStatisticsVO>>> listDepartmentQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listDepartmentQuarterlyStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/department/year/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取部门年度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<DepartmentSaleTaskStatisticsVO>>> listDepartmentYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listDepartmentYearStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/personal/quarter/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取个人季度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<PersonalSaleTaskStatisticsVO>>> listPersonalQuarterlyStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listPersonalQuarterlyStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/personal/year/statistics",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取个人年度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<PersonalSaleTaskStatisticsVO>>> listPersonalYearStatistics(SaleTaskStatisticsRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listPersonalYearStatistics(requestVO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/department/month/list",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取部门月度销售任务")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<DepartmentSaleTaskMonthResponseVO>>> listDepartmentMonthStatistics(ProductManageSaleTaskRequestDTO requestVO) throws SuperCodeException {
        return RestResult.success(saleTaskService.listForDepartmentMonth(requestVO));
    }

    @PostMapping("/department/update-remark")
    @ApiOperation(value = "编辑部门销售任务反馈")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult updateRemark(@RequestBody @Validated UpdateSaleTaskRemarkRequestDTO requestDTO) throws SuperCodeException {
        saleTaskService.updateRemark(requestDTO);
        return RestResult.success();
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/department/export")
    @ApiOperation(value = "部门销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void departmentExport(ProductManageSaleTaskRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.departmentExport(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/department/quarter/export")
    @ApiOperation(value = "部门季度销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void departmentQuarterExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.departmentQuarterExport(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/department/year/export")
    @ApiOperation(value = "部门年度销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void departmentYearExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.departmentYearExport(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/personal/export")
    @ApiOperation(value = "个人销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void personalExport(ProductManageSaleTaskRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.personalExport(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/personal/quarter/export")
    @ApiOperation(value = "个人季度销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void personalQuarterExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.personalQuarterExport(requestDTO, response);
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/personal/year/export")
    @ApiOperation(value = "个人年度销售任务导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void personalYearExport(SaleTaskStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        saleTaskService.personalYearExport(requestDTO, response);
    }

    @GetMapping("/data-synchronization")
    @ApiOperation(value = "个人销售任务部门信息同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult personalDataSynchronization() {
        job.personalDataSynchronization();
        return RestResult.success("个人销售任务部门信息同步完成");
    }

    @GetMapping("/comparison-data-synchronization")
    @ApiOperation(value = "销售任务对比数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult saleTaskComparisonDataSynchronization() {
        job.saleTaskComparisonDataSynchronization();
        return RestResult.success("销售任务对比数据同步完成");
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/month/sale-task-comparison",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "月度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchSTCResponseVO>>> monthSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.monthSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/quarter/sale-task-comparison",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "季度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchSTCResponseVO>>> quarterSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.quarterSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/year/sale-task-comparison",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "年度销售任务对比")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<PageResults<List<SearchSTCResponseVO>>> yearSalesTaskComparison(SearchSTCRequestDTO requestDTO) {
        return RestResult.success(comparisonService.yearSalesTaskComparison(requestDTO));
    }

    @NeedAdvancedSearch
    @PostMapping("/month/stc/export")
    @ApiOperation(value = "月度销售任务对比导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void monthSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        comparisonService.monthSTCExport(requestDTO, response);
    }

    @NeedAdvancedSearch
    @PostMapping("/quarter/stc/export")
    @ApiOperation(value = "季度销售任务对比导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void quarterSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        comparisonService.quarterSTCExport(requestDTO, response);
    }

    @NeedAdvancedSearch
    @PostMapping("/year/stc/export")
    @ApiOperation(value = "年度销售任务对比导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void yearSTCExport(SearchSTCRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        comparisonService.yearSTCExport(requestDTO, response);
    }
}