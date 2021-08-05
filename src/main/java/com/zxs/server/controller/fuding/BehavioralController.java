package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.BehavioralBO;
import net.app315.hydra.intelligent.planting.server.service.fuding.behavioral.IBehavioralService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralAddModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralUpdateModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralVO;
import net.app315.nail.common.result.RichResult;
import net.app315.nail.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 行为管理 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+"/behavioral")
@Api(tags = "行为管理")
public class BehavioralController {

    @Autowired
    private IBehavioralService behavioralService;

    @PostMapping
    @ApiOperation("添加行为记录")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public String add(@RequestBody BehavioralAddModel model){
        BehavioralBO behavioralBO = CopyUtil.copy(model, new BehavioralBO());
        return behavioralService.addBehavioral(behavioralBO);
    }

    @PutMapping
    @ApiOperation(value = "编辑行为记录",response = RichResult.class)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void update(@RequestBody BehavioralUpdateModel model){
        BehavioralBO behavioralBO = CopyUtil.copy(model, new BehavioralBO());
        behavioralService.updateBehavioral(behavioralBO);
    }

    @PutMapping("/delete")
    @ApiOperation(value = "删除行为记录",response = RichResult.class)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void delete(String behavioralId){
        behavioralService.deleteBehavioral(behavioralId);
    }

    @GetMapping
    @ApiOperation(value = "获取行为记录信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header") ,
            @ApiImplicitParam(name = "behavioralId", value = "行为记录id", required = true)
    })
    public BehavioralVO get(String behavioralId){
        BehavioralBO behavioralBO = behavioralService.getBehavioral(behavioralId);
        return CopyUtil.copy(behavioralBO,new BehavioralVO());
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("获取行为记录列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<BehavioralVO>> pageList(BehavioralSearchModel model){
        return behavioralService.getBehavioralList(model);
    }

    @PostMapping(value = "/export")
    @ApiOperation("导出行为数据")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(BehavioralSearchModel model, HttpServletResponse response){
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + DateUtil.getCurrent(DateUtil.ALL_PATTERN) + ".xls");
        behavioralService.export(model,response);
    }
}
