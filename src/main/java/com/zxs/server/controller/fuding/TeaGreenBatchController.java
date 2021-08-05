package com.zxs.server.controller.fuding;

import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.storehouse.AddSemiInStorageRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenBatch;
import net.app315.hydra.intelligent.planting.server.config.excel.EnableRateLimiter;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenBatchService;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaGreenBatchListModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenBatchProductLevelVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 *
 * 茶青批次 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+"/tea-green-batch")
@Api(tags =  "茶青批次")
@Slf4j
public class TeaGreenBatchController {
    @Autowired
    private ITeaGreenBatchService teaGreenBatchService;

    @Autowired
    private CommonUtil commonUtil;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("茶青批次列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaGreenBatch>> pageList(TeaGreenBatchListModel model){
        return teaGreenBatchService.pageList(model);
    }

    @RequestMapping(value = "/product-level/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("茶青批次产品等级列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaGreenBatchProductLevelVO>> productPageList(TeaGreenBatchListModel model){
        return teaGreenBatchService.productPageList(model);
    }
    /**
     * 导出excel
     */
    @EnableRateLimiter
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出茶青批次统计excel")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void exportProductSaleStatistics(DaoSearch model, HttpServletResponse response) throws Exception {
        try {
            teaGreenBatchService.export(model, response);
        }catch (Exception e){
            log.warn("导出茶青批次统计",e);
            CommonUtil.throwSupercodeException(500,"导出茶青批次统计");
        }
    }

    @RequestMapping(value = "/save",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("生成茶青批次")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult saveTeaBatch(String organizationId){
        teaGreenBatchService.saveTeaBatch(organizationId,commonUtil.getSuperToken());
        return new RichResult();
    }

    @RequestMapping(value = "/list/semi",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("茶青批次列表（半成品）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult<AbstractPageService.PageResults<List<AddSemiInStorageRequestDTO>>> listSemi(TeaGreenBatchListModel model){
        return new RichResult(teaGreenBatchService.listSemi(model));
    }

}
