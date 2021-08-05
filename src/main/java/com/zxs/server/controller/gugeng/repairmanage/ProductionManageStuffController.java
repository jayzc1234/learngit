package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.annotation.gugeng.NeedAdvancedSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffUpdateDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-29
 */
@RestController
@RequestMapping(VALID_PATH+"/stuff")
@Api(value = "维护材料", tags = "维护材料")
public class ProductionManageStuffController extends CommonUtil {

    @Autowired
    private ProductionManageStuffService service;

    @PostMapping("/save")
    @ApiOperation(value = "新增", notes = "新增材料")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@Valid  @RequestBody ProductionManageStuffDTO stuffDTO) throws SuperCodeException, ParseException {
        service.add(stuffDTO);
        return RestResult.success();
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新", notes = "更新")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@Valid @RequestBody ProductionManageStuffUpdateDTO stuffUpdateDTO) throws SuperCodeException {
        service.update(stuffUpdateDTO);
        return RestResult.success();
    }

    @PutMapping("/disable")
    @ApiOperation(value = "禁用")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult disable(@RequestParam("id") Long id) throws SuperCodeException {
        service.disable(id);
        return RestResult.success();
    }

    @RequestMapping(value = "/detail",method = {RequestMethod.PUT,RequestMethod.GET})
    @ApiOperation(value = "查看")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<ProductionManageStuffDetailVO> detail(@RequestParam("id") Long id) throws SuperCodeException {
        ProductionManageStuffDetailVO stuffDetailVO=service.detail(id);
        return RestResult.success(stuffDetailVO);
    }

    @PutMapping("/enable")
    @ApiOperation(value = "启用")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult enable(@RequestParam("id") Long id) throws SuperCodeException {
        service.enable(id);
        return RestResult.success();
    }

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffListVO>>> list(ProductionManageStuffListDTO stuffListDTO) throws SuperCodeException {
       return RestResult.success(CommonUtil.iPageToPageResults(service.pageList(stuffListDTO),null));
    }

    @GetMapping("/dropDown")
    @ApiOperation(value = "下拉")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffListVO>>> dropDown(ProductionManageStuffListDTO stuffListDTO) throws SuperCodeException {
        AbstractPageService.PageResults<List<ProductionManageStuffListVO>>pageResults =service.dropDown(stuffListDTO);
        return RestResult.success(pageResults);
    }

    /**
     * 信息导出
     */
    @NeedAdvancedSearch
    @PostMapping("/export")
    @ApiOperation(value = "导出excel", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public void export(ProductionManageStuffListDTO listDTO, HttpServletResponse response) throws Exception {
        service.exportExcelList(listDTO, getExportNumber(), "材料",ProductionManageStuffListVO.class, response);
    }
}
