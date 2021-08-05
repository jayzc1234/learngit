package com.zxs.server.controller.gugeng.wechat;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.server.service.gugeng.wechat.ProductionManageWechatAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-18
 */
@RestController
@RequestMapping("/wechat/auth")
@Api(value = "微信公众平台", tags = "微信公众平台")
public class ProductionManageWechatAuthController {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private ProductionManageWechatAuthService service;

    @PostMapping("/save")
    @ApiOperation(value = "绑定微信openID", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
        ,@ApiImplicitParam(name = "openID", paramType = "query", defaultValue = "64b379cd47c843458378f479a115c322", value = "微信openID", required = true)})
    public RestResult save(String openID) throws SuperCodeException {
        // TODO obj类型自行修改
        service.add(openID);
        return RestResult.success();
    }

/*    @PostMapping("/update")
    @ApiOperation(value = "", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult update(@RequestBody Object obj) throws SuperCodeException {
        // TODO obj类型自行修改
        service.update(obj);
        return RestResult.success();
    }

    @GetMapping("/detail")
    @ApiOperation(value = "", notes = "")
    public RestResult getById(@RequestParam("id") Long id) throws SuperCodeException {
        service.getById(id);
        return null;
    }

    @GetMapping("/list")
    @ApiOperation(value = "", notes = "")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RestResult list(Object obj) throws SuperCodeException {
        // TODO obj类型自行修改
        service.list(obj);
        return null;
    }*/
}
