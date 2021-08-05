package com.zxs.server.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * <p>
 * 茶农 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_PATH+"/tea-farmer")
@Api(tags = "茶农管理")
public class TeaFarmerController {

    @Autowired
    private ITeaFarmerService iTeaFarmerService;
    @Autowired
    private AreaUtils areaUtil;
    @Autowired
    private SuperCodeRequests superCodeRequests;

    @Value("${qiniu.upload.domain}")
    private String qiniuUploadDomain;

    @PostMapping
    @ApiOperation("添加茶农")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public String add(@RequestBody TeaFarmerAddModel model) {
        TeaFarmerBO teaFarmerBO = CopyUtil.copy(model, new TeaFarmerBO());
        return iTeaFarmerService.addTeaFarmer(teaFarmerBO);
    }


    @PostMapping("/batch")
    @ApiOperation("批量添加茶农")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void batchAdd(@RequestBody TeaFarmerBatchListModel model) {
        iTeaFarmerService.batchAddTeaFarmer(model);
    }

    @PutMapping
    @ApiOperation(value = "编辑茶农", response = RichResult.class)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void update(@RequestBody TeaFarmerUpdateModel model) {
        TeaFarmerBO teaFarmerBO = CopyUtil.copy(model, new TeaFarmerBO());
        iTeaFarmerService.updateTeaFarmer(teaFarmerBO);
    }

    @GetMapping
    @ApiOperation(value = "获取茶农信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header"),
            @ApiImplicitParam(name = "farmerId", value = "茶农id")
    })
    public TeaFarmerVO get(String farmerId) {
        TeaFarmerBO teaFarmerBO = iTeaFarmerService.getTeaFarmer(farmerId);
        return CopyUtil.copy(teaFarmerBO, new TeaFarmerVO());
    }


    @RequestMapping(value = "/list", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation("获取茶农列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<TeaFarmerListVO>> pageList(TeaFarmerSearchModel model, String areaCode) {
        AreaVO areaVO = areaUtil.getAllAreaByCode(areaCode);
        if(areaVO !=null){
            model.setCity(areaVO.getCity());
            model.setCounty(areaVO.getCounty());
            model.setProvince(areaVO.getProvince());
            model.setTownShipCode(areaVO.getStreet());
        }
        return iTeaFarmerService.getTeaFarmerList(model);
    }


    @RequestMapping(value = "/byCooperativeId", method = {RequestMethod.POST, RequestMethod.GET})
    @ApiOperation("根据合作社id获取茶农列表（分页）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header"),
            @ApiImplicitParam(name = "cooperativeId", value = "合作社id")
    })

    public AbstractPageService.PageResults<List<TeaFarmerListVO>> pageList(TeaFarmerSearchModel model) {
        return iTeaFarmerService.getTeaFarmerListByCooperativeId(model);
    }


    @PostMapping("/status")
    @ApiOperation("禁用/启用")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void updateStatus(@RequestBody TeaFarmerUpdateModel model) {
        iTeaFarmerService.updateTeaFarmerStatus(model.getFarmerId());
    }

    @PostMapping(value = "/export")
    @ApiOperation("导出茶农")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void export(TeaFarmerExportDTO daoSearch, HttpServletResponse response) throws ExcelException {
        iTeaFarmerService.export(daoSearch, response);
    }


    @PostMapping("/import")
    @ApiOperation("导入茶农")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public void importExcel(@RequestBody ImportVO importVO)  {

        try {
            if (StringUtils.isEmpty(importVO.getUniqueCode())) {
                throw new TeaException("上传文件失败");
            }
            byte[] resu = superCodeRequests.getAndGetResultBySpring(qiniuUploadDomain + importVO.getUniqueCode(), null, null, byte[].class, false);
            InputStream file = new ByteArrayInputStream(resu);
            iTeaFarmerService.importExcel(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new TeaException("文件导入失败" + e.getMessage());
        }


    }

}
