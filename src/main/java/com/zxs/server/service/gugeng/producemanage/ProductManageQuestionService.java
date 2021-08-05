package com.zxs.server.service.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.AnswerVo;
import com.jgw.supercodeplatform.productmanagement.vo.QuestionAndAnswerVo;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ObjectConverter;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.AddQuestion;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchQuestionAndAnswer;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.UpdateQuestion;
import net.app315.hydra.intelligent.planting.enums.gugeng.SeasonStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestion;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductManageQuestionAnswer;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductManageQuestionAnswerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductManageQuestionMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * @desc 生产管理问题  服务
 * @auther zhuhaifan
 * @date 2019/5/31 14:48
 */

@Service
public class ProductManageQuestionService extends CommonUtil implements ProductManageConstant {

    @Resource
    ProductManageQuestionMapper questionMapper;

    @Resource
    private ProductManageQuestionAnswerMapper answerMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ObjectConverter<ProductManageQuestionAnswer, AnswerVo> objectConverter;

    /**
     * 新增问题提问
     *
     * @param addQuestion
     * @return
     * @throws SuperCodeException
     */
    public ProductManageQuestion addQuestion(AddQuestion addQuestion) throws SuperCodeException {
        ProductManageQuestion productManageQuestion = new ProductManageQuestion();
        BeanUtils.copyProperties(addQuestion, productManageQuestion);
        Integer productCycle = Integer.valueOf(addQuestion.getProductCycle());
        productManageQuestion.setProductCycle(Objects.requireNonNull(SeasonStatusEnum.getByType(productCycle)).getValue());
        Employee employee=commonUtil.getEmployee();
        productManageQuestion.setAuthDepartmentId(employee.getDepartmentId());
        productManageQuestion.setStatus(0);
        productManageQuestion.setDeleteStatus(0);
        int insert = questionMapper.insert(productManageQuestion);
        if (insert > 0) {
            return productManageQuestion;
        } else {
            throw new SuperCodeException("插入失败");
        }
    }


    /**
     * 删除
     *
     * @param questionId
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteQuestion(String questionId) {
        //答案表删除该问题的答案
        answerMapper.delete(new QueryWrapper<ProductManageQuestionAnswer>()
                .lambda()
                .eq(ProductManageQuestionAnswer::getQuestionId, questionId)
        );

        return questionMapper.delete(questionIdQueryWrapper(questionId));
    }

    /**
     * 修改
     *
     * @param
     * @return
     */
    public ProductManageQuestion updateQuestion(UpdateQuestion updateQuestion) throws SuperCodeException {
        ProductManageQuestion productManageQuestion = getQuestionByQuestionId(updateQuestion.getQuestionId());
        if (productManageQuestion != null) {
            BeanUtils.copyProperties(updateQuestion, productManageQuestion);
            int update = questionMapper.updateById(productManageQuestion);
            if (update > 0) {
                return productManageQuestion;
            } else {
                throw new SuperCodeException("更新失败");
            }
        } else {
            throw new SuperCodeException("更新失败");
        }


    }


    /**
     * 查询
     * 通过questionId 找 问题对象
     *
     * @param questionId
     * @return
     */
    public ProductManageQuestion getQuestionByQuestionId(String questionId) {
        return questionMapper.selectOne(questionIdQueryWrapper(questionId));
    }

    /**
     * 查询
     * 通过questionId 找 答案List
     *
     * @param questionId
     * @return
     */
    public List<ProductManageQuestionAnswer> getAnswerByQuestionId(String questionId) {

        return answerMapper.selectList(getAnswerQueryWrapper(questionId));
        // return answerMapper.selectOne(getAnswerQueryWrapper(questionId));
    }


    /**
     * 通过questionId 获得 问题的 queryWrapper
     *
     * @param questionId
     * @return
     */
    public QueryWrapper<ProductManageQuestion> questionIdQueryWrapper(String questionId) {
        QueryWrapper<ProductManageQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
        return queryWrapper;
    }

