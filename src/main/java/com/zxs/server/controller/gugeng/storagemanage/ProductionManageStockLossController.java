package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddCodeLessStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddTILRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ConfirmCodeLessStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageStockLossService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockLossDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockLossResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.NOT_SELECTED;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryLossErrorMsgConstants.REPORT_LOSS_PARAM_WRITE_AT_LEAST_ONE;


/**
 * <p>
 * 库存报损 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStockLoss")
@Api(value = "库存报损", tags = "库存报损接口")
public class ProductionManageStockLossController extends CommonUtil {

    @Autowired
    private ProductionManageStockLossService service;


    @GetMapping("/detail")
    @ApiOperation(value = "库存报损详情", notes = "库存报损详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "12345", value = "库存报损表主键id", required = true),
    })
    public RestResult<SearchStockLossDetailResponseVO> detail(@RequestParam("id") Long id) throws SuperCodeException {
        CustomAssert.numberIsLegal(id, "库存报损主键id不可为空");
        return RestResult.success(service.detail(id));
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @NeedAdvancedSearch
    @ApiOperation(value = "库存报损列表", notes = "库存报损列表接口")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchStockLossResponseVO>>> list(SearchStockLossRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "库存报损列表导出", notes = "库存报损列表导出")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchStockLossRequestDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(listDTO, response);
    }

    @PostMapping("/code-less/damage")
    @ApiOperation(value = "新增库存报损", notes = "新增库存报损")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult codeLessDamage(@RequestBody @Validated AddCodeLessStockLossRequestDTO requestDTO) throws SuperCodeException {
        // 库存报损至少录入一个数值数据
        CustomAssert.validSaveDataIsLegal(requestDTO.getHandleBoxNum(), requestDTO.getDamageNum(),
                requestDTO.getDamageWeight(), REPORT_LOSS_PARAM_WRITE_AT_LEAST_ONE);

        if (StringUtils.isBlank(requestDTO.getProductLevelCode())) {
            requestDTO.setProductLevelCode(StringUtils.EMPTY);
        }
        service.codeLessDamage(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/code-less/confirm")
    @ApiOperation(value = "报损确认", notes = "报损确认")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult confirmCodeLessStockLoss(@RequestBody @Validated ConfirmCodeLessStockLossRequestDTO requestDTO) throws SuperCodeException {
        boolean isTrue = Objects.isNull(requestDTO.getHandleBoxNum()) && Objects.isNull(requestDTO.getDamageNum()) && Objects.isNull(requestDTO.getDamageWeight());
        CustomAssert.isFalse(isTrue, REPORT_LOSS_PARAM_WRITE_AT_LEAST_ONE);
        // 方便后续的redis锁的获取
        if (StringUtils.isEmpty(requestDTO.getProductLevelCode())) {
            requestDTO.setProductLevelCode(StringUtils.EMPTY);
        }
        service.confirmCodeLessStockLoss(requestDTO);
        return RestResult.success();
    }

    /**
     * v1.8新增全部库存报损，将库存中的数值信息全部清0
     *
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @PostMapping("/total-inventory-loss")
    @ApiOperation(value = "全部库存报损", notes = "全部库存报损")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult totalInventoryLoss(@RequestBody @Validated AddTILRequestDTO requestDTO) throws SuperCodeException {
        // 校验部门信息是否存在，不存在则默认为未选择
        if (StringUtils.isBlank(requestDTO.getDepartmentId())) {
            requestDTO.setDepartmentId(NOT_SELECTED);
            requestDTO.setDepartmentName(NOT_SELECTED);
        }
        service.totalInventoryLoss(requestDTO);
        return RestResult.success();
    }
}