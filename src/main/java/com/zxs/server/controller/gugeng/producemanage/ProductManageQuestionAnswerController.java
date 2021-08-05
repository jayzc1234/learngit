package com.zxs.server.controller.gugeng.producemanage;

import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.QuestionAndAnswerVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddAnswer;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchQuestionAndAnswer;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateAnswer;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestionAnswer;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductManageQuestionAnswerService;
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
 * @date 2019/5/31 14:46
 */

@RestController
@RequestMapping(VALID_PATH+"/answer")
@Api(value = "远程指导Controller", tags = "生产技术指导/远程指导")
public class ProductManageQuestionAnswerController extends CommonUtil {

    @Autowired
    private ProductManageQuestionAnswerService answerService;

    @Autowired
    private ProductManageQuestionService questionService;



    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ApiOperation(value = "解答",httpMethod = "POST")
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public JsonResult<ProductManageQuestionAnswer> add(@Valid @RequestBody AddAnswer addAnswer) throws SuperCodeException {
        return new JsonResult<>(200, "成功", answerService.add(addAnswer));
    }


    @DeleteMapping(value = "/delete")
    @ApiOperation(value = "删除答案")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header"),
            @ApiImplicitParam(name = "questionId", value = "问题Id", required = true, paramType = "query"),
            @ApiImplicitParam(name = "answerId", value = "答案Id", required = true, paramType = "query")})
    public JsonResult<?> delete(@RequestParam(required = true) String questionId ,
                                @RequestParam(required = true) String answerId) throws SuperCodeException{
        int i = answerService.deleteAnswer(questionId,answerId);
        if (i>0){
            return new JsonResult<>(200, "成功", null);
        }
        throw new SuperCodeException("失败");
    }


    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ApiOperation(value ="修改答案" , httpMethod = "POST", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token", defaultValue = "26057cdcfd4440d087bf21bf79d91271", required = true, paramType = "header")
    public JsonResult<ProductManageQuestionAnswer> update(@Valid @RequestBody UpdateAnswer updateAnswer) throws SuperCodeException{
        return new JsonResult<>(200, "成功", answerService.updateAnswer(updateAnswer));
    }


    @RequestMapping(value = "/list",method = {RequestMethod.POST,RequestMethod.GET})
    @ApiOperation(value = "远程指导列表",httpMethod = "GET", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiImplicitParam(name = "super-token", value = "token",  required = true, paramType = "header")
    public JsonResult<AbstractPageService.PageResults<List<QuestionAndAnswerVo>>> getList(SearchQuestionAndAnswer daoSearch) throws SuperCodeException {
        return new JsonResult<AbstractPageService.PageResults<List<QuestionAndAnswerVo>>>(200, "获取成功",questionService.getList(daoSearch) );

    }



}
