package com.zxs.server.mapper.gugeng.producemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.gugeng.HarvestPlanBO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestPlan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
public interface ProductionManageHarvestPlanMapper extends BaseMapper<ProductionManageHarvestPlan> {

    @Select("SELECT IFNULL(SUM(" + ProductionManageHarvestPlan.COL_PRODUCTION_FORECAST + "), 0) FROM t_production_manage_harvest_plan " +
            "WHERE " + ProductionManageHarvestPlan.COL_PLANT_BATCH_ID + " = #{plantBatchId} " +
            "AND " + ProductionManageHarvestPlan.COL_SYS_ID + " = #{sysId} AND " + ProductionManageHarvestPlan.COL_ORGANIZATION_ID + " = #{organizationId}")
    BigDecimal getWeightByBatchId(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId, @Param("organizationId") String organizationId);

    @Select("SELECT DATE_FORMAT(" + ProductionManageHarvestPlan.COL_CREATE_DATE + ", '%Y-%m') AS month, " +
            "IFNULL(SUM(" + ProductionManageHarvestPlan.COL_PRODUCTION_FORECAST + "), 0) AS expectQuantity " +
            "FROM t_production_manage_harvest_plan ${ew.customSqlSegment}")
    List<HarvestPlanBO> listByHalfYearMsg(@Param(Constants.WRAPPER) Wrapper queryWrapper);
}