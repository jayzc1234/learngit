package com.zxs.server.controller.common;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicScaleMaintainDTO;
import net.app315.hydra.intelligent.planting.pojo.common.ElectronicScaleMaintain;
import net.app315.hydra.intelligent.planting.server.electronicscale.protocol.websocket.ConnectionHandler;
import net.app315.hydra.intelligent.planting.server.service.common.ElectronicScaleMaintainService;
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
 * @since 2021-02-02
 */
@Slf4j
@RestController
@RequestMapping(VALID_PATH+"/electronic-scale/maintain")
@Api(value = "电子秤维护", tags = "电子秤维护")
public class ElectronicScaleMaintainController {

    @Autowired
    private ElectronicScaleMaintainService service;

    @Autowired
    private ConnectionHandler connectionHandler;

    @PostMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody ElectronicScaleMaintainDTO scaleMaintainDTO) {
        service.add(scaleMaintainDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@PathVariable("id") Long id, @RequestBody ElectronicScaleMaintainDTO scaleMaintainDTO) {
        service.update(id,scaleMaintainDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    public RichResult getById(@PathVariable("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除", notes = "删除")
    public RichResult remove(@PathVariable("id") Long id) {
        service.removeById(id);
        return RestResult.ok();
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ElectronicScaleMaintain>>> list(DaoSearch daoSearch) {
        return CommonUtil.pageResult(service.pageList(daoSearch),null);
    }

    @PostMapping("/scale/add")
    @ApiOperation(value = "从电子秤添加电子秤编号", notes = "从电子秤添加电子秤编号")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult addFromScale(@RequestParam String uniqueId) {
        ElectronicScaleMaintainDTO scaleMaintainDTO = new ElectronicScaleMaintainDTO();
        scaleMaintainDTO.setUniqueId(uniqueId);
        ElectronicScaleMaintain electronicScaleMaintain = service.add(scaleMaintainDTO);
        return RestResult.ok(electronicScaleMaintain.getSerialNum());
    }

    @PutMapping("/weight")
    @ApiOperation(value = "接收电子秤重量", notes = "接收电子秤重量")
    public RichResult weight(@RequestParam("serialNum") String serialNum, @RequestParam Double weight) throws Throwable {
        connectionHandler.sendWeight(serialNum,weight);
        return RestResult.ok();
    }
}
