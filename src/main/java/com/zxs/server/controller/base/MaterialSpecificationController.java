package com.zxs.server.controller.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialSpecificationService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * 物料规格
 * @author zc
 */
@RestController
@RequestMapping(VALID_PATH+"/material/specification")
@Api(value = "物料规格接口", tags = "物料规格接口")
public class MaterialSpecificationController {

    @Autowired
    private MaterialSpecificationService service;

    @GetMapping("/list")
    @ApiOperation(value = "根据物料查询规格")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public RichResult materials(String sameBatchMaterialId, DaoSearch params) throws Exception {
        IPage<MaterialSpecification> iPage = service.listPage(sameBatchMaterialId,params);
        return RestResult.ok(CommonUtil.iPageToPageResults(iPage,null));
    }

    @GetMapping("/enable/list/field")
    @ApiOperation(value = "查询物流规格字段")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public JsonResult<AbstractPageService.PageResults<List<Map<String, Object>>>> field(String sameBatchMaterialId, DaoSearch params) throws Exception {

        IPage<MaterialSpecification> iPage = service.listPage(sameBatchMaterialId,params);
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        Page page = new Page(params.getDefaultPageSize(),params.getDefaultCurrent(),(int)iPage.getTotal());
        AbstractPageService.PageResults<List<Map<String, Object>>> pageresultMap = new AbstractPageService.PageResults<List<Map<String, Object>>>(listMap, page);
        List<MaterialSpecification> list = iPage.getRecords();
        if (null != list && !list.isEmpty()) {
            for (MaterialSpecification baseProductionManageMaterialSpecification : list) {
                Map<String, Object> map = new LinkedHashMap<String, Object>();
                map.put("objectUniqueValue", baseProductionManageMaterialSpecification.getId());
                map.put("field", baseProductionManageMaterialSpecification.getSpecificationInfo());
                listMap.add(map);
            }
            pageresultMap.setList(listMap);
        }
        return new JsonResult<>(200, "获取成功", pageresultMap);
    }

}
