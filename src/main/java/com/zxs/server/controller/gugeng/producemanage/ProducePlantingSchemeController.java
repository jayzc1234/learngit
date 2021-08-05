package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.ProducePlantingSchemeDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProducePlantingSchemeServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ObjectUniqueValueResponseVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2021-07-05
 */
@RestController
@RequestMapping(VALID_PATH + "/planting/scheme")
@Api(value = "", tags = "标准种植方案")
public class ProducePlantingSchemeController {

    @Autowired
    private ProducePlantingSchemeServiceImpl service;

    @PostMapping("/add")
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody ProducePlantingSchemeDTO obj) {
        service.add(obj);
        return RestResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody ProducePlantingSchemeDTO obj) {
        service.update(obj);
        return RestResult.ok();
    }

    @GetMapping(value = "/detail")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<ProducePlantingSchemeDTO> getById(@RequestParam("id") Integer id) {
        return RestResult.ok(service.get(id));
    }


    @DeleteMapping("/remove")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult remove(@RequestParam("id") Integer id) {
        service.removeById(id);
        return RestResult.ok();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<ProducePlantingSchemeDTO>>> list(DaoSearch obj) {
        return RestResult.ok(service.selectList(obj));
    }


    @ApiOperation(value = "获取字段列表", notes = "")
    @GetMapping("/field")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<ObjectUniqueValueResponseVO>>> listfield(DaoSearch requestDTO) throws Exception {
        return RestResult.success(service.listfield(requestDTO));
    }

}
