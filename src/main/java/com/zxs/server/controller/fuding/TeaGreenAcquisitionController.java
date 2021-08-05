package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaGreenAcquisitionBO;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionBaseService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionStatisticsService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.common.AcquisitionStatusVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 茶青收购记录 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+"/tea-green-acquisition")
@Api(tags = "茶青收购")
public class TeaGreenAcquisitionController {
    @Autowired
    private ITeaGreenAcquisitionService teaGreenAcquisitionService;
    @Autowired
    private ITeaGreenAcquisitionStatisticsService iTeaGreenAcquisitionStatisticsService;

    @Autowired
    private ITeaGreenAcquisitionBaseService teaGreenAcquisitionBaseService;

    @PostMapping("/list")
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("收购记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> pageList(TeaGreenAcquisitionSearchModel model){
        return teaGreenAcquisitionService.getTeaGreenAcquisition(model);
    }

    @GetMapping("/real-time-statistics")
    @ApiOperation("茶青收购实时统计")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionRealTimeStatisticsVO realTimeStatistics(){
        return teaGreenAcquisitionService.getTeaGreenAcquisitionRealTimeStatistics();
    }


    @RequestMapping(value = "/statistics",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("茶青收购统计")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionHourDataVO statistics(TeaGreenAndHairyTeaStatisticsQuery model){
        return iTeaGreenAcquisitionStatisticsService.statisticsQuery(model);
    }

    @PostMapping(value = "/export")
    @ApiOperation("导出茶青收购记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(TeaGreenAcquisitionExportModel model, HttpServletResponse response) throws Exception {
        teaGreenAcquisitionService.export(model, response);
    }

    @PostMapping(value = "/export/bareWeight")
    @ApiOperation("导出茶青皮重")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void exportBareWeight(TeaGreenAcquisitionExportDTO model, HttpServletResponse response) throws Exception {
        teaGreenAcquisitionService.exportBareWeight(model, response);
    }

    @PostMapping(value = "/export/quantity")
    @ApiOperation("导出茶青总重")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void exportQuantity(TeaGreenAcquisitionExportDTO model, HttpServletResponse response) throws Exception {
        teaGreenAcquisitionService.exportQuantity(model, response);
    }

    @PutMapping(value = "/write-off")
    @ApiOperation("茶青收购记录核销")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void writeOff(@RequestBody AcquisitionStatusVO acquisitionStatusVO) {
        teaGreenAcquisitionService.writeOff(acquisitionStatusVO);
    }

    @PutMapping(value = "/disable")
    @ApiOperation("茶青收购记录作废")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void disable(@RequestBody AcquisitionStatusVO acquisitionStatusVO){
        teaGreenAcquisitionService.disable(acquisitionStatusVO);
    }

    @PostMapping(value = "/batch")
    @ApiOperation("批量提交茶青收购记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionVO batchRecord(@RequestBody TeaGreenAcquisitionAddModel model){
        return teaGreenAcquisitionService.batchRecord(model);
    }

    @PostMapping
    @ApiOperation("添加茶青收购记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionVO add(@RequestBody TeaGreenAcquisitionAddModel model){
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = CopyUtil.copy(model, new TeaGreenAcquisitionBO());
        return teaGreenAcquisitionBaseService.addAcquisition(teaGreenAcquisitionBO);
    }

    @GetMapping("/acquisition")
    @ApiOperation("获取单据详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header"),
            @ApiImplicitParam(name = "acquisitionId", value = "交易编号", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true)
    })
    public TeaGreenAcquisitionVO getAcquisition(String acquisitionId){
        return teaGreenAcquisitionService.getAcquisition(acquisitionId);
    }

    @RequestMapping(value = "/bareWeight/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("皮重列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> bareWeightList(TeaGreenAcquisitionSearchModel model){
        return teaGreenAcquisitionService.getTeaGreenAcquisitionBareWeight(model);
    }

    @PostMapping("/save/bareWeight")
    @ApiOperation("去皮重")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionVO saveBareWeight(@RequestBody TeaGreenAcquisitionAddModel model){
        return teaGreenAcquisitionService.saveBareWeight(model);
    }
}
