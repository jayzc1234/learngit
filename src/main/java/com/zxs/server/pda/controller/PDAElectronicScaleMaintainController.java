package com.zxs.server.pda.controller;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicPushDTO;
import net.app315.hydra.intelligent.planting.pojo.common.ElectronicScaleMaintain;
import net.app315.hydra.intelligent.planting.server.service.common.ElectronicScaleMaintainService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.PDA_VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2021-02-02
 */
@Slf4j
@RestController
@RequestMapping(PDA_VALID_PATH+"/electronic-scale/maintain")
@Api(value = "电子秤维护", tags = "电子秤维护")
public class PDAElectronicScaleMaintainController {

    @Autowired
    private ElectronicScaleMaintainService service;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ElectronicScaleMaintain>>> list(DaoSearch daoSearch) {
        return CommonUtil.pageResult(service.pageList(daoSearch),null);
    }

    @RequestMapping(value = "/push/info",method = {RequestMethod.POST})
    @ApiOperation(value = "保存推送id与电子秤编号关系", notes = "保存推送id与电子秤编号关系")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult pushInfo(@RequestBody ElectronicPushDTO electronicPushDTO) {
        service.pushInfo(electronicPushDTO);
        return RestResult.ok();
    }

}
