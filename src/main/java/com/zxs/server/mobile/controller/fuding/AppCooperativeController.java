package com.zxs.server.mobile.controller.fuding;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeService;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaUtils;
import net.app315.hydra.intelligent.planting.utils.fuding.AreaVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeSearchModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_MOBILE_PATH;

/**
 * <p>
 * 联合体 前端控制器
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@RestController
@RequestMapping(VALID_MOBILE_PATH+ "/cooperative")
@Api(tags = "联合体管理")
public class AppCooperativeController {
    @Autowired
    private ICooperativeService iCooperativeService;
    @Autowired
    private AreaUtils areaUtil;

    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation("获取当前登录用户的联合体列表（分页）")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "cd8716732ef8476b9894dbe1ba2dd7b1", required = true, paramType = "header")
    public AbstractPageService.PageResults<List<CooperativeListVO>> pageList(CooperativeSearchModel model, String areaCode){
        if(!StringUtils.isEmpty(areaCode)){

            AreaVO areaVO = areaUtil.getAllAreaByCode(areaCode);
            if(areaVO !=null){
                model.setCity(areaVO.getCity());
                model.setCounty(areaVO.getCounty());
                model.setProvince(areaVO.getProvince());
                model.setTownShipCode(areaVO.getStreet());
            }
        }
        return iCooperativeService.getCooperativeList(model);
    }



}
