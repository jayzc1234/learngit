package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageProductRecordService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductRecordDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductRecordResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductStockStatisticsResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 产品档案-控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-29
 */
@RestController
@RequestMapping(VALID_PATH+"/product-record")
@Api(value = "产品档案管理", tags = "产品档案管理")
public class ProductionManageProductRecordController extends CommonUtil {
    // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageProductRecordService service;

    @GetMapping("/detail")
    @ApiOperation(value = "获取产品档案详情", notes = "获取产品档案详情")
    public RestResult<SearchProductRecordDetailResponseVO> detail(@RequestParam("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "产品档案主键不合法");
        return RestResult.success(service.getDetail(id));
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取产品档案列表", notes = "获取产品档案列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchProductRecordResponseVO>>> list(SearchProductRecordRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    /**
     * 信息导出
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchProductRecordRequestDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, getExportNumber(), "产品档案信息", response);
    }

    /**
     * 通过mq来更新产品档案信息，仅用于更新种植相关信息
     */
    @PostMapping("/update")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody PlantingBatchResponseDTO dto) {
        service.updateByMq(dto);
        return RestResult.success();
    }


    @GetMapping("/list/statistics")
    @ApiOperation(value = "获取产品档案统计信息", notes = "获取产品档案统计信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<SearchProductStockStatisticsResponseVO> statistics(SearchProductRecordStatisticsRequestDTO requestDTO) throws SuperCodeException {
        // 校验时间是否正确
        CustomAssert.isFalse(LocalDate.parse(requestDTO.getStartQueryDate())
                .isAfter(LocalDate.parse(requestDTO.getEndQueryDate())), "开始日期不可大于结束日期");
        return RestResult.success(service.statistics(requestDTO));
    }

    @GetMapping("/principalName-data-sync")
    @ApiOperation(value = "区域负责人数据同步", notes = "区域负责人数据同步")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult principalNameDataSync() {
        service.principalNameDataSync();
        return RestResult.success("区域负责人数据同步完成");
    }
}