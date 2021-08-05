package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeBO;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeService;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaVO;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.*;
import net.app315.hydra.intelligent.planting.vo.fuding.common.ImportVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 联合体 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+ "/cooperative")
@Api(tags = "合作社管理")
public class CooperativeController {
    @Autowired
    private ICooperativeService iCooperativeService;
    @Autowired
    private AreaUtils areaUtil;

    @Autowired
    private SuperCodeRequests superCodeRequests;

    @Value("${qiniu.upload.domain}")
    private String qiniuUploadDomain;

    @PostMapping
    @ApiOperation("添加合作社")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public String add(@Valid @RequestBody CooperativeAddModel model){
        CooperativeBO cooperativeBO = CopyUtil.copy(model,new CooperativeBO());
        return iCooperativeService.addCooperative(cooperativeBO);
    }


    @PostMapping("/batch")
    @ApiOperation("批量添加合作社")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult batchAdd(@Valid @RequestBody CooperativeBatchListModel model){
         return iCooperativeService.batchAddCooperative(model);
    }

    @PutMapping
    @ApiOperation(value = "编辑合作社",response = RichResult.class)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void update(@RequestBody CooperativeUpdateModel model){
        CooperativeBO cooperativeBO = CopyUtil.copy(model,new CooperativeBO());
        iCooperativeService.updateCooperative(cooperativeBO);
    }

    @GetMapping
    @ApiOperation(value = "获取合作社信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header") ,
            @ApiImplicitParam(name = "cooperativeId", value = "合作社id", required = true)
    })
    public CooperativeVO get(String cooperativeId){
        CooperativeBO cooperativeBO = iCooperativeService.getCooperative(cooperativeId);
        return CopyUtil.copy(cooperativeBO,new CooperativeVO());
    }

    @GetMapping("/no")
    @ApiOperation(value = "获取自动生成合作社编号")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header") ,
    })
    public String getNo(){
        return iCooperativeService.getNo();
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("获取合作社列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<CooperativeListVO>> pageList(CooperativeSearchModel model, String areaCode){
        AreaVO areaVO = areaUtil.getAllAreaByCode(areaCode);
        if(areaVO !=null){
            model.setCity(areaVO.getCity());
            model.setCounty(areaVO.getCounty());
            model.setProvince(areaVO.getProvince());
            model.setTownShipCode(areaVO.getStreet());
        }
        return iCooperativeService.getCooperativeList(model);
    }

    @PostMapping("/status")
    @ApiOperation("禁用/启用")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void updateStatus( @RequestBody CooperativeUpdateModel model){
        iCooperativeService.updateCooperativeStatus(model.getCooperativeId());
    }

    @PostMapping(value = "/export")
    @ApiOperation("导出合作社")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(CooperativeExportDTO daoSearch,HttpServletResponse response) throws ExcelException {
        iCooperativeService.export(daoSearch,response);
    }


    @PostMapping("/import")
    @ApiOperation("导入合作社")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void importExcel(@RequestBody ImportVO importVO) {
        try {
            if (StringUtils.isEmpty(importVO.getUniqueCode())) {
                throw new TeaException("上传文件失败");
            }
            byte[] resu = superCodeRequests.getAndGetResultBySpring(qiniuUploadDomain + importVO.getUniqueCode(), null, null, byte[].class, false);
            InputStream  file = new ByteArrayInputStream(resu);
            iCooperativeService.importExcel(file);

        } catch (Exception e) {
            e.printStackTrace();
            throw new TeaException("文件导入失败:" + e.getMessage());
        }

    }

}
