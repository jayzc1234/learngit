package com.zxs.server.mapper.gugeng.repairmanage;


import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageConsumeStuff;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-30
 */
public interface ProductionManageConsumeStuffMapper extends CommonSql<ProductionManageConsumeStuff> {
    @Delete("delete from t_production_manage_consume_stuff where " + ProductionManageConsumeStuff.COL_REPAIR_APPLY_ID + "=#{repairApplyId}")
    void deleteByRepairApplyId(@Param("repairApplyId") Long repairApplyId);

    @Select("select * from t_production_manage_consume_stuff where " + ProductionManageConsumeStuff.COL_REPAIR_APPLY_ID + "=#{repairApplyId}")
    List<ProductionManageConsumeStuff> selectByApplyId(@Param("repairApplyId") Long repairApplyId);
}
