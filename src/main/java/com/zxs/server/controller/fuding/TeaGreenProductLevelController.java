package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenProductLevelDO;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.TeaGreenProductLevelService;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenProductLevelAddModel;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author caihong
 * @since 2021-07-05
 */
@RestController
@RequestMapping(VALID_PATH+"/teaGreenProductLevel")
@Api(value = "农产品等级管理", tags = "农产品等级管理")
public class TeaGreenProductLevelController {

    @Autowired
    private TeaGreenProductLevelService service;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody TeaGreenProductLevelAddModel obj) {
        service.add(obj);
        return new RichResult();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@PathVariable("id") Integer id, @RequestBody TeaGreenProductLevelAddModel obj) {
        service.update(obj, id);
        return new RichResult();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult<TeaGreenProductLevelDO> getById(@RequestParam("id") Integer id) {
        return new RichResult<>(service.getById(id));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult remove(@PathVariable("id") Integer id) {
        service.delete(id);
        return new RichResult();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<TeaGreenProductLevelDO>>> list(DaoSearch daoSearch) {
        return new RichResult<>(CommonUtil.iPageToPageResults(service.listPage(daoSearch), null));
    }

    @GetMapping("/select")
    @ApiOperation(value = "获取下拉列表", notes = "获取下拉列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<TeaGreenProductLevelDO>>> select(DaoSearch daoSearch, @RequestParam(name = "productLevelName", required = false) String productLevelName) {
        daoSearch.setSearch(productLevelName);
        return new RichResult<>(CommonUtil.iPageToPageResults(service.selectByOrgAndSysId(daoSearch), null));
    }
}
