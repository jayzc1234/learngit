package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReturn;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderReturnDataStatiscis;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-22
 */
public interface ProductionManageOrderReturnDataStatiscisMapper extends CommonSql<ProductionManageOrderReturnDataStatiscis> {

    @Select(START_SCRIPT
            + "select DATE_FORMAT(" + ProductionManageOrderProductReturn.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate," +
            "SUM(" + ProductionManageOrderProductReturn.COL_RETURN_BOX_QUANTITY + ") returnBoxQuantity," +
            "count(DISTINCT " + ProductionManageOrderProductReturn.COL_ORDER_ID + ") orderNum," +
            "truncate(SUM(" + ProductionManageOrderProductReturn.COL_RETURN_WEIGHT + "),2) as returnWeight," +
            "SUM(" + ProductionManageOrderProductReturn.COL_RETURN_QUANTITY + ") as returnQuantity " +
            "from t_production_manage_order_product_return " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<Map<String, Object>> returnDataCurveLine(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProductReturn> queryWrapper);
}
