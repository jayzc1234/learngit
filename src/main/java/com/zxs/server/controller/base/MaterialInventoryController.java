package com.zxs.server.controller.base;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialFieldListDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInventoryListDTO;
import net.app315.hydra.intelligent.planting.dto.base.SetMaterialWarningDTO;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInfoService;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInventoryService;
import net.app315.hydra.intelligent.planting.vo.base.*;
import net.app315.nail.common.result.RichResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 物料库存表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
@RestController
@RequestMapping(VALID_PATH+"/material/inventory")
@Api(value = "物料库存表", tags = "物料库存表")
public class MaterialInventoryController {

    @Autowired
    private MaterialInventoryService service;

    @Autowired
    private MaterialInfoService materialInfoService;

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping
    @ApiOperation(value = "获取物料库存详情", notes = "获取物料库存详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<PublicMaterialInfoListViewVO> getById(@ApiParam(value = "物料id", required = true)
                                  @RequestParam("publicMaterialId") String publicMaterialId, @ApiParam(value = "规格主键id", required = true) @RequestParam("materialSpecificationId") Long materialSpecificationId) {
        DaoSearch daoSearch = new DaoSearch();
        daoSearch.setFlag(1);
        IPage<PublicMaterialInfoListViewVO> publicMaterialInfoListViewVOIPage = service.listMaterialOutInfo(null, null, materialSpecificationId, publicMaterialId, daoSearch);
        List<PublicMaterialInfoListViewVO> records = publicMaterialInfoListViewVOIPage.getRecords();
        if (CollectionUtils.isEmpty(records)){
            CommonUtil.throwSuperCodeExtException(500,"不存在当前库存信息");
        }
        return RestResult.ok(records.get(0));
    }


    @GetMapping("/list")
    @ApiOperation(value = "获取物料库存列表", notes = "获取物料库存列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<MaterialInfoInventoryListVO>>> list(MaterialInfoInventoryListDTO materialInfoInventoryListDTO) {
        return CommonUtil.pageResult(service.pageList(materialInfoInventoryListDTO),null);
    }


    @ApiOperation("物料出入库流水")
    @GetMapping("/out-in/list")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<WarehouseOutAndInInfoListVO>>> warehouseOutAndInInfo(DaoSearch params) throws Exception {
        return CommonUtil.pageResult(service.warehouseOutAndInInfo(params),null);
    }

    @ApiOperation("批次详情--单个物料出入库流水")
    @GetMapping("/single/out-in/list")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<WarehouseMaterialInfoListVO>>> singleMaterialWarehouseOutAndInInfo(@ApiParam(value = "规格主键id", required = false) @RequestParam("materialSpecificationId") Long materialSpecificationId,
                                                                                                                              String publicMaterialId, DaoSearch daoSearch) throws Exception {
        return CommonUtil.pageResult(service.listSingleMaterialWarehouseInOutFlow(publicMaterialId,materialSpecificationId,daoSearch),null);

    }


    @ApiOperation("流水详情--单个物料出入库详情")
    @GetMapping("/single/out/list")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<SingleMaterialInOutWarehouseListVO>>> singleMaterialOutWarehouseInfo(@ApiParam(value = "物料id", required = true)
                                                                                                                       @RequestParam("publicMaterialId") String publicMaterialId,@ApiParam(value = "规格主键id", required = false) @RequestParam("materialSpecificationId") Long materialSpecificationId, DaoSearch params) throws Exception {
        return CommonUtil.pageResult(service.listSingleMaterialOutWarehouseInfo(publicMaterialId,materialSpecificationId,params),null);
    }


    @ApiOperation("设置预警值")
    @PutMapping("/warning")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult setWarning(@RequestBody SetMaterialWarningDTO params) throws Exception {
        service.setWarning(params);
        return RestResult.ok();
    }


