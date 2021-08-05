package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReceived;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderProductDataStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderProductDataStatisticsListVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-30
 */
public interface ProductionManageOrderProductDataStatisticsMapper extends CommonSql<ProductionManageOrderProductDataStatistics> {
    @Select(START_SCRIPT
            + "select  " + ProductionManageOrderProductDataStatistics.COL_PRODUCT_NAME + "," +
            "IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY + "),2),0) OrderMoney," +
            "IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY + "),2),0) ReceivedOrderMoney," +
            ProductionManageOrderProductDataStatistics.COL_PRODUCT_SORT_NAME +
            " from t_production_manage_order_product_data_statistics " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageOrderProductDataStatisticsListVO> pageList(Page<ProductionManageOrderProductDataStatisticsListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper);

    @Select(START_SCRIPT
            + "select  " + ProductionManageOrderProductDataStatistics.COL_PRODUCT_NAME + "," +
            "IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY + "),2),0) OrderMoney," +
            "IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY + "),2),0) ReceivedOrderMoney," +
            ProductionManageOrderProductDataStatistics.COL_PRODUCT_SORT_NAME +
            " from t_production_manage_order_product_data_statistics " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageOrderProductDataStatisticsListVO> selectByWrapper(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper);

    @Insert(START_SCRIPT
            + "INSERT into t_production_manage_order_product_data_statistics " +
            "(" + ProductionManageOrderProductDataStatistics.COL_ORDER_DATE + "," + ProductionManageOrderProductDataStatistics.COL_ORGANIZATION_ID + "," +
            ProductionManageOrderProductDataStatistics.COL_SYS_ID + "," + ProductionManageOrderProductDataStatistics.COL_PRODUCT_SORT_ID + "," +
            ProductionManageOrderProductDataStatistics.COL_PRODUCT_SORT_NAME + "," + ProductionManageOrderProductDataStatistics.COL_PRODUCT_ID + "," +
            ProductionManageOrderProductDataStatistics.COL_PRODUCT_NAME + "," + ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY + "," + ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY + ")" +
            " SELECT  DATE_FORMAT(o." + ProductionManageOrderProductDataStatistics.COL_ORDER_DATE + ", '%Y-%m-%d') OrderDate," +
            "o." + ProductionManageOrderProductDataStatistics.COL_ORGANIZATION_ID + "," +
            "o." + ProductionManageOrderProductDataStatistics.COL_SYS_ID +
            "  ,op." + ProductionManageOrderProduct.COL_PRODUCT_SORT_ID +
            " ,op." + ProductionManageOrderProduct.COL_PRODUCT_SORT_NAME +
            ",op." + ProductionManageOrderProduct.COL_PRODUCT_ID +
            ",op." + ProductionManageOrderProduct.COL_PRODUCT_NAME +
            ",SUM(opr." + ProductionManageOrderProductReceived.COL_RECEIVED_PRO_MONEY + ") ReceivedOrderMoney," +
            "  SUM(op." + ProductionManageOrderProduct.COL_TOTAL_PRICE + ") OrderMoney" +
            " FROM " +
            " t_production_manage_order o" +
            " LEFT JOIN t_production_manage_order_product op ON o." + ProductionManageOrder.COL_ID + " = op." + ProductionManageOrderProduct.COL_ORDER_ID +
            " LEFT JOIN t_production_manage_order_product_received opr ON op." + ProductionManageOrderProduct.COL_PRODUCT_ID + " = opr." + ProductionManageOrderProductReceived.COL_PRODUCT_ID +
            " AND op." + ProductionManageOrderProduct.COL_ORDER_ID + " = opr." + ProductionManageOrderProductReceived.COL_ORDER_ID +
            START_WHERE
            + "<if test = 'productId != null and productId != &apos;&apos; '>  op." + ProductionManageOrderProduct.COL_PRODUCT_ID + " = #{productId}</if>"
            + "<if test = 'orderDate != null and orderDate != &apos;&apos;  '> and DATE_FORMAT(o." + ProductionManageOrder.COL_ORDER_DATE + ", '%Y-%m-%d') = #{orderDate}</if>"
            + END_WHERE
            + "GROUP BY op." + ProductionManageOrderProduct.COL_PRODUCT_ID + ",DATE_FORMAT(o." + ProductionManageOrder.COL_ORDER_DATE + ", '%Y-%m-%d')"
            + END_SCRIPT
    )
    void syncData(@Param("productId") String productId, @Param("orderDate") String orderDate);

    @Select(START_SCRIPT
            + "select  IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_ORDER_MONEY + "),2),0) totalOrderMoney," +
            "IFNULL(truncate(sum(" + ProductionManageOrderProductDataStatistics.COL_RECEIVED_ORDER_MONEY + "),2),0) totalReceivedOrderMoney " +
            " from t_production_manage_order_product_data_statistics "
            + " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    ProductionManageOrderProductDataStatisticsListVO sumMoney(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProductDataStatistics> queryWrapper);

}
