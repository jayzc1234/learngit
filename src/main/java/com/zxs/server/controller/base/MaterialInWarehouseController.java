package com.zxs.server.controller.base;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchInWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInWarehouseDTO;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInWarehouseService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 物料入库表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
@RestController
@RequestMapping(VALID_PATH+"/material/inWarehouse")
@Api(value = "物料入库表", tags = "物料入库表")
public class MaterialInWarehouseController {

    @Autowired
    private MaterialInWarehouseService service;

    @PostMapping("/in")
    @ApiOperation(value = "单次入库", notes = "单次入库")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody MaterialInfoInWarehouseDTO infoInWarehouseDTO) {
        service.add(infoInWarehouseDTO);
        return RestResult.ok();
    }

    @ApiOperation("物料批量入库")
    @PostMapping("/batch/in")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult batchIn(@Valid @RequestBody MaterialInfoBatchInWarehouseDTO params) throws SuperCodeException {
        return service.batchIn(params);
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取物料入库表详情", notes = "获取物料入库表详情")
    public RichResult getById(@PathVariable("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

}
