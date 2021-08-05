package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockLossOperateTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockLossResponseVO;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.INVENTORY_LOSS;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InventoryLossConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.FIRST_STORAGE_DATE;

/**
 * <p>
 * 库存报损 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
public interface ProductionManageStockLossMapper extends CommonSql<ProductionManageStockLoss> {
    @Select(START_SCRIPT
            + "select * from t_production_manage_stock_loss"
            + START_WHERE
            + ProductionManageStockLoss.COL_ID + " in"
            + "<foreach item='item' collection='list' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + END_WHERE
            + END_SCRIPT)
    List<SearchStockLossResponseVO> listExcelByIds(List<? extends Serializable> ids);

    @Select(START_SCRIPT
            + "select truncate(SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + "),2) from t_production_manage_stock_loss"
            + " where " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + "=#{plantBatchId}"
            + END_SCRIPT)
    BigDecimal sumWeightByPlantBatchId(@Param("plantBatchId") String plantBatchId);

    BigDecimal getStockLossWeight(QueryWrapper<ProductionManageStockLoss> stockCheckWrapper);

    /**
     * 获取库存报损列表信息
     *
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStockLoss> list(SearchStockLossRequestDTO requestDTO, CommonUtil commonUtil) {
        Page<ProductionManageStockLoss> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStockLoss> queryWrapper = commonUtil.queryTemplate(ProductionManageStockLoss.class);
        String search = requestDTO.getSearch();
        // search为空则进行高级检索，否则进行普通检索
        if (StringUtils.isBlank(search)) {
            String[] damageInterval = LocalDateTimeUtil.substringDate(requestDTO.getDamageDate());
            String[] storageInterval = LocalDateTimeUtil.substringDate(requestDTO.getStorageDate());
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getBaseName()), OLD_BASE_NAME, requestDTO.getBaseName())
                    .eq(StringUtils.isNotBlank(requestDTO.getCreateUserName()), OLD_CREATE_USER_NAME, requestDTO.getCreateUserName())
                    .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), OLD_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                    .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), OLD_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductLevelName()), OLD_PRODUCT_LEVEL_NAME, requestDTO.getProductLevelName())
                    .ge(StringUtils.isNotBlank(damageInterval[0]), DAMAGE_DATE, damageInterval[0])
                    .le(StringUtils.isNotBlank(damageInterval[1]), DAMAGE_DATE, damageInterval[1])
                    .ge(StringUtils.isNotBlank(storageInterval[0]), FIRST_STORAGE_DATE, storageInterval[0])
                    .le(StringUtils.isNotBlank(storageInterval[1]), FIRST_STORAGE_DATE, LocalDateTimeUtil.addOneDay(storageInterval[1]))
                    .eq(StringUtils.isNotBlank(requestDTO.getProductName()), OLD_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(StringUtils.isNotBlank(requestDTO.getDepartmentName()), OLD_DEPARTMENT_NAME, requestDTO.getDepartmentName())
                    .eq(Objects.nonNull(requestDTO.getLossConfirmationStatus()), LOSS_CONFIRMATION_STATUS, requestDTO.getLossConfirmationStatus())
                    .eq(Objects.nonNull(requestDTO.getLossConfirmationStatus()), OPERATE_TYPE, StockLossOperateTypeEnum.STOCK_LOSS.getKey());
        } else {
            queryWrapper.and(wrapper -> wrapper.or().like(OLD_BASE_NAME, search)
                    .or().like(OLD_CREATE_USER_NAME, search)
                    .or().like(OLD_GREENHOUSE_NAME, search)
                    .or().like(OLD_PLANT_BATCH_NAME, search)
                    .or().like(OLD_PRODUCT_LEVEL_NAME, search)
                    .or().like(OLD_PRODUCT_NAME, search)
                    .or().like(OLD_DEPARTMENT_NAME, search));
        }


        // 添加数据权限
        commonUtil.roleDataAuthFilter(INVENTORY_LOSS, queryWrapper, OLD_CREATE_USER_ID, StringUtils.EMPTY);

        queryWrapper.orderByDesc(DAMAGE_DATE);

        return selectPage(page, queryWrapper);
    }


    /**
     * 统计所有库存报损数据
     * 按照批次类型 + 报损时间 + 批次id + 产品id进行分组
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-11
     * @updateDate 2019-12-11
     * @updatedBy shixiongfei
     */
    @Select("SELECT " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + ", " + ProductionManageStockLoss.COL_PLANT_BATCH_NAME + ", " +
            ProductionManageStockLoss.COL_PRODUCT_ID + ", " + ProductionManageStockLoss.COL_PRODUCT_NAME + ", " +
            "IFNULL(SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + "), 0) AS damageWeight, " +
            ProductionManageStockLoss.COL_TYPE + ", " +
            "DATE_FORMAT(" + ProductionManageStockLoss.COL_DAMAGE_DATE + ", '%Y-%m-%d') AS damageDate " +
            "FROM t_production_manage_stock_loss " +
            "WHERE " + ProductionManageStockLoss.COL_SYS_ID + " = #{sysId} " +
            "AND " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "AND " + ProductionManageStockLoss.COL_LOSS_CONFIRMATION_STATUS + " = #{confirmStatus} " +
            "GROUP BY " + ProductionManageStockLoss.COL_DAMAGE_DATE + ", " + ProductionManageStockLoss.COL_TYPE + ", " +
            ProductionManageStockLoss.COL_PLANT_BATCH_ID + ", " + ProductionManageStockLoss.COL_PRODUCT_ID)
    List<ProductionManageStockLoss> listAllStatisticsMsg(@Param("sysId") String sysId, @Param("organizationId") String organizationId, @Param("confirmStatus") Byte confirmStatus);


    @Select("SELECT IFNULL(SUM(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + "), 0) FROM t_production_manage_stock_loss " +
            "WHERE " + ProductionManageStockLoss.COL_SYS_ID + " = #{sysId} " +
            "AND " + ProductionManageStockLoss.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "AND " + ProductionManageStockLoss.COL_PLANT_BATCH_ID + " = #{plantBatchId} " +
            "AND " + ProductionManageStockLoss.COL_LOSS_CONFIRMATION_STATUS + " = #{confirmStatus}")
    BigDecimal getWeightByBatchId(@Param("plantBatchId") String plantBatchId, @Param("sysId") String sysId,
                                  @Param("organizationId") String organizationId, @Param("confirmStatus") Byte confirmStatus);
}