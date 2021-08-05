package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddAnswer;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateAnswer;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestion;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestionAnswer;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductManageQuestionAnswerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductManageQuestionMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
 * @desc 描述
 * @auther zhuhaifan
 * @date 2019/5/31 14:47
 */

@Service
public class ProductManageQuestionAnswerService implements ProductManageConstant {


    @Resource
    ProductManageQuestionAnswerMapper answerMapper;

    @Resource
    ProductManageQuestionMapper questionMapper;


    @Autowired
    private CommonUtil commonUtil;
    /**
     * 新增答案
     *
     * @param addAnswer
     * @return
     * @throws SuperCodeException
     */
    @Transactional(rollbackFor = Exception.class)
    public ProductManageQuestionAnswer add(AddAnswer addAnswer) throws SuperCodeException {
        ProductManageQuestionAnswer answer = new ProductManageQuestionAnswer();
        BeanUtils.copyProperties(addAnswer, answer);
        Employee employee= commonUtil.getEmployee();
        answer.setAuthDepartmentId(employee.getDepartmentId());
        int insert = answerMapper.insert(answer);
        if (insert > 0) {
            ProductManageQuestion questionByQuestion = getQuestionByQuestionId(addAnswer.getQuestionId());
            if (questionByQuestion != null) {
                questionByQuestion.setStatus(1);
                int update = questionMapper.updateById(questionByQuestion);
                if (update > 0) {
                    return answer;
                }
            }
        }
        throw new SuperCodeException("插入失败");

    }


    /**
     * 删除答案
     *
     * @param answerId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteAnswer(String questionId, String answerId) throws SuperCodeException {
        int delete = answerMapper.delete(getAnswerWrapper(answerId));
        if (delete > 0) {
            //通过questionId找有多少答案
            List<ProductManageQuestionAnswer> list = getAnswerListByQuestionId(questionId);
            if (list.isEmpty()) { //该问题没有答案 将状态改成 未回答
                ProductManageQuestion questionByQuestion = getQuestionByQuestionId(questionId);
                questionByQuestion.setStatus(0);
                int update = questionMapper.updateById(questionByQuestion);
                if (update > 0) {
                    return update;
                }

            } else {
                return delete;
            }

        }

        throw new SuperCodeException("更新失败");
    }


    /**
     * 修改更新
     *
     * @param updateAnswer
     * @return
     * @throws SuperCodeException
     */
    public ProductManageQuestionAnswer updateAnswer(UpdateAnswer updateAnswer) throws SuperCodeException {
        ProductManageQuestionAnswer answer = getAnswerByAnswerId(updateAnswer.getAnswerId());
        if (answer != null) {
            BeanUtils.copyProperties(updateAnswer, answer);
            int update = answerMapper.updateById(answer);
            if (update > 0) {
                return answer;
            }
        }

        throw new SuperCodeException("更新失败");



    }

    /**
     * 通过  answerId 得到 答案
     *
     * @param answerId
     * @return
     */
    public ProductManageQuestionAnswer getAnswerByAnswerId(String answerId) {
        return answerMapper.selectOne(getAnswerWrapper(answerId));
    }


    /**
     * 查询
     * 通过questionId 找 答案List
     *
     * @param questionId
     * @return
     */
    public List<ProductManageQuestionAnswer> getAnswerListByQuestionId(String questionId) {
        QueryWrapper<ProductManageQuestionAnswer> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
        return answerMapper.selectList(queryWrapper);
    }


    /**
     * 查询
     * 通过questionId 找 问题对象
     *
     * @param questionId
     * @return
     */
    public ProductManageQuestion getQuestionByQuestionId(String questionId) {
        QueryWrapper<ProductManageQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
        return questionMapper.selectOne(queryWrapper);
    }

    /**
     * 通过answerId 获得 答案的 queryWrapper
     *
     * @param answerId
     * @return
     */
    public QueryWrapper<ProductManageQuestionAnswer> getAnswerWrapper(String answerId) {
        QueryWrapper<ProductManageQuestionAnswer> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(i -> i.eq(COLUMN_ANSWERID, answerId));
        return queryWrapper;
    }


//    /**
//     * 通过questionId 获得 答案的 queryWrapper
//     *
//     * @param questionId
//     * @return
//     */
//    private QueryWrapper<ProductManageQuestionAnswer> getAnswerQueryWrapper(String questionId) {
//        QueryWrapper<ProductManageQuestionAnswer> queryWrapper = new QueryWrapper<>();
//        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
//        return queryWrapper;
//    }
//
//    /**
//     * 通过questionId 获得 问题的 queryWrapper
//     *
//     * @param questionId
//     * @return
//     */
//    private QueryWrapper<ProductManageQuestion> getQuestionQueryWrapper(String questionId) {
//        QueryWrapper<ProductManageQuestion> queryWrapper = new QueryWrapper<>();
//        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
//        return queryWrapper;
//    }


}
