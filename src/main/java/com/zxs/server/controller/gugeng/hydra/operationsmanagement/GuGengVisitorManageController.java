package com.zxs.server.controller.gugeng.hydra.operationsmanagement;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengVisitorManageDTO;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengVisitorManageListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengVisitorManage;
import net.app315.hydra.intelligent.planting.server.service.gugeng.hydra.operationsmanagement.GuGengVisitorManageServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-03
 */
@RestController
@RequestMapping(VALID_PATH+"/guGengVisitorManage")
@Api(value = "来访人员管理", tags = "来访人员管理")
public class GuGengVisitorManageController  extends CommonUtil {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private GuGengVisitorManageServiceImpl service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "新增")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody GuGengVisitorManageDTO visitorManageDTO) throws SuperCodeException {
        return service.add(visitorManageDTO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody GuGengVisitorManageDTO visitorManageDTO) throws SuperCodeException {
        return service.update(visitorManageDTO);
    }

    @DeleteMapping("/deleteById")
    @ApiOperation(value = "删除", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult deleteById(@RequestParam("id") Long id ) throws SuperCodeException {
        service.deleteById(id);
        return new RichResult();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "查看", notes = "查看")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<GuGengVisitorManageVO> getById(@RequestParam("id") Long id) throws SuperCodeException {
        RichResult<GuGengVisitorManageVO> richResult=new RichResult();
        GuGengVisitorManage visitorManage = service.getById(id);
        if (null==visitorManage){
            richResult.setState(500);
            richResult.setMsg("该人员信息不存在");
        }else {
            GuGengVisitorManageVO visitorManageVO=new GuGengVisitorManageVO();
            BeanUtils.copyProperties(visitorManage,visitorManageVO);
            visitorManageVO.setVisitDate(visitorManage.getVisitDate());
            richResult.setResults(visitorManageVO);
            richResult.setState(200);
        }
        return richResult;
    }

    @NeedAdvancedSearch
    @RequestMapping(value = "/list",method = {RequestMethod.GET, RequestMethod.POST})
    @ApiOperation(value = "列表", notes = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<GuGengVisitorManageListVO>>> list(GuGengVisitorManageListDTO visitorManageListDTO) throws SuperCodeException {
        RichResult<AbstractPageService.PageResults<List<GuGengVisitorManageListVO>>> richResult=new RichResult();
        richResult.setState(200);
        IPage<GuGengVisitorManageListVO> iPage=service.pageList(visitorManageListDTO);
        AbstractPageService.PageResults<List<GuGengVisitorManageListVO>> listPageResults = CommonUtil.iPageToPageResults(iPage, null);
        richResult.setResults(listPageResults);
        return richResult;
    }

    /**
     * 信息导出
     */
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(GuGengVisitorManageListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "来访人员管理",GuGengVisitorManageListVO.class, response);
    }
}
