package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffSortDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffSortService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSortVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@RestController
@RequestMapping(VALID_PATH+"/stuffSort")
@Api(value = "材料分类", tags = "材料分类")
public class ProductionManageStuffSortController {

    @Autowired
    private ProductionManageStuffSortService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid  @RequestBody ProductionManageStuffSortDTO stuffSortDTO) throws SuperCodeException, ParseException {
        service.add(stuffSortDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody ProductionManageStuffSortDTO stuffSortDTO) throws SuperCodeException {
        service.update(stuffSortDTO);
        return RestResult.success();
    }

    @DeleteMapping("/deleteOne")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    @ApiOperation(value = "删除", notes = "")
    public RestResult deleteOne(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteOne(id);
        return RestResult.success();
    }

    @GetMapping("/first")
    @ApiOperation(value = "获取最外层分类列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionManageStuffSortVO>> first() throws SuperCodeException {
        List<ProductionManageStuffSortVO> list=service.first();
        return RestResult.success(list);
    }

    @GetMapping("/children")
    @ApiOperation(value = "根据父分类id获取子层次分类", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionManageStuffSortVO>> children(@ApiParam(value = "要获取子分类的父分类主键id") @RequestParam("previousSortId") Long previousSortId) throws SuperCodeException {
        List<ProductionManageStuffSortVO> list=service.children(previousSortId);
        return RestResult.success(list);
    }

}
