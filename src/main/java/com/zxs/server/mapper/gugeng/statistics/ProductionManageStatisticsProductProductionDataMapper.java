package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductProductionData;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-29
 */
public interface ProductionManageStatisticsProductProductionDataMapper extends BaseMapper<ProductionManageStatisticsProductProductionData> {


    /**
     * 获取产品产量列表(含分页)
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsProductProductionData> list(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsProductProductionData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsProductProductionData> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.select("SUM(" + ProductionManageStatisticsProductProductionData.COL_INBOUND_WEIGHT + ") AS inboundWeight", "SUM(" + ProductionManageStatisticsProductProductionData.COL_NON_COMMODITY_PRODUCT_WEIGHT + ") AS nonCommodityProductWeight",
                ProductionManageStatisticsProductProductionData.COL_PRODUCT_ID + " AS productId", ProductionManageStatisticsProductProductionData.COL_PRODUCT_NAME + " AS productName", "SUM(" + ProductionManageStatisticsProductProductionData.COL_PACKING_BOX_NUM + ") AS packingBoxNum",
                ProductionManageStatisticsProductProductionData.COL_PRODUCT_LEVEL_CODE + " AS productLevelCode", ProductionManageStatisticsProductProductionData.COL_PRODUCT_LEVEL_NAME + " AS productLevelName");

        return selectPage(page, wrapper);
    }

    /**
     * 获取sql包装器
     *
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStatisticsProductProductionData> getWrapper(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductProductionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsProductProductionData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsProductProductionData.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStatisticsProductProductionData.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), ProductionManageStatisticsProductProductionData.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsProductProductionData.COL_HARVEST_DATE, requestDTO.getStartQueryDate())
                // 设置检索的结束时间 + 1
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsProductProductionData.COL_HARVEST_DATE, LocalDate.parse(requestDTO.getEndQueryDate()).plusDays(1).toString())
                .groupBy(ProductionManageStatisticsProductProductionData.COL_PRODUCT_LEVEL_CODE);

        return queryWrapper;
    }

    /**
     * 获取产品产量列表(不含分页),
     * 只可能查出一条数据
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsProductProductionData> histogram(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductProductionData> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.select("SUM(" + ProductionManageStatisticsProductProductionData.COL_INBOUND_WEIGHT + ") AS inboundWeight", "SUM(" + ProductionManageStatisticsProductProductionData.COL_NON_COMMODITY_PRODUCT_WEIGHT + ") AS nonCommodityProductWeight",
                ProductionManageStatisticsProductProductionData.COL_PRODUCT_ID + " AS productId", ProductionManageStatisticsProductProductionData.COL_PRODUCT_NAME + " AS productName");
        return selectList(wrapper);
    }
}