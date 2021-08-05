package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffInWarehouse;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 材料入库表 Mapper 接口
 * </p>
 * ×
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
public interface ProductionManageStuffInWarehouseMapper extends CommonSql<ProductionManageStuffInWarehouse> {
    @Select(START_SCRIPT
            + "select TotalInventory from t_production_manage_stuff_in_warehouse ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    BigDecimal selectStuffBatchRemainingNum(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffInWarehouse> queryWrapper);

    @Select(START_SCRIPT
            + "select if(count(*)=0,0,1) from t_production_manage_stuff_in_warehouse ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    boolean isStuffBatchInOrgHas(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffInWarehouse> stuffInWarehouseQueryWrapper);
}
