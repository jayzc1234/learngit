package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffOutWarehousePublicDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffOutWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.text.ParseException;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 材料出库表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@RestController
@RequestMapping(VALID_PATH+"/productionManageStuffOutWarehouse")
@Api(value = "材料出库表", tags = "材料出库表")
public class ProductionManageStuffOutWarehouseController {

    @Autowired
    private ProductionManageStuffOutWarehouseService service;

    @PostMapping("/save")
    @ApiOperation(value = "出库", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid  @RequestBody ProductionManageStuffOutWarehousePublicDTO outWarehousePublicDTO) throws SuperCodeException, ParseException {
        service.add(outWarehousePublicDTO);
        return RestResult.success();
    }

}
