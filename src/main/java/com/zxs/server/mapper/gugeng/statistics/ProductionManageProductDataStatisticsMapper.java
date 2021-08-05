package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndBatchBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndGreenhouseBO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageProductDataStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 生产数据统计表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */

public interface ProductionManageProductDataStatisticsMapper extends BaseMapper<ProductionManageProductDataStatistics> {


    @Select("SELECT pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " AS plantBatchId, SUM(mo." + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + ") AS receivedOrderMoney " +
            "FROM t_production_manage_order mo " +
            "INNER JOIN t_production_manage_outbound_package_message pm ON pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + " = mo." + ProductionManageOrder.COL_ID +
            "INNER JOIN t_production_manage_sort_instorage si ON si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " ${ew.customSqlSegment}")
    IPage<OrderAndBatchBO> listTotalAmount(Page<OrderAndBatchBO> page, @Param(Constants.WRAPPER) Wrapper queryWrapper);


    /**
     * 通过当天时间获取订单完成数及包装信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     */
    @Select("SELECT " + ProductionManageOrder.COL_ID + ", " + ProductionManageOrder.COL_ORDER_MONEY + ", " +
            ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + " FROM t_production_manage_order " +
            "WHERE " + ProductionManageOrder.COL_ORDER_STATUS + " = #{orderStatus} " +
            "AND DATE_FORMAT(" + ProductionManageOrder.COL_DONE_DATE + ", '%Y-%m-%d') >= #{currentDate} AND currentDate < #{endDate}")
    @Results({
            @Result(property = "orderMoney", column = ProductionManageOrder.COL_ORDER_MONEY, javaType = BigDecimal.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "receivedOrderMoney", column = ProductionManageOrder.COL_RECEIVED_ORDER_MONEY, javaType = BigDecimal.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "messageBOs", javaType = List.class, column = ProductionManageOrder.COL_ID, many =
            @Many(select = "com.jgw.supercodeplatform.productmanagement.dao.storagemanage.ProductionManageOutboundPackageMessageMapper.listByOrderId"))
    })
    List<OrderAndGreenhouseBO> listByCurrentDate(@Param("currentDate") String currentDate, @Param("endDate") String endDate, @Param("orderStatus") Byte orderStatus);
}