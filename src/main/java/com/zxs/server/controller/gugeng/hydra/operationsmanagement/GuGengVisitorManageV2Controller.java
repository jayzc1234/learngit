package com.zxs.server.controller.gugeng.hydra.operationsmanagement;


import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.hydra.operationsmanagement.GuGengVisitorManageNewDTO;
import net.app315.hydra.intelligent.planting.server.service.gugeng.hydra.operationsmanagement.GuGengVisitorManageServiceImpl;
import net.app315.hydra.intelligent.planting.vo.gugeng.hydra.operationsmanagement.GuGengVisitorManageNewDetailVO;
import net.app315.nail.common.result.RichResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-03
 */
@RestController
@RequestMapping(VALID_PATH+"/guGengVisitorManage/v2")
@Api(value = "来访人员管理相关接口V2版本", tags = "来访人员管理相关接口V2版本")
public class GuGengVisitorManageV2Controller extends CommonUtil {
        // 可在模版中添加相应的controller通用方法，编辑模版在resources/templates/controller.java.vm文件中

    @Autowired
    private GuGengVisitorManageServiceImpl service;


    /**
     * 最新版本新增方法
     * @param visitorManageDTO
     * @return
     * @throws SuperCodeException
     */
    @PostMapping("/save")
    @ApiOperation(value = "v3.2.2.3版本新增", notes = "v3.2.2.3版本新增")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult save(@RequestBody GuGengVisitorManageNewDTO visitorManageDTO) throws SuperCodeException {
        return service.addV2(visitorManageDTO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "v3.2.2.3版本编辑", notes = "v3.2.2.3版本编辑")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult updateV2(@RequestBody GuGengVisitorManageNewDTO visitorManageDTO) throws SuperCodeException {
        return service.updateV2(visitorManageDTO);
    }


    @GetMapping("/detail")
    @ApiOperation(value = "查看", notes = "查看")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public RichResult<GuGengVisitorManageNewDetailVO> getById(@RequestParam("id") Long id) throws SuperCodeException {
        return service.detail(id);
    }

}
