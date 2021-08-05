package com.zxs.server.controller.gugeng.producemanage;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.QuestionAndAnswerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchQuestionAndAnswer;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductManageQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * @desc 描述
 * @auther zhuhaifan
 * @date 2019/6/25 9:14
 */

@RestController
@RequestMapping(VALID_PATH+"/commonQuestion")
@Api(value = "常见问题Controller", tags = "生产技术指导/常见问题")
public class ProductManageCommonQuestionController extends CommonUtil {

    @Autowired
    private ProductManageQuestionService questionService;


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "常见问题列表",httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token",  required = true, paramType = "header")
    public JsonResult<AbstractPageService.PageResults<List<QuestionAndAnswerVo>>> getList(SearchQuestionAndAnswer daoSearch) throws SuperCodeException {
        return new JsonResult<AbstractPageService.PageResults<List<QuestionAndAnswerVo>>>(200, "获取成功",questionService.getList(daoSearch) );
    }

}
