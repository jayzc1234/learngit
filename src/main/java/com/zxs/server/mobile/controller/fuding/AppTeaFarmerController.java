package com.zxs.server.mobile.controller.fuding;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 * 茶农 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+"/tea-farmer")
@Api(tags = "茶农管理")
public class AppTeaFarmerController {

    @Autowired
    private ITeaFarmerService iTeaFarmerService;

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

}
