package com.zxs.server.mobile.controller.anjiwhitetea.labormanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketManage;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketManageService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 * 竹筐管理 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/bamboo-basket-manage")
@Api(value = "移动端竹筐管理", tags = "移动端竹筐管理")
public class BambooBasketManageMobileController {

    @Autowired
    private BambooBasketManageService service;

    @PutMapping
    @ApiOperation(value = "添加竹筐管理", notes = "添加竹筐管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestParam("serialNumber") String serialNumber) {
        service.add(serialNumber);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PostMapping
    @ApiOperation(value = "编辑竹筐管理", notes = "编辑竹筐管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestParam("id") Long id,@RequestParam("serialNumber") String serialNumber) {
        service.update(id,serialNumber);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PostMapping("/setUseStatus")
    @ApiOperation(value = "启用禁用", notes = "启用禁用")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true),
            @ApiImplicitParam(name = "useStatus", paramType = "query", defaultValue = "", value = "0为禁用、1为启用", required = true)
    })
    public RichResult setUseStatus(@RequestParam("id") Long id, Integer useStatus) {
        service.changeUseStatus(id, useStatus);
        return RestResult.ok();
    }


    @RequestMapping(value = "/list",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "获取竹筐管理列表", notes = "获取竹筐管理列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<BambooBasketManage>>> list(BambooBasketListDTO basketListDTO) {
        return RestResult.ok(service.pageList(basketListDTO));
    }
	
	    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(BambooBasketListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, null, BambooBasketManage.class, response);
    }

    @GetMapping("/pull")
    @ApiOperation(value = "竹筐下拉", notes = "竹筐下拉")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<BambooBasketManage>>> pull(BambooBasketListDTO basketListDTO) {
        return RestResult.ok(service.pageList(basketListDTO));
    }
	
	
}
