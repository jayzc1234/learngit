package com.zxs.server.mapper.gugeng.salemanage;

import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReturn;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-03
 */
public interface ProductionManageOrderProductReturnMapper extends CommonSql<ProductionManageOrderProductReturn> {
    @Select(START_SCRIPT
            +"select max(ReturnBatchNum) from production_manage_order_product_return " +
            " where order_id=#{orderId}"
            + END_SCRIPT
    )
    Integer selectMaxReturnBatchNum(@Param("orderId") Long orderId);

}
