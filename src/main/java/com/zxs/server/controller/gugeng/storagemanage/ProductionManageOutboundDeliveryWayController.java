package com.zxs.server.controller.gugeng.storagemanage;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.model.storagemanage.OutboundDeliveryExpress;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddOutboundDeliveryRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryWayEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundDeliveryWayService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.ProductionManageOutboundDeliveryWayVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 * 出库发货方式表 前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@RestController
@RequestMapping(VALID_PATH+"/uutbound-deliver-way")
@Api(value = "出库发货管理", tags = "出库发货管理")
public class ProductionManageOutboundDeliveryWayController {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageOutboundDeliveryWayService service;

    /**
     * 保存出库发货信息
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    @PostMapping("/save")
    @ApiOperation(value = "保存出库发货信息", notes = "保存出库发货信息")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult save(@RequestBody @Validated AddOutboundDeliveryRequestDTO requestDTO) throws SuperCodeException {
        service.add(requestDTO);
        return RestResult.success();
    }


    /**
     * 获取出库发货信息
     * @param outboundId
     * @return
     * @throws SuperCodeException
     */
    @GetMapping("/detail")
    @ApiOperation(value = "获取发货信息详情", notes = "获取发货信息详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult<List<ProductionManageOutboundDeliveryWayVO>> getById(@RequestParam("outboundId") Long outboundId) throws SuperCodeException {
        if (Objects.isNull(outboundId) || outboundId < 1) {
            throw new SuperCodeException("发货信息主键id不合法");
        }
        //获取发货方式信息
        List<ProductionManageOutboundDeliveryWay> outboundDeliveryWays=service.getOutboundDeliveryWays(outboundId);
        List<ProductionManageOutboundDeliveryWayVO> outboundDeliveryWayVOList=new ArrayList<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(outboundDeliveryWays)){
            for (ProductionManageOutboundDeliveryWay outboundDeliveryWay:outboundDeliveryWays) {
                ProductionManageOutboundDeliveryWayVO outboundDeliveryWayVO=new ProductionManageOutboundDeliveryWayVO();
                BeanUtils.copyProperties(outboundDeliveryWay,outboundDeliveryWayVO);
                if (DeliveryWayEnum.EXPRESS.getKey()==outboundDeliveryWay.getDeliveryWay()){
                    List<OutboundDeliveryExpress> outboundDeliveryExpressList=service.getOutboundDeliveryExpressList(outboundDeliveryWay.getExpressNo(),outboundDeliveryWay.getExpressCo());
                    outboundDeliveryWayVO.setExpressList(outboundDeliveryExpressList);
                }
                outboundDeliveryWayVOList.add(outboundDeliveryWayVO);
            }
        }
        return RestResult.success(outboundDeliveryWayVOList);
    }
}