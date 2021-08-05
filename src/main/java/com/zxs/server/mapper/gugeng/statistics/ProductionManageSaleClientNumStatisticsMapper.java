package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleClientNumStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleClientNumStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-21
 */
public interface ProductionManageSaleClientNumStatisticsMapper extends CommonSql<ProductionManageSaleClientNumStatistics> {
    @Select(START_SCRIPT
            + "SELECT * FROM "
            + "("
            + "SELECT " + ProductionManageClient.COL_SALE_USER_ID + "," + ProductionManageClient.COL_SALE_USER_NAME + "," +
            "DATE_FORMAT(" + ProductionManageClient.COL_CREATE_DATE + ", '%Y-%m-%d') CreateDate," +
            "COUNT(*) PotentialClientNum FROM"
            + " t_production_manage_client c"
            + START_WHERE
            + " DATE_FORMAT(c." + ProductionManageClient.COL_CREATE_DATE + ", '%Y-%m-%d') &gt;= #{intervalListDTO.startQueryDate}"
            + " and DATE_FORMAT(c." + ProductionManageClient.COL_CREATE_DATE + ", '%Y-%m-%d') &lt;= #{intervalListDTO.endQueryDate}"
            + " and c." + ProductionManageClient.COL_ORGANIZATION_ID + "=#{intervalListDTO.organizationId}"
            + " and c." + ProductionManageClient.COL_SYS_ID + "=#{intervalListDTO.sysId}"
            + END_WHERE
            + " group by c." + ProductionManageClient.COL_SALE_USER_ID
            + ") a " +
            " left JOIN ("
            + " SELECT"
            + " IFNULL(count(DISTINCT o." + ProductionManageOrder.COL_CLIENT_ID + "),0) OrderClientNum," + ProductionManageOrder.COL_SALE_USER_ID
            + " FROM "
            + " t_production_manage_order o "
            + START_WHERE
            + " DATE_FORMAT(o." + ProductionManageOrder.COL_ORDER_DATE + ", '%Y-%m-%d') &gt;= #{intervalListDTO.startQueryDate}"
            + " and DATE_FORMAT(o." + ProductionManageOrder.COL_ORDER_DATE + ", '%Y-%m-%d') &lt;= #{intervalListDTO.endQueryDate}"
            + " and o." + ProductionManageOrder.COL_ORGANIZATION_ID + "=#{intervalListDTO.organizationId}"
            + " and o." + ProductionManageOrder.COL_SYS_ID + "=#{intervalListDTO.sysId}"
            + END_WHERE
            + " group by o." + ProductionManageOrder.COL_SALE_USER_ID
            + ") b ON a." + ProductionManageClient.COL_SALE_USER_ID + " = b." + ProductionManageOrder.COL_SALE_USER_ID
            + "<choose>" +
            "    <when test='intervalListDTO.orderField !=null and intervalListDTO.orderField != &apos;&apos; and intervalListDTO.orderType !=null and intervalListDTO.orderType != &apos;&apos;'>" +
            "     order by ${intervalListDTO.orderField} ${intervalListDTO.orderType}" +
            "    </when>" +
            "     <otherwise>" +
            "      order by OrderClientNum desc" +
            "    </otherwise>" +
            "</choose>"
            + END_SCRIPT
    )
    IPage<ProductionManageSaleClientNumStatisticsVO> pageList(Page<ProductionManageSaleClientNumStatisticsVO> page, @Param("intervalListDTO") DateIntervalListDTO intervalListDTO);

    @Select(START_SCRIPT
            + "SELECT convert(COUNT(IF(" + ProductionManageClient.COL_CLIENT_TYPE + "=1,1,NULL))/COUNT(*),decimal(25,4)) from t_production_manage_client "
            + " where " + ProductionManageClient.COL_ORGANIZATION_ID + "=#{organizationId} AND " + ProductionManageClient.COL_SYS_ID + "=#{sysId} and " + ProductionManageClient.COL_SALE_USER_ID + "=#{saleUserId} "
            + END_SCRIPT
    )
    Double getConversionRates(@Param("saleUserId") String saleUserId, @Param("organizationId") String organizationId, @Param("sysId") String sysId);
}
