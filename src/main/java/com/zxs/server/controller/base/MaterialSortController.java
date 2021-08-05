package com.zxs.server.controller.base;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortListDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSort;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialSortService;
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
 * @since 2020-08-20
 */
@RestController
@RequestMapping(VALID_PATH+"/material/sort")
@Api(value = "物料分类接口", tags = "物料分类")
public class MaterialSortController {

    @Autowired
    private MaterialSortService service;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody MaterialSortDTO materialSortDTO) {
        service.add(materialSortDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody MaterialSortDTO materialSortDTO) {
        service.update(materialSortDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult getById(@PathVariable("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "禁用", notes = "禁用")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult remove(@PathVariable("id") Integer id,@RequestParam("status") Integer status) {
        service.updateStatus(id,status);
        return RestResult.ok();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<MaterialSort>>> list(MaterialSortListDTO daoSearch) {
        return CommonUtil.pageResult(service.pageList(daoSearch),null);
    }

}
