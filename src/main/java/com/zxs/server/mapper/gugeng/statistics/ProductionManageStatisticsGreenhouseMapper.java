package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchGreenhouseStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsGreenhouse;
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
 * @since 2019-10-20
 */
public interface ProductionManageStatisticsGreenhouseMapper extends BaseMapper<ProductionManageStatisticsGreenhouse> {


    /**
     * 获取区域数据统计列表（包含分页）
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param 
     * @return 
     */
    default IPage<ProductionManageStatisticsGreenhouse> listGreenhouse(SearchGreenhouseStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsGreenhouse> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsGreenhouse> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.orderByDesc(ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE);
        return selectPage(page, wrapper);
    }

    /**
     * 获取sql检索包装器
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param 
     * @return 
     */
    default QueryWrapper<ProductionManageStatisticsGreenhouse> getWrapper(SearchGreenhouseStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsGreenhouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsGreenhouse.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsGreenhouse.COL_ORGANIZATION_ID, organizationId)
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE, requestDTO.getStartQueryDate())
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE, LocalDateTimeUtil.addOneDay(requestDTO.getEndQueryDate()))
                .eq(StringUtils.isNotBlank(requestDTO.getPartitionName()), ProductionManageStatisticsGreenhouse.COL_PARTITION_NAME, requestDTO.getPartitionName())
                .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), ProductionManageStatisticsGreenhouse.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName());
        return queryWrapper;
    }

    /**
     * 获取区域数据统计列表
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsGreenhouse> list(SearchGreenhouseStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsGreenhouse> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.select("SUM(IFNULL(" + ProductionManageStatisticsGreenhouse.COL_HARVEST_WEIGHT + ", 0)) AS harvestWeight", "DATE_FORMAT(" + ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE + ", '%Y-%m-%d') AS harvestDate")
                .groupBy(ProductionManageStatisticsGreenhouse.COL_HARVEST_DATE);
        return selectList(wrapper);
    }

    /**
     * 获取采收总重量
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default BigDecimal getTotalHarvestWeight(SearchGreenhouseStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsGreenhouse> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.select("SUM(IFNULL(" + ProductionManageStatisticsGreenhouse.COL_HARVEST_WEIGHT + ", 0)) AS harvestWeight");
        ProductionManageStatisticsGreenhouse greenhouse = selectOne(wrapper);
        return Objects.isNull(greenhouse) ? BigDecimal.ZERO : greenhouse.getHarvestWeight();
    }
}