    @ApiOperation("入库时，获取物料列表详情")
    @GetMapping("/in/material/list")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<MaterialInfoListVO>>> getMaterialInfo(@ApiParam(value = "物料名称") @RequestParam(value = "materialName", required = false) String materialName
                                                                                                         , @ApiParam(value = "物料类型") @RequestParam(value = "materialSortId", required = false) Long materialSortId
                                                                                                         , DaoSearch daoSearch) throws Exception {
        return CommonUtil.pageResult( materialInfoService.dropPage(materialSortId,materialName,daoSearch),null);
    }

    @ApiOperation("根据物料id获取该物料入库批次列表")
    @GetMapping("/in/batch")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<WarehouseMaterialInfoListVO>>> listBatchByMaterialId(@ApiParam(value = "物料id", required = true)
                                                                                                             @RequestParam("publicMaterialId") String publicMaterialId, @ApiParam(value = "规格主键id", required = false) @RequestParam("materialSpecificationId") Long materialSpecificationId, DaoSearch daoSearch) throws Exception {
        //TODO other.put("hasRemainder", true);
        return CommonUtil.pageResult(service.listBatchByMaterialId(null,publicMaterialId,materialSpecificationId,daoSearch),null);
    }

    @ApiOperation("出库时，获取物料列表详情")
    @GetMapping("/out/material/list")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RestResult<AbstractPageService.PageResults<List<PublicMaterialInfoListViewVO>>> listMaterialOutInfo(@ApiParam(value = "物料名称")@RequestParam(value = "materialName", required = false) String materialName ,
                                                                                                               @ApiParam(value = "物料类型")@RequestParam(value = "materialSortId", required = false) String materialSortId,
                                                                                                               @ApiParam(value = "物料主键id")@RequestParam(value = "materialSpecificationId", required = false) Long materialSpecificationId, DaoSearch daoSearch) throws Exception {
        return CommonUtil.pageResult(service.listMaterialOutInfo(materialName,materialSortId,materialSpecificationId,null,daoSearch),null);
    }


    @ApiOperation("校验物料是否符合出库条件")
    @GetMapping("/check/out")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult checkMaterialIsAdequacy(@ApiParam(value = "物料id", required = true)
                                              @RequestParam("publicMaterialId") String publicMaterialId
            , @ApiParam(value = "物料批次")@RequestParam(value = "materialBatch", required = false) String materialBatch
            , @ApiParam(value = "物料出库数", required = true) @RequestParam("outboundNum") BigDecimal outboundNum
            , @ApiParam(value = "物料规格主键id", required = true)@RequestParam("materialSpecificationId") Long materialSpecificationId) throws Exception {
        service.checkMaterialIsAdequacy(publicMaterialId, materialBatch, outboundNum, materialSpecificationId);
        return RestResult.ok();
    }

    @ApiOperation("获取出入库编号")
    @GetMapping("/code")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult getOutAndInCode() throws SuperCodeException {
        return RestResult.ok(service.getOutAndInCode());
    }

    @GetMapping("/enable/list/field")
    @ApiOperation(value = "根据字段父获取物料列表（包括搜索）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public JsonResult<AbstractPageService.PageResults<List<FieldObjectVO>>> enableMaterialsByField(MaterialFieldListDTO params) throws Exception {

        IPage<PublicMaterialInfoListViewVO> vs = service.enableMaterialsByField(params);
        AbstractPageService.PageResults pageResults = new AbstractPageService.PageResults();
        Page page = new Page((int)vs.getSize(),(int)vs.getCurrent(),(int)vs.getTotal());
        pageResults.setPagination(page);
        List<PublicMaterialInfoListViewVO> records = vs.getRecords();
        List<FieldObjectVO> ovs = records.stream().map(p -> new FieldObjectVO(p.getSameBatchMaterialId(), p.getMaterialName(), p)).collect(Collectors.toList());
        pageResults.setList(ovs);
        return new JsonResult<>(200, "获取成功", pageResults);
    }



}
