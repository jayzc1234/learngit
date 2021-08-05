package com.zxs.server.mapper.gugeng.datascreen;


import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.*;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductionManageSaleScreenMapper {

    @Select("SELECT DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m') month, count(*) headquartersOrder, 0 externalOrder  FROM `t_production_manage_order`\n" +
            " where " + ProductionManageOrder.COL_ORDER_DATE + " is not null  " +
            " and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} \n" +
            " group by DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')")
    List<ProductionManageSalesOrderAnalysis> selectSalesOrderAnalysis(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT " + ProductionManageOrder.COL_CLIENT_NAME + " name, " + ProductionManageOrder.COL_ORDER_MONEY + " lumpSum," +
            " case " + ProductionManageOrder.COL_ORDER_TYPE + " when 3 then concat( " + ProductionManageOrder.COL_ORDER_QUANTITY + ",'箱') " +
            " when 2 then  concat(" + ProductionManageOrder.COL_ORDER_PRODUCT_NUM + " ,'个') when 1 then  concat( " + ProductionManageOrder.COL_ORDER_WEIGHT + ",'斤')  end orderNumber " +
            " FROM t_production_manage_order \n" +
            " where  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} " +
            " ORDER BY " + ProductionManageOrder.COL_ID + " DESC LIMIT 0,15")
    List<ProductionManageSalesOrder> selectLastestOrder(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT " + ProductionManageOrderProduct.COL_PRODUCT_ID + ", " + ProductionManageOrderProduct.COL_PRODUCT_NAME + " commodityName, " +
            " ROUND(sum(" + ProductionManageOrderProduct.COL_TOTAL_PRICE + "),2) salesAmount FROM t_production_manage_order_product  p\n" +
            " join t_production_manage_order o on p." + ProductionManageOrderProduct.COL_ORDER_ID + " = o." + ProductionManageOrder.COL_ID + " \n" +
            " where  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} " +
            " GROUP by " + ProductionManageOrderProduct.COL_PRODUCT_ID + "\n" +
            " order by salesAmount desc  LIMIT 0,15")
    List<ProductionManageCommodityRanking> commodityRanking(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m') month, ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) lumpSum  " +
            " FROM `t_production_manage_order`\n" +
            " where " + ProductionManageOrder.COL_ORDER_DATE + " is not null and " + ProductionManageOrder.COL_ORDER_DATE + ">#{start} " +
            " and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId}\n" +
            " group by DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')")
    List<ProductionManageSalesDataBar> selectSalesDataBar(@Param("organizationId") String organizationId, @Param("sysId") String sysId, String start);

    @Select("SELECT " + ProductionManageOrder.COL_CLIENT_MIDDLE_ADDRESS + " regionalName,ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) quantityOrder " +
            " FROM `t_production_manage_order`\n" +
            " where " + ProductionManageOrder.COL_CLIENT_MIDDLE_ADDRESS + " is not null and LENGTH(" + ProductionManageOrder.COL_CLIENT_MIDDLE_ADDRESS + ")>0  " +
            " and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId}\n" +
            " group by " + ProductionManageOrder.COL_CLIENT_MIDDLE_ADDRESS + "\n" +
            " order by quantityOrder desc  LIMIT 0,15 ")
    List<ProductionManageRegionalRanking> selectRegionalRanking(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%m-%d') name,ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) lumpSum " +
            " FROM `t_production_manage_order`\n" +
            " where " + ProductionManageOrder.COL_ORDER_DATE + ">= #{weekDayStr} and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} " +
            " and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} \n" +
            " group by DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%m-%d')")
    List<ProductionManageSalesOrder> selectDayOrder(@Param("weekDayStr") String weekDayStr, @Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT '今日销售' type,count(*) orderNumber, case when sum(" + ProductionManageOrder.COL_ORDER_MONEY + ")  is null then 0 " +
            " else  ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) end lumpSum FROM `t_production_manage_order` \n" +
            " where " + ProductionManageOrder.COL_ORDER_DATE + ">= #{today} and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} " +
            " and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} \n" +
            " union \n" +
            " SELECT '本月销售' type,count(*) orderNumber, ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) lumpSum FROM `t_production_manage_order` \n" +
            " where " + ProductionManageOrder.COL_ORDER_DATE + ">= #{month} and  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} " +
            " and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId} \n" +
            " union\n" +
            " SELECT '年度销售' type,count(*) orderNumber,ROUND( sum(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) lumpSum FROM `t_production_manage_order`  " +
            " where  " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId}  ")
    List<ProductionManageSalesData> selectSalesData(@Param("today") String today, @Param("month") String month, @Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT ROUND( sum(o." + ProductionManageOrder.COL_ORDER_MONEY + "),2) lumpSum, c." + ProductionManageClient.COL_PROVINCE_NAME + " regionalName " +
            " FROM t_production_manage_order o \n" +
            " left join t_production_manage_client c on o." + ProductionManageOrder.COL_CLIENT_ID + "= c." + ProductionManageClient.COL_ID + "\n" +
            " where c." + ProductionManageClient.COL_PROVINCE_NAME + " is not null and length(c." + ProductionManageClient.COL_PROVINCE_NAME + ")>0 " +
            " and   o." + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} " +
            " and o." + ProductionManageOrder.COL_SYS_ID + "=#{sysId} \n" +
            " group by c." + ProductionManageClient.COL_PROVINCE_NAME + "\n" +
            " order by lumpSum desc")
    List<ProductionManageRegionalRanking> selectTopSaleRegionalList(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("SELECT count( distinct " + ProductionManageOrder.COL_CLIENT_ID + ") orderCustomer FROM t_production_manage_order " +
            "where " + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageOrder.COL_SYS_ID + "=#{sysId}  ")
    Integer selectClientCount(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

}
