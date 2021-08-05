package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.val;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlReqDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengProductLoss;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengProductLoss.*;


/**
 * 
 *
 * @author shixiongfei
 * @date 2020-02-25
 * @since 
 */
public interface GuGengProductLossMapper extends BaseMapper<GuGengProductLoss> {

    default IPage<GuGengProductLoss> list(SearchPlReqDTO request, String sysId, String organizationId) {
        Page<GuGengProductLoss> page = new Page<>(request.getDefaultCurrent(), request.getDefaultPageSize());
        QueryWrapper<GuGengProductLoss> wrapper = new QueryWrapper<>();
        wrapper.eq(COL_SYS_ID, sysId)
                .eq(COL_ORGANIZATION_ID, organizationId)
                .eq(COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey());
        val search = request.getSearch();
        if (StringUtils.isBlank(search)) {
            val lossInterval = LocalDateTimeUtil.substringDate(request.getReportLossDate());
            wrapper.eq(Objects.nonNull(request.getReportLossReason()), COL_REPORT_LOSS_REASON, request.getReportLossReason())
                    .eq(StringUtils.isNotBlank(request.getBatchName()), COL_BATCH_NAME, request.getBatchName())
                    .eq(StringUtils.isNotBlank(request.getProductName()), COL_PRODUCT_NAME, request.getProductName())
                    .eq(StringUtils.isNotBlank(request.getProductLevelName()), COL_PRODUCT_LEVEL_NAME, request.getProductLevelName())
                    .ge(StringUtils.isNotBlank(lossInterval[0]), COL_CREATE_TIME, lossInterval[0])
                    .le(StringUtils.isNotBlank(lossInterval[1]), COL_CREATE_TIME, lossInterval[1])
                    .eq(Objects.nonNull(request.getReportLossStatus()), COL_REPORT_LOSS_STATUS, request.getReportLossStatus())
                    .eq(StringUtils.isNotBlank(request.getCreateUserName()), COL_CREATE_USER_NAME, request.getCreateUserName());
        } else {
            wrapper.like(COL_PRODUCT_NAME, search);
        }

        wrapper.orderByDesc(COL_CREATE_TIME);
        return selectPage(page, wrapper);
    }

    default GuGengProductLoss getById(Long id, String sysId, String organizationId) {
        QueryWrapper<GuGengProductLoss> wrapper = new QueryWrapper<>();
        wrapper.eq(COL_SYS_ID, sysId)
                .eq(COL_ORGANIZATION_ID, organizationId)
                .eq(COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .eq(COL_ID, id);
        return selectOne(wrapper);
    }

    @Select("SELECT IFNULL(SUM(" + COL_REPORT_LOSS_WEIGHT + "), 0) FROM t_gu_geng_product_loss " +
            "WHERE " + COL_SYS_ID + " = #{sysId} AND " + COL_ORGANIZATION_ID + " = #{organizationId} AND " + COL_BATCH_ID + " = #{plantBatchId} " +
            "AND " + COL_REPORT_LOSS_STATUS + " = #{confirmStatus} AND " + COL_IS_DELETED + " = #{isDelete} ")
    BigDecimal getWeightByBatchId(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId,
                                  @Param("organizationId") String organizationId, @Param("confirmStatus") byte confirmStatus,
                                  @Param("isDelete") int isDelete);
}