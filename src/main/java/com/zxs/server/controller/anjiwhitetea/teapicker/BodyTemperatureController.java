package com.zxs.server.controller.anjiwhitetea.teapicker;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BodyTemperatureAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BodyTemperatureListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BodyTemperature;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.impl.BodyTemperatureServiceImpl;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
@RestController
@RequestMapping(VALID_PATH+"/bodyTemperature")
@Api(value = "测量体温", tags = "测量体温")
public class BodyTemperatureController {

    @Autowired
    private BodyTemperatureServiceImpl service;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult add(@RequestBody BodyTemperatureAddDTO obj) {
        service.add(obj);
        return RestResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody BodyTemperature obj) {
        service.updateById(obj);
        return RestResult.ok();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult getById(@RequestParam("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult remove(@PathVariable("id") Integer id) {
        service.removeById(id);
        return RestResult.ok();
    }

    @RequestMapping(value ="/list" , method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<BodyTemperature>>> list(BodyTemperatureListDTO obj) {
        return RestResult.ok(service.list(obj));
    }

    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(BodyTemperatureListDTO request, HttpServletResponse response) throws Exception {
        service.exportList(request, response);
    }
}
