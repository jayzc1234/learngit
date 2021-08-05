package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStorageStockDayStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.VStockFlow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ProductionManageStorageStockDayStatisticsMapper  extends BaseMapper<ProductionManageStorageStockDayStatistics> {

/*    @Select("SELECT TotalInboundWeight instorageWeight, TotalOutboundWeight outboundWeight, Weight stockWeight, DATE_FORMAT(CreateDate,'%Y-%m-%d') as operateDate, ProductId productId, ProductName productName from production_manage_stock \n" +
            "  ${ew.customSqlSegment} "+
            "GROUP BY DATE_FORMAT(CreateDate,'%Y-%m-%d'),ProductId ")
    List<ProductionManageStorageStockDayStatistics> selectStockWeight(@Param(Constants.WRAPPER) Wrapper queryWrapper);*/

    String STOCK_WEIGHT_SQL = "  SELECT InWeight instorageWeight, case when ReturnWeight is null then OutWeight else  (OutWeight-ReturnWeight) end outboundWeight, OutWeight, ReturnWeight, StockWeight stockWeight, i1.dateTime operateDate,i1.productId,i1.productName  FROM     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") InWeight, i." + VStockFlow.COL_PRODUCT_ID + ", i." + VStockFlow.COL_PRODUCT_NAME + " FROM " + VStockFlow.TABLE_NAME + " i\n" +
            "       WHERE " + VStockFlow.COL_OUT_IN_TYPE + " in( 1,2,6 )   and  i." + VStockFlow.COL_SYS_ID + " = #{sysId}  AND i." + VStockFlow.COL_ORGANIZATION_ID + " = #{organizationId}\n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')," + VStockFlow.COL_PRODUCT_ID + " ) i1     \n" +
            "                    LEFT JOIN     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") OutWeight,i." + VStockFlow.COL_PRODUCT_ID + ", i." + VStockFlow.COL_PRODUCT_NAME + " FROM " + VStockFlow.TABLE_NAME + " i\n" +
            "         WHERE   " + VStockFlow.COL_OUT_IN_TYPE + " in( 3,4,5 )     \n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')," + VStockFlow.COL_PRODUCT_ID + " ) o1 on i1.dateTime= o1.dateTime and i1." + VStockFlow.COL_PRODUCT_ID + " = o1." + VStockFlow.COL_PRODUCT_ID +
            "        LEFT JOIN     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") ReturnWeight,i." + VStockFlow.COL_PRODUCT_ID + ", i." + VStockFlow.COL_PRODUCT_NAME + " FROM " + VStockFlow.TABLE_NAME + " i\n" +
            "         WHERE   " + VStockFlow.COL_OUT_IN_TYPE + " in( 9 )     \n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')," + VStockFlow.COL_PRODUCT_ID + " ) r1 on i1.dateTime= r1.dateTime and i1." + VStockFlow.COL_PRODUCT_ID + " = r1." + VStockFlow.COL_PRODUCT_ID +
            "                    LEFT JOIN     \n" +
            "              ( select case when (OutWeight is null and ReturnWeight is null) then InWeight \n" +
            "         when (OutWeight is not null and ReturnWeight is null) then (InWeight-OutWeight)\n" +
            "          when (OutWeight is null and ReturnWeight is not null) then (InWeight+ReturnWeight)\n" +
            "          when (OutWeight is not null and ReturnWeight is not null) then (InWeight-OutWeight+ReturnWeight) end StockWeight, \n" +
            "         dateTime, " + VStockFlow.COL_PRODUCT_ID + "," + VStockFlow.COL_PRODUCT_NAME + " from (    \n" +
            "          select     \n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from " + VStockFlow.TABLE_NAME + " t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2." + VStockFlow.COL_OUT_IN_TYPE + " in( 1,2,6 )   ) InWeight,    \n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from " + VStockFlow.TABLE_NAME + " t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2." + VStockFlow.COL_OUT_IN_TYPE + " in(  3,4,5  )   ) OutWeight,\n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from " + VStockFlow.TABLE_NAME + " t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2." + VStockFlow.COL_OUT_IN_TYPE + " in(  9  )   ) ReturnWeight        \n" +
            "           ,  t1.dateTime, t1." + VStockFlow.COL_PRODUCT_ID + ", t1." + VStockFlow.COL_PRODUCT_NAME +
            "          from (    \n" +
            "           SELECT DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, i." + VStockFlow.COL_PRODUCT_ID + ", i." + VStockFlow.COL_PRODUCT_NAME + "  FROM " + VStockFlow.TABLE_NAME + " i GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')," + VStockFlow.COL_PRODUCT_ID +
            "          ) t1      \n" +
            "                ) t4  ) s1 on i1.dateTime = s1.dateTime  and i1." + VStockFlow.COL_PRODUCT_ID + " = s1." + VStockFlow.COL_PRODUCT_ID +
            "                ";

    @Select(STOCK_WEIGHT_SQL)
    List<ProductionManageStorageStockDayStatistics> selectStockWeight2(String organizationId, String sysId);


    @Select("  \n" +
            "  SELECT  InWeight  instorageWeight,  case  when  ReturnWeight  is  null  then  OutWeight  else    (OutWeight-ReturnWeight)  end  outboundWeight,  OutWeight,  ReturnWeight,  StockWeight  stockWeight,  i1.dateTime  operateDate,i1." + VStockFlow.COL_PRODUCT_ID + ",i1." + VStockFlow.COL_PRODUCT_NAME + "    FROM          \n" +
            "                          (  SELECT      DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  dateTime,  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  InWeight,  i." + VStockFlow.COL_PRODUCT_ID + ",  i." + VStockFlow.COL_PRODUCT_NAME + "  FROM  " + VStockFlow.TABLE_NAME + "  i\n" +
            "WHERE  " + VStockFlow.COL_OUT_IN_TYPE + "  in(  1,2,6  )    and  i." + VStockFlow.COL_SYS_ID + " = #{sysId}  AND i." + VStockFlow.COL_ORGANIZATION_ID + " = #{organizationId} and  " + VStockFlow.COL_OUT_IN_DATE + " >= #{start} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{end} \n" +
            "                                        GROUP  BY  DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  )  i1          \n" +
            "                                        LEFT  JOIN          \n" +
            "                          (  SELECT      DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  dateTime,  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  OutWeight,i." + VStockFlow.COL_PRODUCT_ID + ",  i." + VStockFlow.COL_PRODUCT_NAME + "  FROM  " + VStockFlow.TABLE_NAME + "  i\n" +
            "WHERE      " + VStockFlow.COL_OUT_IN_TYPE + "  in(  3,4,5  )    and  i." + VStockFlow.COL_SYS_ID + " = #{sysId}  AND i." + VStockFlow.COL_ORGANIZATION_ID + " = #{organizationId} and  " + VStockFlow.COL_OUT_IN_DATE + " >= #{start} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{end}        \n" +
            "                                        GROUP  BY  DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  )  o1  on  i1.dateTime=  o1.dateTime     \n" +
            "    LEFT  JOIN          \n" +
            "                          (  SELECT      DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  dateTime,  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  ReturnWeight,i." + VStockFlow.COL_PRODUCT_ID + ",  i." + VStockFlow.COL_PRODUCT_NAME + "  FROM  " + VStockFlow.TABLE_NAME + "  i\n" +
            "WHERE      " + VStockFlow.COL_OUT_IN_TYPE + "  in(  9  )     and  i." + VStockFlow.COL_SYS_ID + " = #{sysId}  AND i." + VStockFlow.COL_ORGANIZATION_ID + " = #{organizationId} and  " + VStockFlow.COL_OUT_IN_DATE + " >= #{start} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{end}       \n" +
            "                                        GROUP  BY  DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  )  r1  on  i1.dateTime=  r1.dateTime  \n" +
            "                                        LEFT  JOIN          \n" +
            "                            ( \n" +
            "\t\t\t\t\t\t\t\t\t\t\t\tselect datetime, sum(StockWeight)\t StockWeight from (\t\n" +
            "\t\t\t\t\t\t\t\t\t\t\t\t\tselect  case  when  (OutWeight  is  null  and  ReturnWeight  is  null)  then  InWeight  \n" +
            "when  (OutWeight  is  not  null  and  ReturnWeight  is  null)  then  (InWeight-OutWeight)\n" +
            "    when  (OutWeight  is  null  and  ReturnWeight  is  not  null)  then  (InWeight+ReturnWeight)\n" +
            "    when  (OutWeight  is  not  null  and  ReturnWeight  is  not  null)  then  (InWeight-OutWeight+ReturnWeight)\tend  StockWeight,  \n" +
            "dateTime,  " + VStockFlow.COL_PRODUCT_ID + "," + VStockFlow.COL_PRODUCT_NAME + "  from  (       \n" +
            "\n" +
            "select          \n" +
            "(select  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  from  " + VStockFlow.TABLE_NAME + "  t2  where  t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + "  and  t2." + VStockFlow.COL_OUT_IN_DATE + "  <=str_to_date(  CONCAT(t1.dateTime,  '  23:59:59')  ,'%Y-%m-%d  %H:%i:%s')  and    t2." + VStockFlow.COL_OUT_IN_TYPE + "  in(  1,2,6  )      )  InWeight,        \n" +
            "(select  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  from  " + VStockFlow.TABLE_NAME + "  t2  where  t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + "  and  t2." + VStockFlow.COL_OUT_IN_DATE + "  <=str_to_date(  CONCAT(t1.dateTime,  '  23:59:59')  ,'%Y-%m-%d  %H:%i:%s')  and    t2." + VStockFlow.COL_OUT_IN_TYPE + "  in(    3,4,5    )      )  OutWeight,\n" +
            "(select  sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ")  from  " + VStockFlow.TABLE_NAME + "  t2  where  t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + "  and  t2." + VStockFlow.COL_OUT_IN_DATE + "  <=str_to_date(  CONCAT(t1.dateTime,  '  23:59:59')  ,'%Y-%m-%d  %H:%i:%s')  and    t2." + VStockFlow.COL_OUT_IN_TYPE + "  in(    9    )      )  ReturnWeight        \n" +
            ",    t1.dateTime,  t1." + VStockFlow.COL_PRODUCT_ID + ",  t1." + VStockFlow.COL_PRODUCT_NAME + "        \n" +
            "from  (        \n" +
            "SELECT  DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')  dateTime,  i." + VStockFlow.COL_PRODUCT_ID + ",  i." + VStockFlow.COL_PRODUCT_NAME + "    FROM  " + VStockFlow.TABLE_NAME + "  i  where " + VStockFlow.COL_OUT_IN_DATE + " >= #{start} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{end}  GROUP  BY  DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')   ," + VStockFlow.COL_PRODUCT_ID +
            " )  t1  \n" +
            "\n" +
            " ) z1\n" +
            "\n" +
            "    )z2 \n" +
            "group by datetime\n" +
            "\n" +
            ")  s1  on  i1.dateTime  =  s1.dateTime    \n" +
            "                                 ")
    List<ProductionManageStorageStockDayStatistics> selectTodayStockWeight2(@Param("organizationId") String organizationId, @Param("sysId") String sysId, @Param("start") String start, @Param("end") String end);

    @Select("  SELECT InWeight instorageWeight, case when ReturnWeight is null then OutWeight else  (OutWeight-ReturnWeight) end outboundWeight, OutWeight, ReturnWeight, StockWeight stockWeight, i1.dateTime operateDate,i1." + VStockFlow.COL_PRODUCT_ID + ",i1.productName  FROM     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") InWeight, i." + VStockFlow.COL_PRODUCT_ID + ", i.ProductName FROM v_stock_flow i\n" +
            "       WHERE OutInType in( 1,2,6 )   and  i.SysId = #{sysId}  AND i.OrganizationId = #{organizationId} and  " + VStockFlow.COL_OUT_IN_DATE + " >= #{startTime} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{endTime}  and " + VStockFlow.COL_PRODUCT_ID + "=#{productId}  \n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') ) i1     \n" +
            "                    LEFT JOIN     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") OutWeight,i." + VStockFlow.COL_PRODUCT_ID + ", i.ProductName FROM v_stock_flow i\n" +
            "         WHERE   OutInType in( 3,4,5 )     \n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')," + VStockFlow.COL_PRODUCT_ID + " ) o1 on i1.dateTime= o1.dateTime and i1." + VStockFlow.COL_PRODUCT_ID + " = o1." + VStockFlow.COL_PRODUCT_ID +
            "        LEFT JOIN     \n" +
            "             ( SELECT   DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") ReturnWeight,i." + VStockFlow.COL_PRODUCT_ID + ", i.ProductName FROM v_stock_flow i\n" +
            "         WHERE   OutInType in( 9 )     \n" +
            "                    GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') ) r1 on i1.dateTime= r1.dateTime and i1." + VStockFlow.COL_PRODUCT_ID + " = r1." + VStockFlow.COL_PRODUCT_ID +
            "                    LEFT JOIN     \n" +
            "              ( select case when (OutWeight is null and ReturnWeight is null) then InWeight \n" +
            "         when (OutWeight is not null and ReturnWeight is null) then (InWeight-OutWeight)\n" +
            "          when (OutWeight is null and ReturnWeight is not null) then (InWeight+ReturnWeight)\n" +
            "          when (OutWeight is not null and ReturnWeight is not null) then (InWeight-OutWeight+ReturnWeight) end StockWeight, \n" +
            "         dateTime, " + VStockFlow.COL_PRODUCT_ID + ",ProductName from (    \n" +
            "          select     \n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from v_stock_flow t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2.OutInType in( 1,2,6 )   ) InWeight,    \n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from v_stock_flow t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2.OutInType in(  3,4,5  )   ) OutWeight,\n" +
            "           (select sum(" + VStockFlow.COL_OUT_IN_WEIGHT + ") from v_stock_flow t2 where t2." + VStockFlow.COL_PRODUCT_ID + "=t1." + VStockFlow.COL_PRODUCT_ID + " and t2." + VStockFlow.COL_OUT_IN_DATE + " <=str_to_date( CONCAT(t1.dateTime, ' 23:59:59') ,'%Y-%m-%d %H:%i:%s') and  t2.OutInType in(  9  )   ) ReturnWeight        \n" +
            "           ,  t1.dateTime, t1." + VStockFlow.COL_PRODUCT_ID + ", t1.ProductName    \n" +
            "          from (    \n" +
            "           SELECT DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d') dateTime, i." + VStockFlow.COL_PRODUCT_ID + ", i.ProductName  FROM v_stock_flow i  where " + VStockFlow.COL_OUT_IN_DATE + " >= #{startTime} and  " + VStockFlow.COL_OUT_IN_DATE + " < #{endTime} and " + VStockFlow.COL_PRODUCT_ID + "=#{productId} GROUP BY DATE_FORMAT(" + VStockFlow.COL_OUT_IN_DATE + ",'%Y-%m-%d')     \n" +
            "          ) t1      \n" +
            "                ) t4  ) s1 on i1.dateTime = s1.dateTime  and i1." + VStockFlow.COL_PRODUCT_ID + " = s1." + VStockFlow.COL_PRODUCT_ID + "\n" +
            "                ")
    List<ProductionManageStorageStockDayStatistics> selectTodayStockWeight2ByProductId(String organizationId, String sysId, String startTime, String endTime, String productId);


}
