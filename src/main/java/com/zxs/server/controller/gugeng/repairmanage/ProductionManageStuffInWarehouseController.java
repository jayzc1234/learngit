package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffInWarehousePublicDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockFlowDetailDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffInWarehouse;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffInWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 材料入库表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@RestController
@RequestMapping(VALID_PATH+"/stuff/in/warehouse")
@Api(value = "材料入库", tags = "材料入库")
public class ProductionManageStuffInWarehouseController {

    @Autowired
    private ProductionManageStuffInWarehouseService service;

    @PostMapping("/save")
    @ApiOperation(value = "入库", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody ProductionManageStuffInWarehousePublicDTO inWarehousePublicDTO) throws SuperCodeException, ParseException {
        service.add(inWarehousePublicDTO);
        return RestResult.success();
    }

    @GetMapping("/dropDownBatch")
    @ApiOperation(value = "材料批次下拉", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffInWarehouse>>> batchDetailList(ProductionManageStuffStockFlowDetailDTO stuffStockFlowDetailDTO) throws SuperCodeException {
        AbstractPageService.PageResults<List<ProductionManageStuffInWarehouse>>pageResults=service.dropDownBatch(stuffStockFlowDetailDTO);
        return RestResult.success(pageResults);
    }

}
