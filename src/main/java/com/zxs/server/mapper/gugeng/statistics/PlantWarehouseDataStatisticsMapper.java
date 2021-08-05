package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.gugeng.InboundMessageBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OutboundMessageBO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.*;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchPlantBatchResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 种植仓储数据统计mapper
 * @author shixiongfei
 * @date 2019-09-22
 * @since v1.3
 */
public interface PlantWarehouseDataStatisticsMapper extends BaseMapper {

    /**
     * 获取种植批次信息列表
     *
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Select("SELECT DISTINCT " +
            "s." + ProductionManageStock.COL_PLANT_BATCH_ID + " AS plantBatchId, s." + ProductionManageStock.COL_PLANT_BATCH_NAME + " AS plantBatchName " +
            "FROM t_production_manage_stock s " +
            "INNER JOIN t_production_manage_sort_instorage si " +
            "ON si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = s." + ProductionManageStock.COL_PLANT_BATCH_ID + " AND si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " = s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " ${ew.customSqlSegment}"
    )
    List<SearchPlantBatchResponseVO> listByPlantBatchId(@Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 通过种植批次id获取分拣入库相关信息
     *
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Select("SELECT " +
            "si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " AS plantBatchId, si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + " AS plantBatchName, " +
            "si." + ProductionManageSortInstorage.COL_GREENHOUSE_ID + " AS greenhouseId, si." + ProductionManageSortInstorage.COL_GREENHOUSE_NAME + " AS greenhouseName, " +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " AS productLevelCode, si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME + " AS productLevelName, " +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE + " AS productSpecCode, " +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_NAME + " AS productSpecName, si." + ProductionManageSortInstorage.COL_SORTING_SPEC_CODE + " AS sortingSpecCode, " +
            "si." + ProductionManageSortInstorage.COL_SORTING_SPEC_NAME + " AS sortingSpecName, " +
            // 如果为外采批次，则获取外采批次的采收时间, 种植批次的采收时间从溯源信息中获取，不从古耕的入库中获取
            "(" +
            "SELECT GROUP_CONCAT(DISTINCT DATE_FORMAT(s." + ProductionManageSortInstorage.COL_HARVEST_DATE + ",'%Y-%m-%d')) " +
            "FROM t_production_manage_sort_instorage s " +
            "WHERE s." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND s." + ProductionManageSortInstorage.COL_TYPE + " IN (0, 2) " +
            "AND s." + ProductionManageSortInstorage.COL_SYS_ID + " = #{sysId} AND s." + ProductionManageSortInstorage.COL_ORGANIZATION_ID + " = #{organizationId}" +
            ") AS harvestDate, " +

            // 获取入库时间
            "(" +
            "SELECT GROUP_CONCAT(DISTINCT DATE_FORMAT(so." + ProductionManageSortInstorage.COL_CREATE_DATE + ",'%Y-%m-%d')) " +
            "FROM t_production_manage_sort_instorage so " +
            "WHERE so." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND so." + ProductionManageSortInstorage.COL_SYS_ID + " = #{sysId} " +
            "AND so." + ProductionManageSortInstorage.COL_ORGANIZATION_ID + " = #{organizationId}" +
            ") AS sortingInventoryDate, " +

            // 获取入库天数
            "(" +
            "SELECT DATEDIFF(NOW(), i." + ProductionManageSortInstorage.COL_CREATE_DATE + ") FROM t_production_manage_sort_instorage i " +
            "WHERE i." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND i." + ProductionManageSortInstorage.COL_SYS_ID + " = #{sysId} " +
            "AND i." + ProductionManageSortInstorage.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "AND i." + ProductionManageSortInstorage.COL_TYPE + " = si." + ProductionManageSortInstorage.COL_TYPE + " " +
            "and i." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " = si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE +
            "ORDER BY i." + ProductionManageSortInstorage.COL_CREATE_DATE + " ASC LIMIT 1 " +
            ") AS inboundDays, " +

            // 获取批次类型 1 => 种植批次 2 => 外采批次
            "IF(si." + ProductionManageSortInstorage.COL_TYPE + " = 1 or si." + ProductionManageSortInstorage.COL_TYPE + " = 3, 1, 2) AS plantBatchType " +
            "FROM t_production_manage_sort_instorage si " +
            "WHERE si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND si." + ProductionManageSortInstorage.COL_SYS_ID + " = #{sysId} " +
            "AND si." + ProductionManageSortInstorage.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "GROUP BY si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE)
    List<InboundMessageBO> getSortingStorageMessage(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId, @Param("organizationId") String organizationId);


    /**
     * 1 => 获取总入库数值信息
     * 2 => 今日包装出库数值信息
     * 3 => 获取总出库数值信息
     * 4 => 今日库存报损数值信息
     * 5 => 库存数值信息
     * 6 => 今日盘点出库数值信息
     * 7 => 今日换箱数值信息
     * 8 => 今日箱子报损的数值信息
     * 9 => 今日空余箱子处理的数值信息
     * 10 => 今日退货入库数值信息
     *
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Select({
            // 获取总入库数值信息
            "SELECT 1 AS sorting, IFNULL(" + ProductionManageStock.COL_TOTAL_INBOUND_BOX_NUM + ", 0) AS outboundBoxNum, IFNULL(" + ProductionManageStock.COL_TOTAL_INBOUND_QUANTITY + ", 0) AS outboundQuantity, " +
                    "IFNULL(" + ProductionManageStock.COL_TOTAL_INBOUND_WEIGHT + ", 0) AS outboundWeight FROM production_manage_stock " +
                    "WHERE " + ProductionManageStock.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStock.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStock.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "UNION " +
                    // 获取当日包装数值信息
                    "SELECT 2 AS sorting, IFNULL(SUM(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_BOX_NUM + "), 0) AS outboundBoxNum, " +
                    "IFNULL(SUM(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_NUM + "), 0) AS outboundQuantity, " +
                    "IFNULL(SUM(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_WEIGHT + "), 0) AS outboundWeight FROM production_manage_outbound_package_message opm " +
                    " INNER JOIN t_production_manage_outbound o ON o." + ProductionManageOutbound.COL_ID + " = opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_ID +
                    " WHERE opm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID + " = #{plantBatchId} " +
                    "AND opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    " AND o." + ProductionManageOutbound.COL_SYS_ID + " = #{sysId} AND o." + ProductionManageOutbound.COL_ORGANIZATION_ID + " = #{organizationId} AND DATE_FORMAT(opm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE + ", '%Y-%m-%d') = #{now} " +
                    "AND opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM + " > 0 " +
                    "UNION " +
                    // 获取总出库数值信息
                    "SELECT 3 AS sorting, IFNULL(" + ProductionManageStock.COL_TOTAL_INBOUND_BOX_NUM + ", 0) AS outboundBoxNum," +
                    " IFNULL(" + ProductionManageStock.COL_TOTAL_OUTBOUND_QUANTITY + ", 0) AS outboundQuantity, " +
                    "IFNULL(" + ProductionManageStock.COL_TOTAL_OUTBOUND_WEIGHT + ", 0) AS outboundWeight FROM t_production_manage_stock " +
                    "WHERE " + ProductionManageStock.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStock.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStock.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "UNION " +
                    // 获取当日库存报损数值信息
                    "SELECT 4 AS sorting, IFNULL(SUM(" + ProductionManageStockLoss.COL_HANDLE_BOX_NUM + "), 0) AS outboundBoxNum, " +
                    "IFNULL(SUM(" + ProductionManageStockLoss.COL_DAMAGE_NUM + "), 0) AS outboundQuantity, " +
                    "IFNULL(SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + "), 0) AS outboundWeight FROM t_production_manage_stock_loss " +
                    "WHERE " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStockLoss.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStockLoss.COL_LOSS_CONFIRMATION_STATUS + " = 1 " +
                    "AND " + ProductionManageStockLoss.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "AND " + ProductionManageStockLoss.COL_OPERATE_TYPE + " = 1 AND DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ", '%Y-%m-%d') = #{now} " +
                    "UNION " +
                    // 获取库存数值信息
                    "SELECT 5 AS sorting, IFNULL(" + ProductionManageStock.COL_BOX_NUM + ", 0) AS outboundBoxNum, IFNULL(" + ProductionManageStock.COL_QUANTITY + ", 0) AS outboundQuantity, " +
                    "IFNULL(" + ProductionManageStock.COL_WEIGHT + ", 0) AS outboundWeight FROM t_production_manage_stock " +
                    "WHERE " + ProductionManageStock.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStock.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStock.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "UNION " +
                    // 获取今日库存盘点出库数值信息（通过库存流水来查询）
                    "SELECT 6 AS sorting, IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_BOX_NUM + "),0) AS outboundBoxNum," +
                    " IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_NUM + "), 0) AS outboundQuantity, " +
                    "IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_WEIGHT + "),0) AS outboundWeight FROM t_production_manage_stock_flow_details " +
                    "WHERE " + ProductionManageStockFlowDetails.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStockFlowDetails.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE + " = 4 AND " + ProductionManageStockFlowDetails.COL_SYS_ID + " = #{sysId} " +
                    "AND " + ProductionManageStockFlowDetails.COL_ORGANIZATION_ID + " = #{organizationId} AND DATE_FORMAT(" + ProductionManageStockFlowDetails.COL_OUT_IN_DATE + ", '%Y-%m-%d') = #{now} " +
                    "UNION " +
                    // 获取今日换箱数值信息（通过换箱来查询）
                    "SELECT 7 AS sorting, IF(" + ProductionManagePackageBox.COL_NEW_BOX_NUMBER + " - " + ProductionManagePackageBox.COL_OLD_BOX_NUMBER + " < 0, " +
                    "ABS(" + ProductionManagePackageBox.COL_NEW_BOX_NUMBER + " - " + ProductionManagePackageBox.COL_OLD_BOX_NUMBER + "), 0) AS outboundBoxNum, " +
                    "0 AS outboundQuantity, 0 AS outboundWeight FROM t_production_manage_package_box WHERE " + ProductionManagePackageBox.COL_PLANT_BATCH_ID + " = #{plantBatchId} " +
                    "AND " + ProductionManagePackageBox.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManagePackageBox.COL_TYPE + " = 1 AND DATE_FORMAT(" + ProductionManagePackageBox.COL_CREATE_DATE + ", '%Y-%m-%d') = #{now} " +
                    "UNION " +
                    // 获取今日箱子报损的数值信息（通过库存报损来查询）
                    "SELECT 8 AS sorting, IFNULL(SUM(" + ProductionManageStockLoss.COL_HANDLE_BOX_NUM + "), 0) AS outboundBoxNum, 0 AS outboundQuantity, 0 AS outboundWeight " +
                    "FROM t_production_manage_stock_loss " +
                    "WHERE " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStockLoss.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStockLoss.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "AND " + ProductionManageStockLoss.COL_OPERATE_TYPE + " = 3 AND DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ", '%Y-%m-%d') = #{now} " +
                    "UNION " +
                    // 获取今日空余箱子的数值信息（通过库存报损来查询）
                    "SELECT 9 AS sorting, IFNULL(SUM(" + ProductionManageStockLoss.COL_HANDLE_BOX_NUM + "), 0) AS outboundBoxNum, 0 AS outboundQuantity, 0 AS outboundWeight " +
                    "FROM t_production_manage_stock_loss " +
                    "WHERE " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStockLoss.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStockLoss.COL_SYS_ID + " = #{sysId} AND " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " = #{organizationId} " +
                    "AND " + ProductionManageStockLoss.COL_OPERATE_TYPE + " = 2 AND DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ", '%Y-%m-%d') = #{now} " +
                    "UNION " +
                    // 获取今日退货入库数值信息（通过库存流水来查询）
                    "SELECT 10 AS sorting, IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_BOX_NUM + "),0) AS outboundBoxNum, " +
                    "IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_NUM + "), 0) AS outboundQuantity, " +
                    "IFNULL(SUM(" + ProductionManageStockFlowDetails.COL_OUT_IN_WEIGHT + "),0) AS outboundWeight FROM t_production_manage_stock_flow_details " +
                    "WHERE " + ProductionManageStockFlowDetails.COL_PLANT_BATCH_ID + " = #{plantBatchId} AND " + ProductionManageStockFlowDetails.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode} " +
                    "AND " + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE + " = 9 AND " + ProductionManageStockFlowDetails.COL_SYS_ID + " = #{sysId} " +
                    "AND " + ProductionManageStockFlowDetails.COL_ORGANIZATION_ID + " = #{organizationId} AND DATE_FORMAT(" + ProductionManageStockFlowDetails.COL_OUT_IN_DATE + ", '%Y-%m-%d') = #{now} "
    })
    List<OutboundMessageBO> getOutboundMessage(@Param("plantBatchId") String plantBatchId, @Param("productLevelCode") String productLevelCode,
                                               @Param("now") String now, @Param("sysId") String sysId, @Param("organizationId") String organizationId);

    /**
     * 通过批次id获取非商品产品总重量
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    @Select("SELECT IFNULL(SUM(" + ProductionManageNotProductStorage.COL_STORAGE_WEIGHT + "), 0) FROM " + ProductionManageNotProductStorage.TABLE_NAME +
            " WHERE " + ProductionManageNotProductStorage.COL_BATCH_ID + " = #{plantBatchId} " +
            "AND " + ProductionManageNotProductStorage.COL_BATCH_TYPE + " = 1 " +
            "AND " + ProductionManageNotProductStorage.COL_SYS_ID + " = #{sysId} " +
            ".AND " + ProductionManageNotProductStorage.COL_ORGANIZATION_ID + " = #{organizationId}")
    BigDecimal getNonProductWeight(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId, @Param("organizationId") String organizationId);


    /**
     * 获取批次信息
     *
     * @author shixiongfei
     * @date 2019-09-23
     * @updateDate 2019-09-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<SearchPlantBatchResponseVO> listByPlantBatchId(String search, String sysId, String organizationId) {
        QueryWrapper<ProductionManageSortInstorage> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), "si." + ProductionManageSortInstorage.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "si." + ProductionManageSortInstorage.COL_ORGANIZATION_ID, organizationId)
                .like(StringUtils.isNotBlank(search), "si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME, search)
                .in("si." + ProductionManageSortInstorage.COL_TYPE, 1, 3)
                .and(query -> query.gt("s." + ProductionManageStock.COL_BOX_NUM, 0)
                        .or().gt("s." + ProductionManageStock.COL_QUANTITY, 0)
                        .or().gt("s." + ProductionManageStock.COL_WEIGHT, 0))
                .groupBy("s." + ProductionManageStock.COL_PLANT_BATCH_ID,
                        "s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE)
                .orderByDesc("si." + ProductionManageSortInstorage.COL_CREATE_DATE);

        return listByPlantBatchId(wrapper);
    }
}