package com.zxs.server.controller.anjiwhitetea.teapicker;


import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.TeaPickerListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.TeaPicker;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.impl.TeaPickerServiceImpl;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-13
 */
@RestController
@RequestMapping(VALID_PATH+"/teaPicker")
@Api(value = "采茶工管理", tags = "采茶工管理")
public class TeaPickerController {

    @Autowired
    private TeaPickerServiceImpl service;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult add(@RequestBody TeaPicker obj) {
        service.add(obj);
        return RestResult.ok();
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody TeaPicker obj) {
        service.update(obj);
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
        service.delete(id);
        return RestResult.ok();
    }

    @RequestMapping(value ="/list" , method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<PageResults<List<TeaPicker>>> list(TeaPickerListDTO obj) {
        return RestResult.ok(service.list(obj));
    }

    @PostMapping("/export")
    @NeedAdvancedSearch
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(TeaPickerListDTO request, HttpServletResponse response) throws Exception {
        service.exportList(request, response);
    }


}
