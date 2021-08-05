package com.zxs.server.mobile.controller.gugeng.salemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClientCategory;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ClientCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

@RestController
@RequestMapping(VALID_MOBILE_PATH+"/client/category")
@Api(tags = "客户类目")
public class ClientCategoryMController {
  
    @Autowired
    private ClientCategoryService service;

    @PostMapping("/add")
    @ApiOperation(value = "新增客户类目", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "categoryName", paramType = "query", defaultValue = "媒体客户", value = "类目名称", required = true),
            @ApiImplicitParam(name = "sortWeight", paramType = "query", defaultValue = "1", value = "排序字段", required = true),
    })
    public RestResult<String> add(@RequestParam String categoryName, @RequestParam Integer sortWeight) throws SuperCodeException{
        service.add(categoryName,sortWeight);
        return RestResult.success();
    }



    @PostMapping("/update")
    @ApiOperation(value = "编辑客户类目", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "类目主键", required = true),
            @ApiImplicitParam(name = "categoryName", paramType = "query", defaultValue = "媒体客户", value = "类目名称", required = true),
            @ApiImplicitParam(name = "sortWeight", paramType = "query", defaultValue = "1", value = "排序字段", required = true),
    })
    public RestResult<String> add(@RequestParam Long id, @RequestParam String categoryName, @RequestParam Integer sortWeight) throws SuperCodeException{
        service.update(id,categoryName,sortWeight);
        return RestResult.success();
    }

    @DeleteMapping("/delete")
    @ApiOperation(value = "删除客户类目", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "类目主键", required = true)
    })
    public RestResult<String> delete(@RequestParam Long id) throws SuperCodeException{
        service.delete(id);
        return RestResult.success();
    }

    @PutMapping("/sort")
    @ApiOperation(value = "排序", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "id", paramType = "query", defaultValue = "1", value = "类目主键", required = true),
            @ApiImplicitParam(name = "moveStep", paramType = "query", defaultValue = "1", value = "排序步长", required = true),
            @ApiImplicitParam(name = "direction", paramType = "query", defaultValue = "0", value = "排序方向，0向下，1向上", required = true),
    })
    public RestResult<String> sort(@RequestParam Long id, @RequestParam int moveStep, @RequestParam int direction) throws SuperCodeException{
        if (moveStep<1) {
            throw new SuperCodeException("moveStep必须为大于0的正整数", 500);
        }
        service.sort(id,moveStep,direction);
        return RestResult.success();
    }


    @GetMapping("/page")
    @ApiOperation(value = "客户类目列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
    })
    public RestResult<PageResults<List<ProductionManageClientCategory>>> page(DaoSearch daoSearch) throws Exception{
        PageResults<List<ProductionManageClientCategory>> pageResults=service.page(daoSearch);
        return RestResult.successDefault(pageResults);
    }

    @GetMapping("/dropDownPage")
    @ApiOperation(value = "客户类目下拉接口", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
    })
    public RestResult<PageResults<List<ProductionManageClientCategory>>> dropDownPage(DaoSearch daoSearch) throws Exception{
        PageResults<List<ProductionManageClientCategory>> pageResults=service.dropDownPage(daoSearch);
        return RestResult.successDefault(pageResults);
    }
}
