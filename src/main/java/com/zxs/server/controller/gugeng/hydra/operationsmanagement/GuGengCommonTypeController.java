package com.zxs.server.controller.gugeng.hydra.operationsmanagement;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengCommonTypeDTO;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengCommonTypeListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengCommonType;
import net.app315.hydra.intelligent.planting.server.service.gugeng.hydra.operationsmanagement.GuGengCommonTypeServiceImpl;
import net.app315.hydra.user.data.auth.sdk.annotation.NeedDataAuth;
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
 * @since 2019-12-02
 */
@RestController
@RequestMapping(VALID_PATH+"/guGengCommonType")
@Api(value = "房间类型/娱乐项目接口", tags = "房间类型/娱乐项目接口")
public class GuGengCommonTypeController {

    @Autowired
    private GuGengCommonTypeServiceImpl service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "新增")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult  save(@RequestBody GuGengCommonTypeDTO commonTypeDTO) throws SuperCodeException {
        service.add(commonTypeDTO);
        return new RichResult ();
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改", notes = "修改")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult  update(@RequestBody GuGengCommonTypeDTO commonTypeDTO) throws SuperCodeException {
        service.update(commonTypeDTO);
        return new RichResult ();
    }

    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult  delete(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteById(id);
        return new RichResult ();
    }

    @NeedDataAuth
    @RequestMapping("/list")
    @ApiOperation(value = "列表", notes = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<GuGengCommonType>>>  list(GuGengCommonTypeListDTO daoSearch) throws SuperCodeException {
        RichResult<AbstractPageService.PageResults<List<GuGengCommonType>>> richResult=new RichResult();
        richResult.setState(200);
        IPage<GuGengCommonType> iPage=service.pageList(daoSearch);
        AbstractPageService.PageResults<List<GuGengCommonType>> listPageResults = CommonUtil.iPageToPageResults(iPage, null);
        richResult.setResults(listPageResults);
        return richResult;
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "下拉接口", notes = "下拉")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<GuGengCommonType>>>  dropDown(GuGengCommonTypeListDTO daoSearch) throws SuperCodeException {
        RichResult<AbstractPageService.PageResults<List<GuGengCommonType>>> richResult=new RichResult();
        richResult.setState(200);
        IPage<GuGengCommonType> iPage=service.dropDown(daoSearch);
        AbstractPageService.PageResults<List<GuGengCommonType>> listPageResults = CommonUtil.iPageToPageResults(iPage, null);
        richResult.setResults(listPageResults);
        return richResult;
    }

}
