package com.zxs.server.mapper.gugeng.purchasing;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchPurchasingManagementRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchasingManagement;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PURCHASING_MANAGE;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.purchasing.PurchasingConstants.*;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-11
 */
public interface ProductionManagePurchasingManagementMapper extends BaseMapper<ProductionManagePurchasingManagement> {

    /**
     * 获取采购管理列表
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManagePurchasingManagement> list(SearchPurchasingManagementRequestDTO requestDTO, String sysId, String organizationId, CommonUtil commonUtil) {
        Page<ProductionManagePurchasingManagement> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManagePurchasingManagement> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ORGANIZATION_ID, organizationId)
                .eq(IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey());

        String search = requestDTO.getSearch();
        // search为空则进行全局检索, 否则普通检索
        if (StringUtils.isBlank(search)) {
            String[] applyInterval = LocalDateTimeUtil.substringDate(requestDTO.getApplicationDate());
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getPurchaseNumber()), PURCHASE_NUMBER, requestDTO.getPurchaseNumber())
                    .eq(StringUtils.isNotBlank(requestDTO.getApplicantName()), APPLICANT_NAME, requestDTO.getApplicantName())
                    .eq(StringUtils.isNotBlank(requestDTO.getApplicantDepartmentName()), APPLICANT_DEPARTMENT_NAME, requestDTO.getApplicantDepartmentName())
                    .eq(Objects.nonNull(requestDTO.getPurchaseStatus()), PURCHASE_STATUS, requestDTO.getPurchaseStatus())
                    .eq(StringUtils.isNotBlank(requestDTO.getPurchaseStaffName()), PURCHASE_STAFF_NAME, requestDTO.getPurchaseStaffName())
                    .ge(StringUtils.isNotBlank(applyInterval[0]), APPLICATION_DATE, applyInterval[0])
                    .lt(StringUtils.isNotBlank(applyInterval[1]), APPLICATION_DATE, LocalDateTimeUtil.addOneDay(applyInterval[1]))
                    .eq(StringUtils.isNotBlank(requestDTO.getPurchaseName()), PURCHASE_NAME, requestDTO.getPurchaseName());
        } else {
            queryWrapper.and(wrapper ->
                    wrapper.or().like(PURCHASE_NUMBER, search)
                    .or().like(APPLICANT_NAME, search)
                    .or().like(APPLICANT_DEPARTMENT_NAME, search)
                    .or().like(PURCHASE_STAFF_NAME, search))
                    .or().like(PURCHASE_NAME, search);
        }

        queryWrapper.orderByDesc(APPLICATION_DATE, CREATE_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(PURCHASING_MANAGE, queryWrapper, CREATED_BY, StringUtils.EMPTY);
        return selectPage(page, queryWrapper);
    }
}