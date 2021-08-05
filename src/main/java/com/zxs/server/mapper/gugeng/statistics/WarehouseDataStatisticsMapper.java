package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchWarehouseDataStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 仓储数据统计mapper
 * @author shixiongfei
 * @date 2019-09-21
 * @since
 */
public interface WarehouseDataStatisticsMapper extends BaseMapper<ProductionManageStock> {


    /**
     * 获取库存总重量
     *
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     * @param requestDTO 请求dto
     * @param sysId 系统id
     * @param organizationId 组织id
     * @return
     */
    default BigDecimal getTotalWeight(SearchWarehouseDataStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        List<Integer> types = SortingInventoryTypeEnum.getType(requestDTO.getType());
        QueryWrapper<ProductionManageStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(" + ProductionManageStock.COL_WEIGHT + ") AS weight")
                .eq(StringUtils.isNotBlank(sysId), ProductionManageStock.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStock.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStock.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchId()), ProductionManageStock.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), ProductionManageStock.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                .eq(StringUtils.isNotBlank(requestDTO.getProductSpecCode()), ProductionManageStock.COL_PRODUCT_SPEC_CODE, requestDTO.getProductSpecCode())
                .eq(StringUtils.isNotBlank(requestDTO.getSortingSpecCode()), ProductionManageStock.COL_SORTING_SPEC_CODE, requestDTO.getSortingSpecCode())
                .in(CollectionUtils.isNotEmpty(types), ProductionManageStock.COL_CODE_INCLUDE_TYPE, types);
        ProductionManageStock stock = selectOne(queryWrapper);
        return Optional.ofNullable(stock).map(ProductionManageStock::getWeight).orElse(BigDecimal.ZERO);
    }
}