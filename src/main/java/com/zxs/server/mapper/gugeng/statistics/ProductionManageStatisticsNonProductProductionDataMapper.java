package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchPPDRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsNonProductProductionData;
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
public interface ProductionManageStatisticsNonProductProductionDataMapper extends BaseMapper<ProductionManageStatisticsNonProductProductionData> {


    /**
     * 通过产品id获取入库重量和产品称重重量
     *
     * @author shixiongfei
     * @date 2019-10-30
     * @updateDate 2019-10-30
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default ProductionManageStatisticsNonProductProductionData getByProductId(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsNonProductProductionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(" + ProductionManageStatisticsNonProductProductionData.COL_YIELD + ") AS yield", "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_INBOUND_WEIGHT + ") AS inboundWeight")
                .eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsNonProductProductionData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsNonProductProductionData.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_ID, requestDTO.getProductId())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE, requestDTO.getStartQueryDate())
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE, LocalDate.parse(requestDTO.getEndQueryDate()).plusDays(1).toString());
        return selectOne(queryWrapper);
    }

    /**
     * 获取非产品等级产品产量数据列表
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsNonProductProductionData> histogram(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsNonProductProductionData> queryWrapper = getWrapper(requestDTO, sysId, organizationId);
        queryWrapper.select("SUM(" + ProductionManageStatisticsNonProductProductionData.COL_YIELD + ") AS yield", "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_NON_COMMODITY_PRODUCT_WEIGHT + ") AS nonCommodityProductWeight",
                "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_INBOUND_WEIGHT + ") AS inboundWeight", ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_ID + " AS productId", ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_NAME + " AS productName")
                // 按照采收时间升序
                .orderByAsc(ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE);
        return selectList(queryWrapper);
    }

    /**
     * 获取sql包装器
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStatisticsNonProductProductionData> getWrapper(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsNonProductProductionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsNonProductProductionData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsNonProductProductionData.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_ID, requestDTO.getProductId())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE, requestDTO.getStartQueryDate())
                // 结束时间需要进行+1操作
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE, LocalDate.parse(requestDTO.getEndQueryDate()).plusDays(1).toString())
                .groupBy(ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_ID);
        return queryWrapper;
    }

    /**
     * 获取非产品等级的产品产量列表(含分页)
     *
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsNonProductProductionData> list(SearchPPDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsNonProductProductionData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsNonProductProductionData> queryWrapper = getWrapper(requestDTO, sysId, organizationId);
        queryWrapper.select("SUM(" + ProductionManageStatisticsNonProductProductionData.COL_YIELD + ") AS yield", "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_NON_COMMODITY_PRODUCT_WEIGHT + ") AS nonCommodityProductWeight",
                "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_INBOUND_WEIGHT + ") AS inboundWeight", "SUM(" + ProductionManageStatisticsNonProductProductionData.COL_PACKING_BOX_NUM + ") AS packingBoxNum", ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_ID + " AS productId", ProductionManageStatisticsNonProductProductionData.COL_PRODUCT_NAME + " AS productName")
                .orderByDesc(ProductionManageStatisticsNonProductProductionData.COL_HARVEST_DATE);
        return selectPage(page, queryWrapper);
    }
}