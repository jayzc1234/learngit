package com.zxs.server.mobile.controller.gugeng.storagemanage;


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
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageSortInstorageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchDropDownInStorageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortStoragePbIdResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortingStorageDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortingStorageResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InStorageErrorMsgConstants.*;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @date 2019-07-16
 * @updateDate 2019-09-01
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/storagemanage/productSortInstorage")
@Api(tags = "分拣入库管理")
public class ProductSortInstorageMController extends CommonUtil {

    @Autowired
    private ProductionManageSortInstorageService service;

    @PostMapping("/save")
    @ApiOperation(value = "产品分拣入库", notes = "产品分拣入库")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddSortStorageRequestDTO requestDTO) throws SuperCodeException {

        // 校验分拣入库至少录入一个入库数值数据
        CustomAssert.validSaveDataIsLegal(requestDTO.getBoxNum(), requestDTO.getQuantity(),
                requestDTO.getWeight(), ENTER_AT_LEAST_ONE_INBOUND_DATA);

        // 如果未选择产品等级，则对相关数据进行初始化
        if (StringUtils.isBlank(requestDTO.getProductLevelCode())) {
            requestDTO.setProductLevelCode(StringUtils.EMPTY);
            requestDTO.setProductLevelName(StringUtils.EMPTY);
            requestDTO.setProductSpecCode(StringUtils.EMPTY);
            requestDTO.setProductSpecName(StringUtils.EMPTY);
            requestDTO.setSortingSpecCode(StringUtils.EMPTY);
            requestDTO.setSortingSpecName(StringUtils.EMPTY);
        }
        service.add(requestDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑产品分拣入库", notes = "只能编辑分拣入库的重量")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody UpdateCodeLessSortingStorageRequestDTO requestDTO) throws SuperCodeException {
        // 校验分拣入库至少录入一个入库数值数据
        CustomAssert.validSaveDataIsLegal(requestDTO.getBoxNum(), requestDTO.getQuantity(),
                requestDTO.getWeight(), ENTER_AT_LEAST_ONE_INBOUND_DATA);
        service.update(requestDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "产品分拣入库详情信息", notes = "产品分拣入库详情信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分拣入库主键id", dataType = "Long", required = true),
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    })
    public RestResult<SearchSortingStorageDetailResponseVO> detail(@RequestParam(value = "id") Long id) {
        return RestResult.success(service.getDetail(id));
    }

    @NeedAdvancedSearch
    @GetMapping("/list")
    @ApiOperation(value = "获取分拣入库列表", notes = "获取分拣入库列表信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "advancedSearch", paramType = "header", defaultValue = "高级搜索", value = "高级检索", required = true)
    })
    public RestResult<AbstractPageService.PageResults<List<SearchSortingStorageResponseVO>>> list(SearchSortingStorageRequestDTO requestDTO) throws SuperCodeException {
        return RestResult.success(service.list(requestDTO));
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "获取入库的批次", notes = "获取入库的批次")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<SearchDropDownInStorageResponseVO>>> dropDown(ProductInStorageDropDownDTO productInStorageDTO) throws SuperCodeException {
        int type;
        // 如果为种植批次，则分拣类型为采收分拣 为 2
        if (productInStorageDTO.getBatchType() == BatchTypesEnum.PLANT.getKey()) {
            type = SortingInventoryTypeEnum.HARVEST_SORTING.getKey();
        } else {
            type = SortingInventoryTypeEnum.OUTSIDE_SORTING.getKey();
        }
        productInStorageDTO.setBatchType(type);
        return RestResult.success(service.listBatchByType(productInStorageDTO));
    }

    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(SearchSortingStorageRequestDTO clientListDTO, HttpServletResponse response) throws Exception {
        service.exportExcel(clientListDTO, response);
    }

    /**
     * 通过批次id获取分拣入库列表信息
     *
     * @return
     */
    @GetMapping("/list-by-batch-id")
    @ApiOperation(value = "通过批次id获取分拣入库列表", notes = "通过批次id获取分拣入库列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    })
    public RestResult<AbstractPageService.PageResults<List<SearchSortStoragePbIdResponseVO>>> listByPlantBatchId(SearchSortStorageByPbIdRequestDTO requestDTO) throws SuperCodeException {
        CustomAssert.isNotBlank(requestDTO.getPlantBatchId(), PLANT_BATCH_ID_IS_NOT_NULL);
        return RestResult.success(service.listByPlantBatchId(requestDTO));
    }
}