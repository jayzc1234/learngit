package com.zxs.server.controller.gugeng.repairmanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffSpecificationListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSpecification;
import net.app315.hydra.intelligent.planting.server.service.gugeng.repairmanage.ProductionManageStuffSpecificationService;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSpecificationListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
@RequestMapping(VALID_PATH+"/stuffSpecification")
@Api(value = "材料规格", tags = "材料规格")
public class ProductionManageStuffSpecificationController {

    @Autowired
    private ProductionManageStuffSpecificationService service;

    @GetMapping("/dropDown")
    @ApiOperation(value = "规格下拉")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<AbstractPageService.PageResults<List<ProductionManageStuffSpecificationListVO>>> listBySetSameBatchStuffId(ProductionManageStuffSpecificationListDTO specificationListDTO) {

        AbstractPageService.PageResults<List<ProductionManageStuffSpecificationListVO>>pageResults =service.dropDown(specificationListDTO);
        return RestResult.success(pageResults);
    }


    @GetMapping("/select/ids")
    @ApiOperation(value = "根据id查询")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionManageStuffSpecification>> selectByIds(@RequestParam("ids")ArrayList<Long> ids) {
        if (CollectionUtils.isEmpty(ids)){
            CommonUtil.throwSuperCodeExtException(500,"参数ids不可以为空");
        }
        return RestResult.success(service.selectByIds(ids));
    }
}
