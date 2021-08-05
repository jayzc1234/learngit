package com.zxs.server.controller.gugeng.salemanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengClientMaintainDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.GuGengClientMaintainListDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.GuGengClientMaintainService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.GuGengClientMaintainDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
@RequestMapping(VALID_PATH+"/guGengClientMaintain")
@Api(value = "古耕--客户维护", tags = "古耕--客户维护")
public class GuGengClientMaintainController extends CommonUtil {

    @Autowired
    private GuGengClientMaintainService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid  @RequestBody GuGengClientMaintainDTO gengClientMaintainDTO) throws SuperCodeException {
        service.add(gengClientMaintainDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody GuGengClientMaintainDTO gengClientMaintainDTO) throws SuperCodeException {
        service.update(gengClientMaintainDTO);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "详情", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<GuGengClientMaintainDetailVO> getById(@RequestParam("id") Long id) throws SuperCodeException {
        GuGengClientMaintainDetailVO guGengClientMaintainDetailVO=service.getDetailById(id);
        return RestResult.success(guGengClientMaintainDetailVO);
    }

    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除", notes = "删除")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<String> delete(@RequestParam("id") Long id) throws SuperCodeException {
        service.deleteById(id);
        return RestResult.success();
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.GET,RequestMethod.POST})
    @ApiOperation(value = "列表接口", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(GuGengClientMaintainListDTO clientMaintainListDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(clientMaintainListDTO),null);
    }

    @GetMapping("/listByClientId")
    @ApiOperation(value = "客户维护详情接口", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult listByClientId(GuGengClientMaintainListDTO clientMaintainListDTO) throws SuperCodeException {
        return CommonUtil.pageResult(service.pageList(clientMaintainListDTO),null);
    }

    /**
     * 信息导出
     */
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(GuGengClientMaintainListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "巡检管理", GuGengClientMaintainDetailVO.class, response);
    }
}
