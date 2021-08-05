package com.zxs.server.mapper.gugeng.recruit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.recruit.SearchRecruitManageRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.recruit.ProductionManageRecruit;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.CREATED_BY;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.AUTH_DEPARTMENT_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.EMPLOYMENT_MANAGEMENT;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@Component
public interface ProductionManageRecruitMapper extends BaseMapper<ProductionManageRecruit> {

    /**
     * 获取用工信息列表
     *
     * @author shixiongfei
     * @date 2019-10-08
     * @updateDate 2019-10-08
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageRecruit> list(SearchRecruitManageRequestDTO requestDTO, String sysId, String organizationId, CommonUtil commonUtil) {

        Page<ProductionManageRecruit> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManageRecruit> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageRecruit.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageRecruit.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageRecruit.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey());
        String search = requestDTO.getSearch();
        // search为空则进行高级检索，不为空则进行普通检索
        if (StringUtils.isBlank(search)) {
            String[] applyInterval = LocalDateTimeUtil.substringDate(requestDTO.getApplicationDate());
            String[] arrivalInterval = LocalDateTimeUtil.substringDate(requestDTO.getArrivalDate());
            String[] executiveInterval = LocalDateTimeUtil.substringDate(requestDTO.getExecutiveHiringDate());
            String[] completedInterval = LocalDateTimeUtil.substringDate(requestDTO.getCompletedHiringDate());

            wrapper.eq(StringUtils.isNotBlank(requestDTO.getDepartmentName()), ProductionManageRecruit.COL_DEPARTMENT_NAME, requestDTO.getDepartmentName())
                    .eq(Objects.nonNull(requestDTO.getRecruitmentStatus()), ProductionManageRecruit.COL_RECRUITMENT_STATUS, requestDTO.getRecruitmentStatus())
                    .ge(StringUtils.isNotBlank(applyInterval[0]), ProductionManageRecruit.COL_APPLICATION_DATE, applyInterval[0])
                    .lt(StringUtils.isNotBlank(applyInterval[1]), ProductionManageRecruit.COL_APPLICATION_DATE, LocalDateTimeUtil.addOneDay(applyInterval[1]))
                    .ge(StringUtils.isNotBlank(arrivalInterval[0]), ProductionManageRecruit.COL_ARRIVAL_DATE, arrivalInterval[0])
                    .lt(StringUtils.isNotBlank(arrivalInterval[1]), ProductionManageRecruit.COL_ARRIVAL_DATE, LocalDateTimeUtil.addOneDay(arrivalInterval[1]))
                    .eq(StringUtils.isNotBlank(requestDTO.getExecutiveRecruiterName()), ProductionManageRecruit.COL_EXECUTIVE_RECRUITER_NAME, requestDTO.getExecutiveRecruiterName())
                    .ge(StringUtils.isNotBlank(executiveInterval[0]), ProductionManageRecruit.COL_EXECUTIVE_HIRING_DATE, executiveInterval[0])
                    .lt(StringUtils.isNotBlank(executiveInterval[1]), ProductionManageRecruit.COL_EXECUTIVE_HIRING_DATE, LocalDateTimeUtil.addOneDay(executiveInterval[1]))
                    .eq(StringUtils.isNotBlank(requestDTO.getCompletedRecruiterName()), ProductionManageRecruit.COL_COMPLETED_RECRUITER_NAME, requestDTO.getCompletedRecruiterName())
                    .ge(StringUtils.isNotBlank(completedInterval[0]), ProductionManageRecruit.COL_COMPLETED_HIRING_DATE, completedInterval[0])
                    .lt(StringUtils.isNotBlank(completedInterval[1]), ProductionManageRecruit.COL_COMPLETED_HIRING_DATE, LocalDateTimeUtil.addOneDay(completedInterval[1]));
        } else {
            wrapper.and(query -> query
                    .or().like(ProductionManageRecruit.COL_DEPARTMENT_NAME, search)
                    .or().like(ProductionManageRecruit.COL_EXECUTIVE_RECRUITER_NAME, search)
                    .or().like(ProductionManageRecruit.COL_COMPLETED_RECRUITER_NAME, search));
        }

        // 按创建时间降序
        wrapper.orderByDesc(ProductionManageRecruit.COL_CREATE_DATE);

        InterceptorUserRoleDataAuth roleDataAuth = commonUtil.getRoleFunAuthWithAuthCode(EMPLOYMENT_MANAGEMENT);
        commonUtil.setAuthFilter(wrapper, roleDataAuth, CREATED_BY, AUTH_DEPARTMENT_ID, ProductionManageRecruit.class);
        return selectPage(page, wrapper);
    }

}
