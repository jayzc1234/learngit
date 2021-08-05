package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderDataByType;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderDataByTypeListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderPayBackListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleProductDataListVO;
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
 * @since 2019-10-17
 */
public interface ProductionManageOrderDataByTypeMapper extends CommonSql<ProductionManageOrderDataByType> {

    @Select(START_SCRIPT
            + "select DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m') OrderDate," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) as orderMoney," +
            "truncate(SUM(" + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + "),2) as receivedOrderMoney," +
            "truncate(SUM(" + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + ")/SUM(" + ProductionManageOrder.COL_ORDER_MONEY + ")*100,2) paybackRate  " +
            " from t_production_manage_order " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageOrderPayBackListVO> orderPaybackPageList(Page<ProductionManageSaleProductDataListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);

    /**
     * 汇款率查询
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            + "select DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) as orderMoney," +
            "truncate(SUM(" + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + "),2) as receivedOrderMoney," +
            "truncate(SUM(" + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + ")/SUM(OrderMoney)*100,2) paybackRate " +
            " from t_production_manage_order " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<Map<String, Object>> paybackLine(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);

    /**
     * 订单情况曲线图
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            + "select date_format(" + ProductionManageOrderDataByType.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate," +
            ProductionManageOrderDataByType.COL_ORDER_MONEY + "," + ProductionManageOrderDataByType.COL_ORDER_NUM + "," +
            ProductionManageOrderDataByType.COL_ORDER_QUANTITY + "," + ProductionManageOrderDataByType.COL_ORDER_PRODUCT_NUM + "," +
            ProductionManageOrderDataByType.COL_ORDER_WEIGHT + "," + ProductionManageOrderDataByType.COL_GG_PARTION_NUM +
            " from t_production_manage_order_data_by_type " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<Map<String, Object>> orderConditionCurve(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);

    @Select(START_SCRIPT
            + "select truncate(SUM(" + ProductionManageOrderDataByType.COL_ORDER_MONEY + "),2) as orderMoney," +
            "sum(" + ProductionManageOrderDataByType.COL_ORDER_NUM + ") orderNum," +
            "sum(" + ProductionManageOrderDataByType.COL_ORDER_QUANTITY + ") OrderQuantity," +
            " sum(" + ProductionManageOrderDataByType.COL_ORDER_PRODUCT_NUM + ") OrderProductNum," +
            "truncate(SUM(" + ProductionManageOrderDataByType.COL_ORDER_WEIGHT + "),2) as OrderWeight,type_value," +
            " sum(" + ProductionManageOrderDataByType.COL_GG_PARTION_NUM + ") ggPartionNum" +
            " from t_production_manage_order_data_by_type " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageOrderDataByTypeListVO> orderConditionPageList(Page<ProductionManageOrderDataByTypeListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);

    /**
     * 根据类型统计订单数据
     * @return
     */
    @Select(START_SCRIPT
            + "select " + ProductionManageOrder.COL_ORDER_STATUS + " AS typeValue ," + ProductionManageOrder.COL_ORGANIZATION_ID + "," +
            ProductionManageOrder.COL_SYS_ID + ",date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) as orderMoney," +
            "count(DISTINCT " + ProductionManageOrder.COL_ID + ") orderNum," +
            "sum(" + ProductionManageOrder.COL_ORDER_QUANTITY + ") OrderQuantity," +
            "sum(" + ProductionManageOrder.COL_ORDER_PRODUCT_NUM + ") OrderProductNum," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_WEIGHT + "),2) as OrderWeight," +
            "sum(" + ProductionManageOrder.COL_GG_TOTAL_PARTION_NUM + ") as ggPartionNum " +
            " from t_production_manage_order "
            + " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageOrderDataByType> syncOrderDataByTypeDataListByType(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);

    @Select(START_SCRIPT
            + "select " + ProductionManageOrder.COL_VERIFY_STATUS + " AS typeValue ," + ProductionManageOrder.COL_ORGANIZATION_ID + "," +
            ProductionManageOrder.COL_SYS_ID + ",date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_MONEY + "),2) as orderMoney," +
            "count(DISTINCT " + ProductionManageOrder.COL_ID + ") orderNum," +
            "sum(" + ProductionManageOrder.COL_ORDER_QUANTITY + ") OrderQuantity," +
            ".sum(" + ProductionManageOrder.COL_ORDER_PRODUCT_NUM + ") OrderProductNum," +
            "truncate(SUM(" + ProductionManageOrder.COL_ORDER_WEIGHT + "),2) as OrderWeight," +
            "sum(" + ProductionManageOrder.COL_GG_TOTAL_PARTION_NUM + ") as ggPartionNum " +
            " from t_production_manage_order "
            + " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageOrderDataByType> syncOrderDataByTypeDataListByVerifyStatus(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderDataByType> queryWrapper);
}
