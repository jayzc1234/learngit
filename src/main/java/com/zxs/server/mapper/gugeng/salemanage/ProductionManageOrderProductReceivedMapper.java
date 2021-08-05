package com.zxs.server.mapper.gugeng.salemanage;

import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReceived;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-03
 */
public interface ProductionManageOrderProductReceivedMapper extends CommonSql<ProductionManageOrderProductReceived> {
    @Select(START_SCRIPT
            +"select  max(received_batch_num) "
            +" from t_production_manage_order_product_received "
            +" where order_id=#{orderId}"
            + END_SCRIPT
    )
    Integer getMaxReceivedBatchNum(@Param("orderId") Long orderId);

    @Select(START_SCRIPT
            +"select * "
            +" from t_production_manage_order_product_received "
            +" where order_id=#{orderId}"
            + END_SCRIPT
    )
    List<ProductionManageOrderProductReceived> getByOrderId(@Param("orderId") Long orderId);
}
