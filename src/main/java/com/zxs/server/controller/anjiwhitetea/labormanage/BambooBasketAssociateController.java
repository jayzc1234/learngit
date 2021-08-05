package com.zxs.server.controller.anjiwhitetea.labormanage;


import com.jgw.supercodeplatform.common.AbstractPageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateListDTO;
import net.app315.hydra.intelligent.planting.enums.anjiwhitetea.SettlementTypeEnum;
import net.app315.hydra.intelligent.planting.server.service.anjiwhitetea.BambooBasketAssociateService;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.BambooBasketAssociateListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
@RestController
@RequestMapping(VALID_PATH+"/bamboo-basket-associate")
@Api(value = "竹筐关联", tags = "竹筐关联")
public class BambooBasketAssociateController {

    @Autowired
    private BambooBasketAssociateService service;

    @PutMapping
    @ApiOperation(value = "添加", notes = "添加")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody BambooBasketAssociateAddDTO basketAssociateAddDTO) {
        service.add(basketAssociateAddDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @PostMapping
    @ApiOperation(value = "编辑", notes = "编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult update(@RequestBody BambooBasketAssociateDTO associateDTO) {
        service.update(associateDTO);
        return RestResult.ok();
    }

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @DeleteMapping
    @ApiOperation(value = "解除关联（批量或单个）", notes = "解除关联（批量或单个）")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult remove(@RequestParam("ids") String ids) {
        String[] split = ids.split(",");
        service.removeByIds(new ArrayList<>(Arrays.asList(split)));
        return RestResult.ok();
    }


    @RequestMapping(value ="/list" , method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "获取列表", notes = "获取列表")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<BambooBasketAssociateListVO>>> list(BambooBasketAssociateListDTO associateListDTO) {
        return RestResult.ok(service.pageList(associateListDTO));
    }

    @GetMapping("/pull")
    @ApiOperation(value = "下拉", notes = "下拉")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<AbstractPageService.PageResults<List<BambooBasketAssociateListVO>>> pull(BambooBasketAssociateListDTO basketListDTO) {
        return RestResult.ok(service.pageList(basketListDTO));
    }

    @GetMapping("/getSettlementType")
    @ApiOperation(value = "获取茶叶等级", notes = "获取茶叶等级")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<NameValueVO>> getSettlementType() {
        SettlementTypeEnum[] values = SettlementTypeEnum.values();
        List<NameValueVO> list=new ArrayList<>();
        for (SettlementTypeEnum value : values) {
            NameValueVO<Integer,String> nameAndValue=new NameValueVO<>();
            nameAndValue.setName(value.getKey());
            nameAndValue.setValue(value.getDesc());
            list.add(nameAndValue);
        }
        return RestResult.ok(list);
    }

    @GetMapping("/detail")
    @ApiOperation(value = "获取详情", notes = "获取详情")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult getById(@RequestParam("id") Integer id) {
        return RestResult.ok(service.getById(id));
    }

}