    /**
     * 通过questionId 获得 答案的 queryWrapper
     *
     * @param questionId
     * @return
     */
    public QueryWrapper<ProductManageQuestionAnswer> getAnswerQueryWrapper(String questionId) {
        QueryWrapper<ProductManageQuestionAnswer> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(i -> i.eq(COLUMN_QUESTIONID, questionId));
        return queryWrapper;
    }

    /**
     * 问题与答案的List
     *
     * @param daoSearch
     * @return
     * @throws SuperCodeException
     */
    public PageResults<List<QuestionAndAnswerVo>> getList(SearchQuestionAndAnswer daoSearch) throws SuperCodeException {
        //如果未设置当前页及页数 默认第一页 页数=5
        if (Objects.isNull(daoSearch.getCurrent()) || daoSearch.getCurrent() < 1) {
            daoSearch.setCurrent(1);
        }
        if (Objects.isNull(daoSearch.getPageSize()) || daoSearch.getPageSize() < 1) {
            daoSearch.setPageSize(5);
        }
        // 测试使用mybatisPlus分页插件
        Page<ProductManageQuestion> page = new Page<>(daoSearch.getCurrent(), daoSearch.getPageSize());
        QueryWrapper<ProductManageQuestion> queryWrapper = new QueryWrapper();

        // 获取系统id
        String sysId = commonUtil.getSysId();
        // 获取组织id
        String organizationId = getOrganizationId();

        // 查询指定的任务类型数据
        queryWrapper.eq(StringUtils.isNotBlank(organizationId), ProductManageQuestion.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductManageQuestion.COL_SYS_ID, sysId);

        // 判断搜索框是否为空,为空，则进行高级搜索，不为空，则普通搜索
        // 这里只有高级搜索
        if (StringUtils.isBlank(daoSearch.getSearch())) {
            queryWrapper.eq(StringUtils.isNotBlank(daoSearch.getProductId()), ProductManageQuestion.COL_PRODUCT_ID, daoSearch.getProductId())
                    .like(StringUtils.isNotBlank(daoSearch.getProductCycle()), ProductManageQuestion.COL_PRODUCT_CYCLE, daoSearch.getProductCycle())
                    .eq(StringUtils.isNotBlank(daoSearch.getStatus()), ProductManageQuestion.COL_STATUS, daoSearch.getStatus());
        }

        queryWrapper.orderByDesc(ProductManageQuestion.COL_CREATE_TIME);
        IPage<ProductManageQuestion> iPage = questionMapper.selectPage(page, queryWrapper);
        PageResults<List<QuestionAndAnswerVo>> listPageResults = setListPagination(iPage);
        if (CollectionUtils.isEmpty(iPage.getRecords())) {
            listPageResults.setList(Collections.EMPTY_LIST);
            return listPageResults;
        }

        //找问题的答案 并组装 QuestionAndAnswerVo
        List<QuestionAndAnswerVo> questionAndAnswerList = new ArrayList<>();
        for (ProductManageQuestion question : iPage.getRecords()) {
            QuestionAndAnswerVo questionAndAnswerVo = new QuestionAndAnswerVo();
            BeanUtils.copyProperties(question, questionAndAnswerVo);
            questionAndAnswerVo.setProductCycle(String.valueOf(question.getProductCycle()));
            List<ProductManageQuestionAnswer> answerList = getAnswerByQuestionId(question.getQuestionId());
            //转化为List<AnswerVo>
            List<AnswerVo> answerVoList = objectConverter.convert(answerList, AnswerVo.class);
            questionAndAnswerVo.setAnswerList(answerVoList == null ? new ArrayList<>() : answerVoList);
            questionAndAnswerList.add(questionAndAnswerVo);
        }
        listPageResults.setList(questionAndAnswerList);
        return listPageResults;
    }


    /**
     * 设置分页参数
     *
     * @param iPage
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> PageResults<List<T>> setListPagination(IPage iPage) throws SuperCodeException {
        PageResults<List<T>> pageResults = new PageResults();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResults.setPagination(pagination);
        return pageResults;
    }

}
