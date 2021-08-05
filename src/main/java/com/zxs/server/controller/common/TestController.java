package com.zxs.server.controller.common;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;
import net.app315.hydra.intelligent.planting.server.service.common.DictionaryService;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2020-09-04
 */
@RestController
@RequestMapping(VALID_PATH+"/test/dictionary")
@Api(value = "测试", tags = "测试")
public class TestController {

    @Autowired
    private DictionaryService service;

    /**
     * 现在的restful接口统一采用二级资源的形式
     */
    @GetMapping("/weight/unit")
    @ApiOperation(value = "获取企业重量单位", notes = "获取企业重量单位")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<List<Dictionary>> getById() {

        return RestResult.ok(service.listWeightUnit());
    }

}
