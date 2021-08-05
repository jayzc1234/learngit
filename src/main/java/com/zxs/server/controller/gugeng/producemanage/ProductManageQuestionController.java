package com.zxs.server.controller.gugeng.producemanage;

import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.QuestionAndAnswerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddQuestion;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchQuestionAndAnswer;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateQuestion;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestion;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductManageQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

/**
 * @desc 描述
 * @auther zhuhaifan
 * @date 2019/5/31 14:45
 */

@RestController
@RequestMapping(VALID_PATH+"/question")
@Api(value = "咨询专家Controller", tags = "生产技术指导/咨询专家")
public class ProductManageQuestionController extends CommonUtil {

    @Autowired
    private ProductManageQuestionService questionService;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ApiOperation(value = "提问", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public JsonResult<ProductManageQuestion> add(@Valid @RequestBody AddQuestion addQuestion) throws SuperCodeException {
        return new JsonResult<>(200, "成功", questionService.addQuestion(addQuestion));
    }


    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "删除问题")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header"),
            @ApiImplicitParam(name = "questionId", value = "问题Id", required = true, paramType = "query")
    })
    public JsonResult<?> delete(@RequestParam(required = true) String questionId) throws SuperCodeException {

        int i = questionService.deleteQuestion(questionId);
        if (i > 0) {
            return new JsonResult<>(200, "成功", null);
        }
        throw new SuperCodeException("失败");
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ApiOperation(value = "修改问题", httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public JsonResult<ProductManageQuestion> update(@Valid @RequestBody UpdateQuestion updateQuestion) throws SuperCodeException {
        ProductManageQuestion question = questionService.getQuestionByQuestionId(updateQuestion.getQuestionId());
        if (question.getStatus() == 1) {
            throw new SuperCodeException("该问题已回答不可修改");
        }
        return new JsonResult<>(200, "成功", questionService.updateQuestion(updateQuestion));
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "咨询专家列表",httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token",  required = true, paramType = "header")
    public JsonResult<PageResults<List<QuestionAndAnswerVo>>> getList(SearchQuestionAndAnswer daoSearch) throws SuperCodeException {
        return new JsonResult<PageResults<List<QuestionAndAnswerVo>>>(200, "获取成功",questionService.getList(daoSearch) );

    }





}
