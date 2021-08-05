package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageProductRecord;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductStockStatisticsResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-29
 */
public interface ProductionManageProductRecordMapper extends BaseMapper<ProductionManageProductRecord> {

    /**
     * 获取产品档案统计数据
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT IFNULL(SUM(" + ProductionManageProductRecord.COL_PRODUCE_TOTAL_WEIGHT + "), 0) AS produceTotalWeight, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_DAMAGE_TOTAL_WEIGHT + "), 0) AS damageTotalWeight, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_HARVEST_DAMAGE_WEIGHT + "), 0) AS harvestDamageWeight, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_SORT_DAMAGE_WEIGHT+ "), 0) AS sortDamageWeight, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_STORAGE_DAMAGE_WEIGHT + "), 0) AS storageDamageWeight, IFNULL(SUM(" + ProductionManageProductRecord.COL_SALE_TOTAL_AMOUNT + "), 0) AS saleTotalAmount, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_TOTAL_AMOUNT_RECEIVED + "), 0) AS totalAmountReceived, IFNULL(SUM(" + ProductionManageProductRecord.COL_TOTAL_COST_AMOUNT + "), 0) AS totalCostAmount, " +
            "IFNULL(SUM(" + ProductionManageProductRecord.COL_UNIT_COST_AMOUNT + "), 0) AS unitCostAmount FROM t_production_manage_product_record ${ew.customSqlSegment}")
    SearchProductStockStatisticsResponseVO getStatistics(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取产品档案统计数据
     *
     * @author shixiongfei
     * @date 2019-09-20
     * @updateDate 2019-09-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default SearchProductStockStatisticsResponseVO statistics(SearchProductRecordStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageProductRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge(ProductionManageProductRecord.COL_FIRST_SORT_DATE, requestDTO.getStartQueryDate())
                .le(ProductionManageProductRecord.COL_FIRST_SORT_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                .eq(StringUtils.isNotBlank(sysId), ProductionManageProductRecord.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProductRecord.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), ProductionManageProductRecord.COL_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), ProductionManageProductRecord.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                .eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageProductRecord.COL_PRODUCT_NAME, requestDTO.getProductName());
        return getStatistics(queryWrapper);
    }
}