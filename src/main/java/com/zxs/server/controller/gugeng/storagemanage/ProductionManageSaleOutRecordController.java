package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchSaleOutRecordRequestDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageSaleOutRecordService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSaleOutRecordResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-29
 */
@RestController
@RequestMapping(VALID_PATH+"/sale-out-record")
@Api(value = "销售客户信息管理", tags = "销售客户信息管理")
public class ProductionManageSaleOutRecordController {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageSaleOutRecordService service;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "销售客户列表", notes = "销售客户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    })
    public RestResult<AbstractPageService.PageResults<List<SearchSaleOutRecordResponseVO>>> list(SearchSaleOutRecordRequestDTO requestDTO) throws SuperCodeException {
        CustomAssert.isNotBlank(requestDTO.getPlantBatchId(), "种植批次id不可为空");
        return RestResult.success(service.list(requestDTO));
    }

}