package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionYieldDataRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.YieldComparisonRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductionYieldData;
import org.apache.commons.lang.StringUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-16
 */
public interface ProductionManageStatisticsProductionYieldDataMapper extends BaseMapper<ProductionManageStatisticsProductionYieldData> {


    /** 魔法值最好单独提取在一个常量类中 */
    String STRIKE_THROUGH = "-";

    /**
     * 获取产品产量数据统计列表
     * 库中每一条都代表一个批次，所以不可做分组处理
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-16
     * @updateDate 2019-10-16
     * @updatedBy shixiongfei
     */
    default IPage<ProductionManageStatisticsProductionYieldData> list(ProductionYieldDataRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsProductionYieldData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsProductionYieldData> queryWrapper = getSqlWrapper(requestDTO, sysId, organizationId);
        queryWrapper.orderByDesc(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE);
        return selectPage(page, queryWrapper);
    }

    /**
     * 获取条件查询sql拼接包装器
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     */
    default QueryWrapper<ProductionManageStatisticsProductionYieldData> getSqlWrapper(ProductionYieldDataRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductionYieldData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsProductionYieldData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsProductionYieldData.COL_ORGANIZATION_ID, organizationId);

        // 获取年月
        String[] dateInterval = requestDTO.getQueryDate().split(STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageStatisticsProductionYieldData.COL_PRODUCT_NAME, requestDTO.getProductName())
                .ge(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE, startDate)
                .le(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE, endDate);

        return queryWrapper;
    }

    /**
     * 获取列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     */
    default List<ProductionManageStatisticsProductionYieldData> listByDateInterval(ProductionYieldDataRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductionYieldData> queryWrapper = getSqlWrapper(requestDTO, sysId, organizationId);
        // 按照采收时间升序
        queryWrapper.orderByAsc(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE);

        return selectList(queryWrapper);
    }

    /**
     * 获取产量对比数据统计列表
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsProductionYieldData> listYieldComparisonData(YieldComparisonRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsProductionYieldData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsProductionYieldData> wrapper = getYCDWrapper(requestDTO, sysId, organizationId);
        wrapper.orderByDesc(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE);
        return selectPage(page, wrapper);
    }

    /**
     * 获取产量对比sql包装器
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStatisticsProductionYieldData> getYCDWrapper(YieldComparisonRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductionYieldData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsProductionYieldData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsProductionYieldData.COL_ORGANIZATION_ID, organizationId);

        queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), ProductionManageStatisticsProductionYieldData.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                .eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManageStatisticsProductionYieldData.COL_PRODUCT_NAME, requestDTO.getProductName())
                .ge(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE, requestDTO.getStartQueryDate())
                .lt(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE, LocalDateTimeUtil.addOneDay(requestDTO.getEndQueryDate()));

        return queryWrapper;
    }

    /**
     * 获取产量对比柱状图信息
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsProductionYieldData> listYieldComparisonDataHistogram(YieldComparisonRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsProductionYieldData> wrapper = getYCDWrapper(requestDTO, sysId, organizationId);
        wrapper.orderByAsc(ProductionManageStatisticsProductionYieldData.COL_HARVEST_DATE);
        return selectList(wrapper);
    }
}