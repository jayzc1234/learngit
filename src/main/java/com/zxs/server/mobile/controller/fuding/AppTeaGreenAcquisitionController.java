package com.zxs.server.mobile.controller.fuding;


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

import java.math.BigDecimal;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 * 茶青收购记录 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+ "/tea-green-acquisition")
@Api(tags = "茶青收购")
public class AppTeaGreenAcquisitionController {
    @Autowired
    private ITeaGreenAcquisitionStatisticsService iTeaGreenAcquisitionStatisticsService;

    @Autowired
    private ITeaGreenAcquisitionService teaGreenAcquisitionService;

    @Autowired
    private ITeaGreenAcquisitionBaseService teaGreenAcquisitionBaseService;

    @PostMapping
    @ApiOperation("添加茶青收购记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionVO add(@RequestBody TeaGreenAcquisitionAddModel model){
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = CopyUtil.copy(model, new TeaGreenAcquisitionBO());
        return teaGreenAcquisitionBaseService.addAcquisition(teaGreenAcquisitionBO);
    }

    @PostMapping("/list")
    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("收购记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaGreenAcquisitionVO>> pageList(TeaGreenAcquisitionSearchModel model){
        return teaGreenAcquisitionService.getTeaGreenAcquisitionMobile(model);
    }

    @GetMapping
    @ApiOperation(value = "获取收购记录信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header") ,
            @ApiImplicitParam(name = "acquisitionId", value = "收购记录id", required = true)
    })
    public TeaGreenAcquisitionVO get(String acquisitionId){
        TeaGreenAcquisitionBO teaGreenAcquisitionBO = teaGreenAcquisitionService.getTeaGreenAcquisitionById(acquisitionId);
        TeaGreenAcquisitionVO teaGreenAcquisitionVO = new TeaGreenAcquisitionVO();
         CopyUtil.copy(teaGreenAcquisitionBO,teaGreenAcquisitionVO,(src, target) -> {
           /* if(StringUtils.isNotEmpty(src.getAcquisitionImgs())){
                target.setAcquisitionImgs(JSONObject.parseArray(src.getAcquisitionImgs(),String.class));
            }*/
        });
        teaGreenAcquisitionVO.setTotalAmount(teaGreenAcquisitionBO.getQuantity().multiply(teaGreenAcquisitionBO.getPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
        return teaGreenAcquisitionVO;
    }

    @PutMapping(value = "/disable")
    @ApiOperation("茶青收购记录作废")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void disable(@RequestBody AcquisitionStatusVO acquisitionStatusVO){
        teaGreenAcquisitionService.disable(acquisitionStatusVO);
    }

    @PutMapping(value = "/write-off")
    @ApiOperation("茶青收购记录核销")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void writeOff(@RequestBody AcquisitionStatusVO acquisitionStatusVO){
        teaGreenAcquisitionService.writeOff(acquisitionStatusVO);
    }

    @RequestMapping(value = "/statistics",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("茶青收购统计")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionHourDataVO statistics(TeaGreenAndHairyTeaStatisticsQuery model){
        return iTeaGreenAcquisitionStatisticsService.statisticsQuery(model);
    }

    @GetMapping("/real-time-statistics")
    @ApiOperation("茶青收购实时统计")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public TeaGreenAcquisitionRealTimeStatisticsVO realTimeStatistics(){
        return teaGreenAcquisitionService.getTeaGreenAcquisitionRealTimeStatistics();
    }
}
