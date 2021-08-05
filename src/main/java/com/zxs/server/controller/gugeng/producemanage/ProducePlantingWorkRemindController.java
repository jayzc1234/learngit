package com.zxs.server.controller.gugeng.producemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.PlantingWorkRemindSearchDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchResponseDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProducePlantingWorkRemind;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProducePlantingWorkRemindServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.MassifPrincipalResponseVO;
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
 * @since 2021-07-05
 */
@RestController
@RequestMapping(VALID_PATH + "/planting/work/remind")
@Api(value = "", tags = "农事操作提醒")
public class ProducePlantingWorkRemindController {

    @Autowired
    private ProducePlantingWorkRemindServiceImpl service;

    @GetMapping(value = "/detail")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    public RichResult getById(@RequestParam("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

    @PutMapping("/set-status")
    @ApiOperation(value = "执行")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult setStatus(Long id) throws SuperCodeException {
        service.setStatus(id);
        return RestResult.success();
    }

    @RequestMapping(value = "/list", method = { RequestMethod.GET, RequestMethod.POST })
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<ProducePlantingWorkRemind>>> list(PlantingWorkRemindSearchDTO dto) {
        return RestResult.ok(service.list(dto));
    }


    @GetMapping("/list-massif-principal")
    @ApiOperation(value = "获取地块负责人列表", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<MassifPrincipalResponseVO>>> listMassifPrincipal(String baseName){
        AbstractPageService.PageResults<List<MassifPrincipalResponseVO>> results=new AbstractPageService.PageResults<>(service.listMassifPrincipal(baseName), null);
        return RestResult.ok(results);
    }

    @GetMapping("/get-by-batch-id")
    @ApiOperation(value = "根据批次id获取农事操作提醒", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<ProducePlantingWorkRemind>> getByBatchId(String plantingBatchId) {
        return RestResult.ok(service.list(new PlantingWorkRemindSearchDTO().setPlantingBatchId(plantingBatchId)).getList());
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody PlantingBatchResponseDTO obj) {
        service.add(obj);
        return RestResult.ok();
    }
}
