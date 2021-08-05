package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.gugeng.*;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReturn;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackageMessageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackingMsgResponseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ID;


/**
 * <p>
 * 出库包装信息表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
public interface ProductionManageOutboundPackageMessageMapper extends CommonSql<ProductionManageOutboundPackageMessage> {

    @Select("SELECT pm." + ProductionManageOutboundPackageMessage.COL_ID + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_ID + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_SPEC_CODE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_SPEC_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_SPEC_CODE + ", "
            + "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_SPEC_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WAY_CODE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WAY_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_GG_PACKING_SERVINGS + " AS packingServings, " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_CREATE_USER_ID + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_CREATE_USER_NAME + ", mo." + ProductionManageOrder.COL_ORDER_TYPE +
            " FROM t_production_manage_outbound_package_message pm " +
            " LEFT JOIN t_production_manage_outbound o " +
            " ON o." + ProductionManageOutbound.COL_ID + " = pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID +
            " LEFT JOIN t_production_manage_order mo ON mo." + ProductionManageOrder.COL_ID + " = o." + ProductionManageOutbound.COL_ORDER_ID
            + " ${ew.customSqlSegment}")
    List<SearchPackageMessageResponseVO> list(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select("select opm.*, IFNULL(opm." + ProductionManageOutboundPackageMessage.COL_GG_PACKING_SERVINGS + ", 0) as packingServings from t_production_manage_outbound_package_message opm " +
            "where opm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=#{orderId} ")
    List<ProductionManageOutboundPackageMessage> selectByOrderId(@Param("orderId") Long orderId);


    @Select("SELECT o." + ProductionManageOrder.COL_ORDER_NO + ", o." + ProductionManageOrder.COL_ORDER_DATE + " AS orderDate, o." + ProductionManageOrder.COL_ORDER_TYPE + " AS orderType, o." + ProductionManageOrder.COL_CLIENT_ID + " AS clientId, op." + ProductionManageOrderProduct.COL_UNIT_PRICE + " AS unitPrice, opm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " AS plantBatchId, " +
            "opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_ID + " AS productId, opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE + " AS productLevelCode, opm." + ProductionManageOutboundPackageMessage.COL_GG_PACKING_SERVINGS + " AS packingServings, " +
            "opm." + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + " AS packingBoxNum, opm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + " AS packingWeight, opm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + " AS packingNum, o." + ProductionManageOrder.COL_CLIENT_CATEGORY_ID + " AS categoryId, o." + ProductionManageOrder.COL_CLIENT_CATEGORY_NAME + " AS categoryName, " +
            "o." + ProductionManageOrder.COL_CLIENT_NAME + " AS clientName, o." + ProductionManageOrder.COL_CLIENT_ADDRESS + " AS address FROM t_production_manage_outbound_package_message opm " +
            " INNER JOIN t_production_manage_order_product op ON op." + ProductionManageOrderProduct.COL_ORDER_ID + " = opm."+ProductionManageOutboundPackageMessage.COL_ORDER_ID+" AND op." + ProductionManageOrderProduct.COL_PRODUCT_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_ID + " AND op." + ProductionManageOrderProduct.COL_PRODUCT_LEVEL_CODE + " = opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE +
            " INNER JOIN t_production_manage_order o ON o." + ProductionManageOrder.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID +
            " INNER JOIN t_production_manage_client c ON c." + ProductionManageClient.COL_ID + " = o." + ProductionManageOrder.COL_CLIENT_ID +
            " WHERE opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID + " = #{outboundId}")
    List<OutboundBO> listByOutboundId(@Param("outboundId") Long outboundId);

    /**
     * 通过批次id, 产品等级code, 来获取订单状态为待发货状态的订单-包装信息总和
     *
     * @return
     * @since V1.1.1
     */
    @Select("SELECT SUM(pm." + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + ") AS totalPackingBoxNum, SUM(pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + ") AS totalPackingNum, SUM(pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + ") AS totalPackingWeight " +
            "FROM t_production_manage_outbound_package_message pm " +
            "WHERE pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM + " = 0 AND pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode}")
    OrderAndPackageMessageBO getUnDeliveryTotalByBatchIdAndLevelCode(@Param("plantBatchId") String plantBatchId, @Param("productLevelCode") String productLevelCode);

    /**
     * 通过订单id，订单状态为已完成的包装信息列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     */
    @Select("SELECT opm." + ProductionManageOutboundPackageMessage.COL_GREENHOUSE_ID + " AS greenhouseId, " +
            "opm." + ProductionManageOutboundPackageMessage.COL_GREENHOUSE_NAME + " AS greenhouseName, " +
            "opm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + " AS packingNum, opm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + " AS packingWeight " +
            " FROM t_production_manage_outbound_package_message opm " +
            " INNER JOIN t_production_manage_order o ON o." + ProductionManageOrder.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID +
            " WHERE opm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + " = #{orderId} ")
    @Results({
            @Result(property = "greenhouseId", column = ProductionManageOutboundPackageMessage.COL_GREENHOUSE_ID, javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "greenhouseName", column = ProductionManageOutboundPackageMessage.COL_GREENHOUSE_NAME, javaType = String.class, jdbcType = JdbcType.VARCHAR),
            @Result(property = "packingNum", column = ProductionManageOutboundPackageMessage.COL_PACKING_NUM, javaType = BigDecimal.class, jdbcType = JdbcType.DECIMAL),
            @Result(property = "packingWeight", column = ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT, javaType = BigDecimal.class, jdbcType = JdbcType.DECIMAL)
    })
    List<PackageMessageBO> listByOrderId(@Param("orderId") Long orderId);

    @Select("select count(DISTINCT o." + ProductionManageOrder.COL_ID + ") from t_production_manage_outbound_package_message pm left join t_production_manage_order o on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o." + ProductionManageOrder.COL_ID + " where " + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + "=#{plantBatchId} ")
    Integer statiticsBatchOrderNumByBatchId(@Param("plantBatchId") String plantBatchId);

    @Select("SELECT COUNT(DISTINCT pr." + ProductionManageOrderProductReturn.COL_ORDER_ID + ") returnOrderNum,IFNULL(truncate(SUM(pr." + ProductionManageOrderProductReturn.COL_RETURN_WEIGHT + "),2),0) returnWeight," +
            "IFNULL(SUM(pr." + ProductionManageOrderProductReturn.COL_RETURN_BOX_QUANTITY + "),0) returnBoxQuantity,IFNULL(SUM(pr." + ProductionManageOrderProductReturn.COL_RETURN_QUANTITY + "),0) returnQuantity from t_production_manage_outbound_package_message pm LEFT JOIN t_production_manage_order_product_return pr  on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=pr." + ProductionManageOrderProductReturn.COL_ORDER_ID +
            " where pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + "=#{plantBatchId} ")
    Map<String, Object> statiticsBatchReturnInfoByBatchId(@Param("plantBatchId") String plantBatchId);


    @Select("SELECT COUNT(DISTINCT o." + ProductionManageOrder.COL_ID + ")  from t_production_manage_outbound_package_message pm LEFT JOIN t_production_manage_order o  on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o." + ProductionManageOrder.COL_ID
            + " where pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + "=#{plantBatchId} and o." + ProductionManageOrder.COL_REJECT_STATUS + "=1")
    Integer statiticsBatchRejectOrderNumByBatchId(@Param("plantBatchId") String plantBatchId);

    @Select("SELECT IFNULL(truncate(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 1 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + " ELSE 0 END),2),0) packingWeight ,IFNULL(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 2 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + " ELSE 0 END),0) packingNum from t_production_manage_outbound_package_message pm " +
            "LEFT JOIN t_production_manage_order o on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o." + ProductionManageOrder.COL_ID
            + " where pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + "=#{plantBatchId} AND o." + ProductionManageOrder.COL_ID + "=#{orderId} ")
    Map<String, Object> statiticsBatchPcakWeightAndNumByBatchId(@Param("plantBatchId") String plantBatchId, @Param("orderId") Long orderId);

    @Select("SELECT DISTINCT o." + ProductionManageOrder.COL_ID + "  from t_production_manage_outbound_package_message pm LEFT JOIN t_production_manage_order o  on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o." + ProductionManageOrder.COL_ID
            + " where pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + "=#{plantBatchId} ")
    List<Long> selectOrderIdsByBatchId(@Param("plantBatchId") String plantBatchId);


    @Select(START_SCRIPT
            + "SELECT IFNULL(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 1 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + " ELSE 0 END),0)packingWeight ,IFNULL(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 2 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + " ELSE 0 END),0) packingNum, "
            + " IFNULL(truncate(SUM(o." + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + "),2),0) receivedOrderMoney,IFNULL(truncate(SUM(o." + ProductionManageOrder.COL_ORDER_MONEY + "),2),0) orderMoney"
            + " from t_production_manage_outbound_package_message pm LEFT JOIN t_production_manage_order o on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o." + ProductionManageOrder.COL_ID
            + START_WHERE
            + " o." + ProductionManageOrder.COL_ID + " in "
            + "<foreach collection='list' item='item' separator=',' open='('  close=')'>"
            + "#{item}"
            + "</foreach>"
            + END_WHERE
            + END_SCRIPT
    )
    Map<String, Object> statiticsBatchPcakWeightAndNumByOrderIds(List<Long> orderIds);

    /**
     * 获取包装出库总重量
     *
     * @param queryWrapper sql包装器
     * @return 出库总重量
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     */
    @Select("SELECT IFNULL(SUM(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + "), 0) AS weight FROM t_production_manage_outbound_package_message opm INNER JOIN " +
            " t_production_manage_outbound mo ON mo." + ProductionManageOutbound.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID + " ${ew.customSqlSegment}")
    @Results(
            @Result(column = "weight", property = "weight", javaType = BigDecimal.class, jdbcType = JdbcType.DECIMAL)
    )
    BigDecimal getOutboundWeight(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取包装出库总重量
     *
     * @param queryWrapper sql包装器
     * @return 出库总重量
     * @author zc
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     */
    @Select("SELECT IFNULL(SUM(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + "), 0) AS "+ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT +
            ",date_format("+ProductionManageOutboundPackageMessage.COL_PACKING_DATE+",'%Y-%m')"+
            " FROM t_production_manage_outbound_package_message opm INNER JOIN " +
            " t_production_manage_outbound mo ON mo." + ProductionManageOutbound.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID + " ${ew.customSqlSegment}")
    List<ProductionManageOutboundPackageMessage> getOutboundWeightGroupDate(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select(START_SCRIPT
            + "SELECT IFNULL(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 1 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + " ELSE 0 END),0) packingWeight ,IFNULL(SUM(CASE o." + ProductionManageOrder.COL_ORDER_TYPE + " WHEN 2 then pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + " ELSE 0 END),0) packingNum, "
            + " IFNULL(truncate(SUM(o." + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + "),2),0) receivedOrderMoney,IFNULL(truncate(SUM(o." + ProductionManageOrder.COL_ORDER_MONEY + "),2),0) orderMoney"
            + " from t_production_manage_outbound_package_message pm LEFT JOIN t_production_manage_order                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 " +
            "o on pm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + "=o.Id"
            + START_WHERE
            + " o." + ProductionManageOrder.COL_ID + " =#{orderId} "
            + END_WHERE
            + END_SCRIPT
    )
    Map<String, Object> statiticsBatchPcakWeightAndNumByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT IFNULL(MAX(" + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM + "), 0) FROM t_production_manage_outbound_package_message opm " +
            " INNER JOIN t_production_manage_outbound o ON o." + ProductionManageOutbound.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID +
            " WHERE opm." + ProductionManageOutboundPackageMessage.COL_ORDER_ID + " = #{orderId} AND o." + ProductionManageOutbound.COL_SYS_ID + " = #{sysId} AND o." + ProductionManageOutbound.COL_ORGANIZATION_ID + " = #{organizationId}")
    Integer getMaxOutboundTime(@Param("orderId") Long orderId, @Param("sysId") String sysId, @Param("organizationId") String organizationId);

    @Select("select pm." + ProductionManageOutboundPackageMessage.COL_ID + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + ", pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_ID + ", pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE + ", pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_SPEC_CODE + ", pm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_SPEC_NAME + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_SPEC_CODE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_SPEC_NAME + ", pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WAY_CODE + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WAY_NAME + ", pm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + ", pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + ", " +
            "pm." + ProductionManageOutboundPackageMessage.COL_GG_PACKING_SERVINGS + " AS packingServings, " +
            "pm." + ProductionManageOutboundPackageMessage.COL_GG_IS_SYNC_INBOUND + " AS addOrNotSyncInbound, pm." + ProductionManageOutboundPackageMessage.COL_GG_PLANT_BATCH_TYPE + " AS plantBatchType " +
            "from t_production_manage_outbound_package_message pm " +
            "inner join t_production_manage_outbound o " +
            "on o." + ProductionManageOutbound.COL_ID + " = pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID + " ${ew.customSqlSegment}")
    List<SearchPackingMsgResponseVO> listNotOutboundMsg(@Param(Constants.WRAPPER) Wrapper queryWrapper);


    /**
     * 获取包装信息数值总和数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     */
    @Select("select IFNULL(SUM(" + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + "), 0) AS totalBoxNum, " +
            "IFNULL(SUM(" + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + "), 0) AS totalQuantity, " +
            "IFNULL(SUM(" + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + "), 0) AS totalWeight, " +
            "IFNULL(SUM(" + ProductionManageOutboundPackageMessage.COL_GG_PACKING_SERVINGS + "), 0) AS totalServings " +
            "FROM t_production_manage_outbound_package_message ${ew.customSqlSegment}")
    OutboundTotalNumberBO getTotalNumberByIds(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取指定包装id集合的总数值信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default OutboundTotalNumberBO getTotalNumberByIds(List<Long> ids, Long outboundId) {
        QueryWrapper<ProductionManageOutboundPackageMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.in(OLD_ID, ids)
                .eq(ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID, outboundId);
        return getTotalNumberByIds(queryWrapper);
    }

    @Select("SELECT IFNULL(SUM(pm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + "), 0) FROM t_production_manage_outbound_package_message pm " +
            " INNER JOIN t_production_manage_outbound o ON o." + ProductionManageOutbound.COL_ID + " = pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID +
            " WHERE pm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND o." + ProductionManageOutbound.COL_SYS_ID + " = #{sysId} " +
            "AND o." + ProductionManageOutbound.COL_ORGANIZATION_ID + " = #{organizationId}")
    BigDecimal getWeightByBatchId(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId, @Param("organizationId") String organizationId);

    @Select("SELECT COUNT(*) FROM t_production_manage_outbound_package_message WHERE " + ProductionManageOutboundPackageMessage.COL_ORDER_ID + " = #{orderId}")
    Integer validNeedSecondDelivery(@Param("orderId") Long orderId);

    @Select("SELECT MIN(" + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM + ") AS outboundNum, " + ProductionManageOutboundPackageMessage.COL_ORDER_ID + " AS orderId FROM t_production_manage_outbound_package_message ${ew.customSqlSegment} ")
    List<PackingOutboundNumBO> listMinObNumByOrderIds(@Param(Constants.WRAPPER) Wrapper queryWrapper);
}