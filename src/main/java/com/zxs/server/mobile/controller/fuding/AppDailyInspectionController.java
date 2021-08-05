package com.zxs.server.mobile.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.DailyInspectionBO;
import net.app315.hydra.intelligent.planting.server.service.fuding.behavioral.IDailyInspectionService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionAddModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionSearchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 * 日常巡检 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+ "/daily-inspection")
@Api(tags = "日常巡检管理")
public class AppDailyInspectionController {

    @Autowired
    private IDailyInspectionService dailyInspectionService;

    @PostMapping
    @ApiOperation("添加巡检记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public String add(@RequestBody DailyInspectionAddModel model){
        DailyInspectionBO dailyInspectionBO = CopyUtil.copy(model, new DailyInspectionBO());
        return dailyInspectionService.addDailyInspection(dailyInspectionBO);
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("日常巡检记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<DailyInspectionListVO>> pageList(DailyInspectionSearchModel model){
        return dailyInspectionService.getDailyInspectionList(model);
    }

}
