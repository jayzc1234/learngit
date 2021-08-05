package com.zxs.server.controller.gugeng.salemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengContactManDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengContactManListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.GuGengContactMan;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.GuGengContactManService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-28
 */
@RestController
@RequestMapping(VALID_PATH+"/guGengContactMan")
@Api(value = "古耕联系人接口", tags = "古耕联系人接口")
public class GuGengContactManController {

    @Autowired
    private GuGengContactManService service;

    @PostMapping("/save")
    @ApiOperation(value = "", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid  @RequestBody GuGengContactManDTO gengContactManDTO) throws SuperCodeException {
        service.add(gengContactManDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody GuGengContactManDTO gengContactManDTO) throws SuperCodeException {
        service.update(gengContactManDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "详情", notes = "")
    public RestResult getById(@RequestParam("id") Long id) throws SuperCodeException {
        return RestResult.success(service.getById(id));
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "下拉接口", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<GuGengContactMan>>> dropDown(GuGengContactManListDTO daoSearch) throws SuperCodeException {
        return RestResult.success(CommonUtil.iPageToPageResults(service.dropDown(daoSearch),null));
    }
}
