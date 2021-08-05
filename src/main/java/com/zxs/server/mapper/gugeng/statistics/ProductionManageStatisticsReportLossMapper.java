package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossPageRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.ReportLossTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsReportLoss;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-21
 */
public interface ProductionManageStatisticsReportLossMapper extends BaseMapper<ProductionManageStatisticsReportLoss> {

    /**
     * 获取库存报损数据统计折线图
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     */
    default List<ProductionManageStatisticsReportLoss> listStockLossLineChart(SearchStockLossPageRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsReportLoss> queryWrapper = getStockLossWrapper(requestDTO, sysId, organizationId);
        queryWrapper.select("DATE_FORMAT(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE + ", '%Y-%m-%d') AS reportLossDate", "SUM(IFNULL(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_WEIGHT + ", 0)) AS reportLossWeight")
                // 按照日期分组
                .groupBy(ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE);
        return selectList(queryWrapper);
    }

    /**
     * 获取库存报损sql包装器
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-22
     * @updateDate 2019-10-22
     * @updatedBy shixiongfei
     */
    default QueryWrapper<ProductionManageStatisticsReportLoss> getStockLossWrapper(SearchStockLossPageRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsReportLoss> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsReportLoss.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsReportLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(Objects.nonNull(requestDTO.getType()), ProductionManageStatisticsReportLoss.COL_SORTING_TYPE, requestDTO.getType())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE, requestDTO.getStartQueryDate())
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE, LocalDateTimeUtil.addOneDay(requestDTO.getEndQueryDate()))
                .eq(ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_TYPE, ReportLossTypeEnum.STOCK_LOSS.getKey())
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStatisticsReportLoss.COL_PRODUCT_ID, requestDTO.getProductId())
                .in(CollectionUtils.isNotEmpty(requestDTO.getPlantBatchIds()), ProductionManageStatisticsReportLoss.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchIds());
        return queryWrapper;
    }

    /**
     * 获取库存报损总重量
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-22
     * @updateDate 2019-10-22
     * @updatedBy shixiongfei
     */
    default BigDecimal getTotalStockLossWeight(SearchStockLossPageRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsReportLoss> queryWrapper = getStockLossWrapper(requestDTO, sysId, organizationId);
        queryWrapper.select("SUM(IFNULL(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_WEIGHT + ", 0)) AS reportLossWeight");

        ProductionManageStatisticsReportLoss reportLoss = selectOne(queryWrapper);
        return Objects.isNull(reportLoss) ? BigDecimal.ZERO : reportLoss.getReportLossWeight();
    }

    /**
     * 获取库存报损统计列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-22
     * @updateDate 2019-10-22
     * @updatedBy shixiongfei
     */
    default IPage<ProductionManageStatisticsReportLoss> listStockLossStatistics(SearchStockLossPageRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsReportLoss> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsReportLoss> queryWrapper = getStockLossWrapper(requestDTO, sysId, organizationId);
        queryWrapper.select(ProductionManageStatisticsReportLoss.COL_SORTING_TYPE + " as sortingType", ProductionManageStatisticsReportLoss.COL_PLANT_BATCH_ID + " as plantBatchId", ProductionManageStatisticsReportLoss.COL_PLANT_BATCH_NAME + " as plantBatchName",
                "SUM(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_WEIGHT + ") as reportLossWeight", ProductionManageStatisticsReportLoss.COL_PRODUCT_NAME + " as productName", ProductionManageStatisticsReportLoss.COL_PRODUCT_ID + " as productId")
                // 按照批次类型 + 批次id + 产品id做分组
                .groupBy(ProductionManageStatisticsReportLoss.COL_SORTING_TYPE, ProductionManageStatisticsReportLoss.COL_PLANT_BATCH_ID, ProductionManageStatisticsReportLoss.COL_PRODUCT_ID)
                .orderByDesc(ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE);
        return selectPage(page, queryWrapper);
    }
}