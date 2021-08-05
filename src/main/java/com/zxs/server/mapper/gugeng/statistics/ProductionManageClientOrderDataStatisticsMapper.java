package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageClientOrderDataStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageClientOrderDataStatisticsListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-22
 */
public interface ProductionManageClientOrderDataStatisticsMapper extends CommonSql<ProductionManageClientOrderDataStatistics> {
    @Select(START_SCRIPT
            + "select " + ProductionManageClientOrderDataStatistics.COL_CATEGORY_NAME + "," +
            ProductionManageClientOrderDataStatistics.COL_CLIENT_NAME + "," +
            ProductionManageClientOrderDataStatistics.COL_SALE_USER_NAME + "," +
            ProductionManageClientOrderDataStatistics.COL_CLIENT_ADDRESS + "," +
            ProductionManageClientOrderDataStatistics.COL_ORDER_NUM + "," +
            "truncate(SUM(" + ProductionManageClientOrderDataStatistics.COL_ORDER_MONEY + "),2) as orderMoney," +
            "truncate(SUM(" + ProductionManageClientOrderDataStatistics.COL_RECEIVED_ORDER_MONEY + "),2) as receivedOrderMoney " +
            "from t_production_manage_client_order_data_statistics " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageClientOrderDataStatisticsListVO> pageList(Page<ProductionManageClientOrderDataStatisticsListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageClientOrderDataStatistics> queryWrapper);

    /**
     * 按天统计客户订单数据
     * @return
     */
    @Select(START_SCRIPT
            + "select date_format(o." + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate,c." + ProductionManageClient.COL_CATEGORY_ID + "," +
            "c." + ProductionManageClient.COL_ID + " ClientId,c." + ProductionManageClient.COL_SALE_USER_ID + "," +
            "CONCAT_WS(''," + ProductionManageClient.COL_PROVINCE_NAME + "," + ProductionManageClient.COL_CITY_NAME + "," + ProductionManageClient.COL_COUNTY_NAME + "," +
            ProductionManageClient.COL_TOWN_SHIP_NAME + "," + ProductionManageClient.COL_DETAIL_ADDRESS + ") clientAddress," + ProductionManageClient.COL_CATEGORY_NAME + "," +
            "c." + ProductionManageClient.COL_CLIENT_NAME + ",c." + ProductionManageClient.COL_SALE_USER_NAME + ",count(DISTINCT o." + ProductionManageOrder.COL_ID + ") orderNum," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) as orderMoney,truncate(SUM(" + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + "),2) as receivedOrderMoney" +
            " ,c." + ProductionManageClient.COL_ORGANIZATION_ID + ",c." + ProductionManageClient.COL_SYS_ID
            + " from t_production_manage_client c left join t_production_manage_order o  on o." + ProductionManageOrder.COL_CLIENT_ID + "=c." + ProductionManageClient.COL_ID +
            " group by c." + ProductionManageOrder.COL_ID + ",date_format(o." + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d') ORDER BY " + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + " DESC," + ProductionManageOrder.COL_ORDER_MONEY + " desc"
            + END_SCRIPT
    )
    List<ProductionManageClientOrderDataStatistics> clientOrderDataList();
}
