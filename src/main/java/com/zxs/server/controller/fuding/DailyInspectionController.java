package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.DailyInspectionBO;
import net.app315.hydra.intelligent.planting.server.service.fuding.behavioral.IDailyInspectionService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.*;
import net.app315.nail.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 日常巡检 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+ "/daily-inspection")
@Api(tags = "日常巡检管理")
public class DailyInspectionController {

    @Autowired
    private IDailyInspectionService dailyInspectionService;



    @PostMapping
    @ApiOperation("添加巡检记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public String add(@RequestBody DailyInspectionAddModel model){
        DailyInspectionBO dailyInspectionBO = CopyUtil.copy(model, new DailyInspectionBO());
        return dailyInspectionService.addDailyInspection(dailyInspectionBO);
    }


    @PutMapping
    @ApiOperation("修改巡检记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void update(@RequestBody DailyInspectionUpdateModel model){
        DailyInspectionBO dailyInspectionBO = CopyUtil.copy(model, new DailyInspectionBO());
        dailyInspectionService.updateDailyInspection(dailyInspectionBO);
    }


    @DeleteMapping
    @ApiOperation("删除巡检记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header"),
            @ApiImplicitParam(name = "inspectionId" , value = "巡检记录id", required = true)
    })
     public void delete(String inspectionId){
         dailyInspectionService.deleteDailyInspection(inspectionId);
    }

    @GetMapping
    @ApiOperation("获取巡检记录详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header"),
            @ApiImplicitParam(name = "inspectionId" , value = "巡检记录id", required = true)
    })
    public DailyInspectionVO get(String inspectionId){
        return dailyInspectionService.getInspectionInfo(inspectionId);
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("日常巡检记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<DailyInspectionListVO>> pageList(DailyInspectionSearchModel model){
        return dailyInspectionService.getDailyInspectionList(model);
    }


    @PostMapping(value = "/export")
    @ApiOperation("导出日常巡检数据")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(DailyInspectionSearchModel model, HttpServletResponse response){
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + DateUtil.getCurrent(DateUtil.ALL_PATTERN) + ".xls");
        dailyInspectionService.export(model,response);
    }
}
