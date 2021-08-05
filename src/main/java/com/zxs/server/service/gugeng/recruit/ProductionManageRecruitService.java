package com.zxs.server.service.gugeng.recruit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.productmanagement.vo.recruit.SearchRecruitDetailResponseVO;
import com.jgw.supercodeplatform.productmanagement.vo.recruit.SearchRecruitManageResponseVO;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.BasePdfUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.RecruitPdfExportUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.logicdata.RecruitPdfExportLogicData;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.logicdata.RecruitPdfPageEventHelper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.recruit.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.GenderEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.recruit.RecruitmentStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.recruit.ProductionManageRecruit;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.recruit.ProductionManageRecruitMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  用工管理服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageRecruitService extends ServiceImpl<ProductionManageRecruitMapper, ProductionManageRecruit> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 新增用工申请
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddRecruitRequestDTO requestDTO) throws SuperCodeException {
        ProductionManageRecruit entity = new ProductionManageRecruit();
        BeanUtils.copyProperties(requestDTO, entity);
        CustomAssert.isGreaterThan0(baseMapper.insert(entity), "新增用工申请失败");
    }

    /**
     * 编辑用工申请
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateRecruitRequestDTO requestDTO) throws SuperCodeException {
        // 查询用工申请相关信息
        ProductionManageRecruit recruit = getNotDeletedById(requestDTO.getId());
        // 判断当前用工状态是否为待招聘，不是则报错
        boolean isTrue = RecruitmentStatusEnum.TO_BE_RECRUIT.getKey() == recruit.getRecruitmentStatus();
        CustomAssert.isSuccess(isTrue,"用工申请状态非待招聘状态，不可编辑");

        BeanUtils.copyProperties(requestDTO, recruit);
        CustomAssert.isGreaterThan0(baseMapper.updateById(recruit), "编辑用工申请失败");
    }

    /**
     * 获取用工管理列表
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<SearchRecruitManageResponseVO>> list(SearchRecruitManageRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageRecruit> iPage = baseMapper.list(requestDTO, sysId, organizationId, commonUtil);

        List<ProductionManageRecruit> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchRecruitManageResponseVO> list = records.stream().map(record -> {
            SearchRecruitManageResponseVO responseVO = new SearchRecruitManageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setRecruitmentStatus(record.getRecruitmentStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());

        Page pagination = new Page((int) iPage.getSize(), (int)iPage.getCurrent(), (int)iPage.getTotal());

        return new PageResults<>(list, pagination);
    }

    /**
     * 获取用工管理详情信息
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public SearchRecruitDetailResponseVO getDetail(Long id) throws SuperCodeException {
        ProductionManageRecruit recruit = getNotDeletedById(id);
        SearchRecruitDetailResponseVO responseVO = new SearchRecruitDetailResponseVO();
        BeanUtils.copyProperties(recruit, responseVO);
        responseVO.setGender(recruit.getGender().toString());
        responseVO.setRecruitmentStatus(recruit.getRecruitmentStatus().toString());
        return responseVO;
    }

    /**
     * 执行招聘
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param requestDTO 请求体对象
     * @return
     */
    public void executeRecruit(UpdateRecruitInExecuteRequestDTO requestDTO) throws SuperCodeException {
        ProductionManageRecruit recruit = getNotDeletedById(requestDTO.getId());
        // 检查当前状态是否为待招聘状态，不是则报错
        boolean isTrue = RecruitmentStatusEnum.TO_BE_RECRUIT.getKey() == recruit.getRecruitmentStatus();
        CustomAssert.isSuccess(isTrue, "当前用工状态非待招聘状态，无法执行招聘");

        Date date = requestDTO.getExecutiveHiringDate();
        // 更新招聘状态为招聘中
        boolean isSuccess = update()
                .set(ProductionManageRecruit.COL_RECRUITMENT_STATUS, RecruitmentStatusEnum.IN_RECRUITMENT.getKey())
                .set(ProductionManageRecruit.COL_EXECUTIVE_RECRUITER_ID, requestDTO.getExecutiveRecruiterId())
                .set(ProductionManageRecruit.COL_EXECUTIVE_RECRUITER_NAME, requestDTO.getExecutiveRecruiterName())
                .set(ProductionManageRecruit.COL_EXECUTIVE_HIRING_DATE, Objects.isNull(date) ? LocalDate.now() : date)
                .eq(ProductionManageRecruit.COL_ID, requestDTO.getId())
                .update();
        CustomAssert.isSuccess(isSuccess, "执行招聘失败");
    }

    /**
     * 完成招聘， 如果后续在此添加状态，
     * 可对状态的更新进行抽离整合，减少冗余代码
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void completedRecruit(UpdateRecruitInCompleteRequestDTO requestDTO) throws SuperCodeException {
        ProductionManageRecruit recruit = getNotDeletedById(requestDTO.getId());
        // 检查当前状态是否为招聘中状态，不是则报错
        boolean isTrue = RecruitmentStatusEnum.IN_RECRUITMENT.getKey() == recruit.getRecruitmentStatus();
        CustomAssert.isSuccess(isTrue, "当前用工状态非招聘中状态，无法完成招聘");
        // 更新招聘状态为招聘完成
        Date date = requestDTO.getCompletedHiringDate();

        boolean isSuccess = update()
                .set(ProductionManageRecruit.COL_RECRUITMENT_STATUS, RecruitmentStatusEnum.RECRUITMENT_TO_COMPLETE.getKey())
                .set(ProductionManageRecruit.COL_COMPLETED_RECRUITER_ID, requestDTO.getCompletedRecruiterId())
                .set(ProductionManageRecruit.COL_COMPLETED_RECRUITER_NAME, requestDTO.getCompletedRecruiterName())
                .set(ProductionManageRecruit.COL_COMPLETED_HIRING_DATE, Objects.isNull(date) ? LocalDate.now() : date)
                .set(ProductionManageRecruit.COL_COMPLETED_REMARK, requestDTO.getCompletedRemark())
                .eq(ProductionManageRecruit.COL_ID, requestDTO.getId())
                .update();

        CustomAssert.isSuccess(isSuccess, "完成招聘更新失败");
    }

    /**
     * 通过主键id来获取未删除的用工信息
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private ProductionManageRecruit getNotDeletedById(Long id) throws SuperCodeException {
        ProductionManageRecruit recruit = query().eq("id", id)
                .eq(ProductionManageRecruit.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey()).one();
        CustomAssert.isNull(recruit, "该用工申请信息不存在, 请检查");
        return recruit;
    }

    /**
     * 通过主键id来删除用工信息, 这里采用逻辑删除
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void remove(Long id) throws SuperCodeException {
        // 这里查询是为了保证数据库中数据存在且未被逻辑删除
        getNotDeletedById(id);
        boolean isSuccess = update()
                .set(ProductionManageRecruit.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManageRecruit.COL_ID, id)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除用工信息失败");
    }

    /**
     * 导出用工管理列表
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param requestDTO 请求对象
     * @param response 响应对象，通过流写出excel数据到客户端
     * @return
     */
    public void export(SearchRecruitManageRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();
        List<SearchRecruitManageResponseVO> list;
        // idList为空则全部导出，否则指定导出
        if (CollectionUtils.isNotEmpty(idList)) {
            list = excelByIds(idList);
        } else {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        }

        list.forEach(responseVO -> responseVO.setRecruitmentStatus(
                RecruitmentStatusEnum.getValue(Integer.valueOf(responseVO.getRecruitmentStatus()))));
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "用工管理列表", response);
    }

    /**
     * 通过id集合来获取用工管理列表
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param idList id集合
     * @return
     */
    private List<SearchRecruitManageResponseVO> excelByIds(List<String> idList) {
        Collection<ProductionManageRecruit> recruits = listByIds(idList);
        return recruits.stream().map(recruit -> {
            SearchRecruitManageResponseVO responseVO = new SearchRecruitManageResponseVO();
            BeanUtils.copyProperties(recruit, responseVO);
            responseVO.setRecruitmentStatus(recruit.getRecruitmentStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 导出用工管理pdf
     *
     * @author shixiongfei
     * @date 2019-10-15
     * @updateDate 2019-10-15
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportPdf(Long id, HttpServletResponse response) throws SuperCodeException {
        SearchRecruitDetailResponseVO responseVO = getDetail(id);
        RecruitPdfExportLogicData exportData = new RecruitPdfExportLogicData();
        BeanUtils.copyProperties(responseVO, exportData);
        if (StringUtils.isNotBlank(exportData.getGender())) {
            exportData.setGender(GenderEnum.getValue(Integer.valueOf(exportData.getGender())));
        }
        BasePdfUtils<RecruitPdfExportLogicData> exportUtil = new RecruitPdfExportUtil<>();
        exportUtil.exportPdf(exportData, new RecruitPdfPageEventHelper(), response, "用工管理表");
    }
}
