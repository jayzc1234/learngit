package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.po.gugeng.UpdatePmPO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchOutboundResponseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.PackingOutboundErrorMsgConstants.UPDATE_OUTBOUND_MSG_FAILED;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-24
 */
public interface ProductionManageOutboundMapper extends CommonSql<ProductionManageOutbound> {

    /**
     * 获取出库信息列表
     *
     * @param page
     * @param queryWrapper
     * @return
     * @author shixiongfei
     * @updateDate 2019-09-01
     */
    @Select("SELECT o." + ProductionManageOrder.COL_ID + " AS orderId,o." + ProductionManageOrder.COL_OUTBOUND_STATUS + ",o." + ProductionManageOrder.COL_ORDER_PRODUCT_NUM + "," +
            " o." + ProductionManageOrder.COL_ORDER_NO + ",o." + ProductionManageOrder.COL_ORDER_TYPE + ", c." + ProductionManageClient.COL_ID + " AS clientId, " +
            "c." + ProductionManageClient.COL_CLIENT_NAME + ", o." + ProductionManageOrder.COL_DELIVERY_DATE + ", o." + ProductionManageOrder.COL_PRODUCT_NAMES + " as productNames," +
            " o." + ProductionManageOrder.COL_ORDER_WEIGHT + ", " +
            "o." + ProductionManageOrder.COL_ORDER_QUANTITY + ", o." + ProductionManageOrder.COL_ORDER_STATUS + ", o." + ProductionManageOrder.COL_DELIVERY_TYPE + "," +
            " ob." + ProductionManageOutbound.COL_ID + " AS outboundId, IFNULL(o." + ProductionManageOrder.COL_GG_TOTAL_PARTION_NUM + ", 0) AS totalPartionNum, " +
            "IFNULL(ob." + ProductionManageOutbound.COL_PACKING_WEIGHT + ", 0) AS packingWeight, IFNULL(ob." + ProductionManageOutbound.COL_GG_PACKING_SERVINGS + ", 0) AS packingServings, " +
            "IFNULL(ob." + ProductionManageOutbound.COL_PACKING_NUM + ", 0) AS packingNum, IFNULL(ob." + ProductionManageOutbound.COL_PACKING_BOX_NUM + ", 0) AS packingBoxNum, " +
            "ob." + ProductionManageOutbound.COL_OUTBOUND_DATE + ", ob." + ProductionManageOutbound.COL_CREATE_USER_ID + ", " +
            "ob." + ProductionManageOutbound.COL_CREATE_USER_NAME + ", " +
            "IFNULL(SUM(op." + ProductionManageOrderProduct.COL_PRODUCT_NUM + "), 0) AS productNum FROM t_production_manage_client c " +
            " LEFT JOIN t_production_manage_order o ON c." + ProductionManageClient.COL_ID + " = o." + ProductionManageOrder.COL_CLIENT_ID +
            " LEFT JOIN t_production_manage_outbound ob ON ob." + ProductionManageOutbound.COL_ORDER_ID + " = o." + ProductionManageOrder.COL_ID +
            " LEFT JOIN t_production_manage_order_product op ON op." + ProductionManageOrderProduct.COL_ORDER_ID + " = o." + ProductionManageOrder.COL_ID + " ${ew.customSqlSegment}")
    IPage<SearchOutboundResponseVO> list(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取出库信息列表
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select("SELECT o." + ProductionManageOrder.COL_ID + " AS orderId, o." + ProductionManageOrder.COL_ORDER_NO + ",o." + ProductionManageOrder.COL_DELIVERY_TYPE + ", " +
            "o." + ProductionManageOrder.COL_OUTBOUND_STATUS + ", c." + ProductionManageClient.COL_ID + " AS clientId, c." + ProductionManageClient.COL_CLIENT_NAME + ", " +
            "o." + ProductionManageOrder.COL_DELIVERY_DATE + ", o." + ProductionManageOrder.COL_PRODUCT_NAMES + ", o." + ProductionManageOrder.COL_ORDER_WEIGHT + ", " +
            " o." + ProductionManageOrder.COL_ORDER_QUANTITY + ", o." + ProductionManageOrder.COL_ORDER_STATUS + ", ob." + ProductionManageOutbound.COL_ID + " AS outboundId, " +
            "ob." + ProductionManageOutbound.COL_PACKING_WEIGHT + ", " +
            "ob." + ProductionManageOutbound.COL_PACKING_NUM + ", ob." + ProductionManageOutbound.COL_OUTBOUND_DATE + ", ob." + ProductionManageOutbound.COL_CREATE_USER_ID + ", " +
            "ob." + ProductionManageOutbound.COL_CREATE_USER_NAME +
            " FROM t_production_manage_order o " +
            " LEFT JOIN t_production_manage_outbound ob ON ob." + ProductionManageOutbound.COL_ORDER_ID + " = o." + ProductionManageOrder.COL_ID +
            " LEFT JOIN t_production_manage_client c ON c." + ProductionManageClient.COL_ID + " = o." + ProductionManageOrder.COL_CLIENT_ID + " ${ew.customSqlSegment}")
    IPage<SearchOutboundResponseVO> listByIds(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select(START_SCRIPT +
            "SELECT o." + ProductionManageOrder.COL_ID + " AS orderId, o." + ProductionManageOrder.COL_OUTBOUND_STATUS + ",o." + ProductionManageOrder.COL_DELIVERY_TYPE + "," +
            "o." + ProductionManageOrder.COL_ORDER_NO + ", c." + ProductionManageClient.COL_ID + " AS clientId, c." + ProductionManageClient.COL_CLIENT_NAME + "," +
            " o." + ProductionManageOrder.COL_DELIVERY_DATE + ", o." + ProductionManageOrder.COL_PRODUCT_NAMES + ", " +
            "o." + ProductionManageOrder.COL_ORDER_WEIGHT + ", IFNULL(o." + ProductionManageOrder.COL_GG_TOTAL_PARTION_NUM + ", 0) AS totalPartionNum, " +
            "o." + ProductionManageOrder.COL_ORDER_QUANTITY + ", o." + ProductionManageOrder.COL_ORDER_STATUS + ", ob." + ProductionManageOutbound.COL_ID + " AS outboundId, " +
            "ob." + ProductionManageOutbound.COL_PACKING_WEIGHT + ", IFNULL(ob." + ProductionManageOutbound.COL_GG_PACKING_SERVINGS + ", 0) AS packingServings, " +
            "ob." + ProductionManageOutbound.COL_PACKING_NUM + ", ob." + ProductionManageOutbound.COL_OUTBOUND_DATE + ", ob." + ProductionManageOutbound.COL_CREATE_USER_ID + ", ob." + ProductionManageOutbound.COL_CREATE_USER_NAME +
            " FROM t_production_manage_order o " +
            " LEFT JOIN t_production_manage_outbound ob ON ob." + ProductionManageOutbound.COL_ORDER_ID + " = o." + ProductionManageOrder.COL_ID +
            " LEFT JOIN t_production_manage_client c ON c." + ProductionManageClient.COL_ID + " = o." + ProductionManageOrder.COL_CLIENT_ID + " "
            + START_WHERE
            + "o." + ProductionManageOrder.COL_ID + " in"
            + "<foreach item='item' collection='list' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + END_WHERE
            + END_SCRIPT)
	List<SearchOutboundResponseVO> listExcelByIds(List<? extends Serializable> ids);

    @Select(START_SCRIPT
            + "SELECT SUM(" + ProductionManageOutbound.COL_PACKING_WEIGHT + ") from t_production_manage_outbound "
            + START_WHERE
            + " " + ProductionManageOutbound.COL_OUTBOUND_DATE + " is not null "
            + "<if test='startQueryDate !=null and startQueryDate != &apos;&apos;'> and DATE_FORMAT(" + ProductionManageOutbound.COL_OUTBOUND_DATE + ",'%Y-%m-%d') &gt;= #{startQueryDate}</if>"
            + "<if test='endQueryDate !=null and endQueryDate != &apos;&apos;'> and DATE_FORMAT(" + ProductionManageOutbound.COL_OUTBOUND_DATE + ",'%Y-%m-%d') &lt;= #{endQueryDate}</if>"
            + "<if test='organizationId !=null and organizationId != &apos;&apos;'> and " + ProductionManageOutbound.COL_ORGANIZATION_ID + " =#{organizationId}</if>"
            + "<if test='sysId !=null and sysId != &apos;&apos;'> and " + ProductionManageOutbound.COL_SYS_ID + " =#{sysId}</if>"
            + END_WHERE
            + END_SCRIPT)
    Double statisticsOutBoundWeight(DateIntervalDTO dateIntervalDTO);

    @Select(START_SCRIPT
            + "SELECT SUM(ifnull(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ",0)) as weight from  t_production_manage_stock_loss  "
            + START_WHERE
            + "<if test='startQueryDate !=null and startQueryDate != &apos;&apos; and endQueryDate !=null and endQueryDate != &apos;&apos;'> "
            + "  DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d') &gt;= #{startQueryDate} and " +
            " DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ",'%Y-%m-%d') &lt;= #{endQueryDate}"
            + "</if>"
            + "<if test='organizationId !=null and organizationId != &apos;&apos;'> and " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " =#{organizationId}</if>"
            + "<if test='sysId !=null and sysId != &apos;&apos;'> and " + ProductionManageStockLoss.COL_SYS_ID + " =#{sysId}</if>"
            + END_WHERE
            + END_SCRIPT)
    Double statisticsStackAndLossOutWeight(DateIntervalDTO dateIntervalDTO);

    /**
     * 更新出库的数值信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default void updateNumber(UpdatePmPO po) {
        UpdateWrapper<ProductionManageOutbound> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql(String.format(ProductionManageOutbound.COL_PACKING_WEIGHT+" = IFNULL(" + ProductionManageOutbound.COL_PACKING_WEIGHT + ", 0) + %f", po.getWeight()))
                .setSql(String.format(ProductionManageOutbound.COL_PACKING_NUM+" = IFNULL(" + ProductionManageOutbound.COL_PACKING_NUM + ", 0) + %d", po.getQuantity()))
                .setSql(String.format(ProductionManageOutbound.COL_PACKING_BOX_NUM+" = IFNULL(" + ProductionManageOutbound.COL_PACKING_BOX_NUM + ", 0) + %d", po.getBoxNum()))
                .setSql(String.format(ProductionManageOutbound.COL_GG_PACKING_SERVINGS+" = IFNULL(" + ProductionManageOutbound.COL_GG_PACKING_SERVINGS + ", 0) + %d", po.getServings()))
                .eq(OLD_ID, po.getOutboundId());
        int count = update(null, updateWrapper);
        CustomAssert.zero2Error(count, UPDATE_OUTBOUND_MSG_FAILED);
    }

    @Select(
            "SELECT * from  t_production_manage_outbound where " + ProductionManageOutbound.COL_ORDER_ID + "=#{orderId} order by " + ProductionManageOutbound.COL_ID + " desc limit 1"
            )
    ProductionManageOutbound selectOneByOrderId(@Param("orderId") Long orderId);
}