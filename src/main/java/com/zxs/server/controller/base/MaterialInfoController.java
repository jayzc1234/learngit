package com.zxs.server.controller.base;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoUpdateDTO;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInfoService;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoDetailVO;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoListVO;
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
@RequestMapping(VALID_PATH+"/material/info")
@Api(value = "物料管理", tags = "物料管理")
public class MaterialInfoController {

    @Autowired
    private MaterialInfoService service;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody MaterialInfoDTO materialInfoDTO) {
        service.add(materialInfoDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@PathVariable("id") Long id, @RequestBody MaterialInfoUpdateDTO materialInfoUpdateDTO) {
        service.update(id,materialInfoUpdateDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<MaterialInfoDetailVO> getById(@PathVariable("id") Long id) {
        return RestResult.ok(service.getDetailById(id));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/disable/{id}")
    @ApiOperation(value = "禁用/启用", notes = "禁用/启用")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult enable(@PathVariable("id") Long id,@RequestParam("disableFlag") Integer disableFlag) {
        service.disable(id,disableFlag);
        return RestResult.ok();
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<MaterialInfoListVO>>> list(
                                                                  @RequestParam(value = "disableFlag",required = false) Integer disableFlag,
                                                                  @RequestParam(value ="materialSortId",required = false) Long materialSortId,DaoSearch daoSearch) {
        return CommonUtil.pageResult(service.pageList(disableFlag,materialSortId,daoSearch),null);
    }

}
