package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 出库发货方式表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
public interface ProductionManageOutboundDeliveryWayMapper extends BaseMapper<ProductionManageOutboundDeliveryWay> {


    @Select("SELECT COUNT(*) FROM t_production_manage_outbound_delivery_way dw " +
            "LEFT JOIN t_production_manage_outbound o ON o." + ProductionManageOutbound.COL_ID + " = dw." + ProductionManageOutboundDeliveryWay.COL_OUTBOUND_ID + " ${ew.customSqlSegment}")
    Integer getTotalNumber(@Param(Constants.WRAPPER) Wrapper<ProductionManageOutboundDeliveryWay> wrapper);

}