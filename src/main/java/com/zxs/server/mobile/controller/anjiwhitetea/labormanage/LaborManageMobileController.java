package com.zxs.server.mobile.controller.anjiwhitetea.labormanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageListDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.LaborManageUpdateDTO;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.LaborManageService;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.LaborManageListVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;


/**
 * <p>
 * 用工管理 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/labor-manage")
@Api(value = "移动端用工管理", tags = "移动端用工管理")
public class LaborManageMobileController {

    @Autowired
    private LaborManageService service;

    @PutMapping
    @ApiOperation(value = "添加用工管理", notes = "添加用工管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody LaborManageAddDTO laborManageAddDTO) {
        service.add(laborManageAddDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PostMapping
    @ApiOperation(value = "编辑用工管理", notes = "编辑用工管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody LaborManageUpdateDTO updateDTO) {
        service.update(updateDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/get-by-id")
    @ApiOperation(value = "获取用工管理详情", notes = "获取用工管理详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult getById(@RequestParam("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @DeleteMapping
    @ApiOperation(value = "删除用工管理", notes = "删除用工管理")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult remove(@RequestParam("id") Integer id) {
        service.removeById(id);
        return RestResult.ok();
    }

    @RequestMapping(value ="/list" , method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取用工管理列表", notes = "获取用工管理列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<LaborManageListVO>>> list(LaborManageListDTO laborManageListDTO) {
        return RestResult.ok(service.pageList(laborManageListDTO));
    }

    /**
     * 信息导出
     */
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(LaborManageListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, null, LaborManageListVO.class, response);
    }
}
