package com.zxs.server.controller.base;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.base.ProductLevelMaintain;
import net.app315.hydra.intelligent.planting.server.service.base.ProductLevelMaintainService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-10-14
 */
@RestController
@RequestMapping(VALID_PATH+"/product/level/maintain")
@Api(value = "产品等级维护", tags = "产品等级维护")
public class ProductLevelMaintainController {

    @Autowired
    private ProductLevelMaintainService service;

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@PathVariable("id") Long id, @RequestParam String qualityLevelName) {
        UpdateWrapper<ProductLevelMaintain> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(ProductLevelMaintain.COL_QUALITY_LEVEL_NAME,qualityLevelName);
        updateWrapper.eq(ProductLevelMaintain.COL_ID,id);
        service.update(updateWrapper);
        return RestResult.ok();
    }


    @GetMapping("/list")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<ProductLevelMaintain>>> list(DaoSearch daoSearch, HttpServletRequest request) {
        return RestResult.ok(CommonUtil.iPageToPageResults(service.listPage(daoSearch),null));
    }

    @GetMapping("/select")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<ProductLevelMaintain>> select(String organizationId,String sysId,String productLevelName) {
        return RestResult.ok(service.selectByOrgAndSysId(organizationId,sysId,productLevelName));
    }
}
