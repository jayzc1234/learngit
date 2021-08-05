package com.zxs.server.controller.base;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialOutWarehouseService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 物料出库表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
@RestController
@RequestMapping(VALID_PATH+"/material/outWarehouse")
@Api(value = "物料出库表", tags = "物料出库表")
public class MaterialOutWarehouseController {

    @Autowired
    private MaterialOutWarehouseService service;


    @ApiOperation("物料出库")
    @PostMapping("/out")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult out(@Valid @RequestBody MaterialInfoOutWarehouseDTO params) throws SuperCodeException {
        service.out(params);
        return RestResult.ok();
    }

    @ApiOperation("物料批量出库")
    @PostMapping("/batch/out")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult batchOut(@Valid @RequestBody MaterialInfoBatchOutWarehouseDTO params) throws SuperCodeException {
        service.batchOut(params);
        return RestResult.ok();
    }

}
