package com.zxs.server.controller.common;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.common.DictionaryDTO;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;
import net.app315.hydra.intelligent.planting.pojo.common.SystemSetting;
import net.app315.hydra.intelligent.planting.server.service.common.DictionaryService;
import net.app315.hydra.intelligent.planting.server.service.common.SystemSettingService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-09-04
 */
@RestController
@RequestMapping(VALID_PATH+"/dictionary")
@Api(value = "全局重量单位", tags = "全局重量单位")
public class DictionaryController {

    @Autowired
    private DictionaryService service;

    @Autowired
    private SystemSettingService settingService;


    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/weight/unit")
    @ApiOperation(value = "获取企业重量单位", notes = "获取企业重量单位")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> getById() {
        return RestResult.ok(service.listWeightUnit());
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/weight/unit/un-login")
    @ApiOperation(value = "获取企业重量单位", notes = "获取企业重量单位")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> getByIdUnLogin(@RequestParam(required = false) String organizationId) {
        return RestResult.ok(service.selectWeightUnit(organizationId));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/weight/unit")
    @ApiOperation(value = "设置企业重量单位", notes = "设置企业重量单位")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> put(@RequestBody DictionaryDTO dictionary) {
        service.updateStatus(dictionary);
        return RestResult.ok();
    }

    @GetMapping("yield/list")
    @ApiOperation(value = "获取产量取值", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> listYieldType(Integer type) {
        return RestResult.ok(service.listYieldType());
    }

    @PutMapping("/yield/update")
    @ApiOperation(value = "设置产量取值", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> updateYieldType(String ids) {
        service.updateYieldType(Arrays.asList(ids.split(",")));
        return RestResult.ok();
    }

    @GetMapping("system-setting/get")
    @ApiOperation(value = "系统设置-缩放比例", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<SystemSetting> getSystemSetting() {
        return RestResult.ok(settingService.get());
    }

    @PutMapping("system-setting/update")
    @ApiOperation(value = "系统设置-缩放比例", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult updateSystemSetting(@RequestBody SystemSetting setting) {
        settingService.update(setting);
        return RestResult.ok();
    }

/*    @GetMapping("/planting/remind")
    @ApiOperation(value = "获取农事提醒", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> getPlantingRemind() {
        return RestResult.ok();
    }

    @PutMapping("/planting/remind")
    @ApiOperation(value = "设置农事提醒", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult setPlantingRemind(Integer val) {
        return RestResult.ok();
    }

    @GetMapping("/massif/manager")
    @ApiOperation(value = "获取地块管理员", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<Integer> getMassifManager() {
        return RestResult.ok();
    }

    @PutMapping("/massif/manager")
    @ApiOperation(value = "设置地块管理员", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult setMassifManager(Integer val) {
        return RestResult.ok();
    }*/

}
