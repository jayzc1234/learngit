package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSDRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsShippingData;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
public interface ProductionManageStatisticsShippingDataMapper extends BaseMapper<ProductionManageStatisticsShippingData> {

    /**
     * 获取发货数据统计列表
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsShippingData> list(SearchSDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsShippingData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsShippingData> queryWrapper = getWrapper(requestDTO, sysId, organizationId);
        queryWrapper.orderByDesc(ProductionManageStatisticsShippingData.COL_SHIPPING_DATE);
        return selectPage(page, queryWrapper);
    }

    /**
     * 获取发货数据统计sql包装器
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStatisticsShippingData> getWrapper(SearchSDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsShippingData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsShippingData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsShippingData.COL_ORGANIZATION_ID, organizationId)
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsShippingData.COL_SHIPPING_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsShippingData.COL_SHIPPING_DATE, requestDTO.getEndQueryDate());
        return queryWrapper;
    }

    /**
     * 获取总数量
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default ProductionManageStatisticsShippingData getTotalNumber(SearchSDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsShippingData> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.select("SUM(IFNULL(" + ProductionManageStatisticsShippingData.COL_EXPRESS_DELIVERY_NUMBER + ", 0)) AS expressDeliveryNumber", "SUM(IFNULL(" + ProductionManageStatisticsShippingData.COL_DELIVERY_NUMBER + ", 0)) AS deliveryNumber",
                "SUM(IFNULL(" + ProductionManageStatisticsShippingData.COL_SELF_ACQUIRED_NUMBER + ", 0)) AS selfAcquiredNumber", "SUM(IFNULL(" + ProductionManageStatisticsShippingData.COL_WAIT_SHIP_NUMBER + ", 0)) AS waitShipNumber");
        ProductionManageStatisticsShippingData shippingData = selectOne(wrapper);
        if (Objects.isNull(shippingData)) {
            shippingData = new ProductionManageStatisticsShippingData();
            shippingData.setDeliveryNumber(0);
            shippingData.setExpressDeliveryNumber(0);
            shippingData.setSelfAcquiredNumber(0);
            shippingData.setWaitShipNumber(0);
        }

        return shippingData;
    }

    /**
     * 获取发货数据列表（不含分页）
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsShippingData> listLineChart(SearchSDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsShippingData> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.orderByAsc(ProductionManageStatisticsShippingData.COL_SHIPPING_DATE);
        return selectList(wrapper);
    }
}