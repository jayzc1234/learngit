package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.model.storagemanage.StockListOther;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductLevelRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductionManageStockListDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageInventoryWarning;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPLSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPLSSResponseVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryErrorMsgConstants.UPDATE_INVENTORY_MSG_FAILED;


/**
 * <p>
 * 库存表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
public interface ProductionManageStockMapper extends CommonSql<ProductionManageStock> {

    Logger log = LoggerFactory.getLogger(ProductionManageStockMapper.class);


    @Select(START_SCRIPT +
            "select sum(" + ProductionManageStock.COL_QUANTITY + ") quantity,sum(" + ProductionManageStock.COL_WEIGHT + ") weight " +
            " from t_production_manage_stock " +
            START_WHERE
            + "<if test = 'productId != null and productId != &apos;&apos; '>  " + ProductionManageStock.COL_PRODUCT_ID + " = #{productId}</if>"
            + "<if test = 'productLevelCode != null and productLevelCode != &apos;&apos; '> AND " + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " = #{productLevelCode}</if>"
            + "<if test = 'organizationId != null and organizationId != &apos;&apos; '> AND " + ProductionManageStock.COL_ORGANIZATION_ID + " = #{organizationId}</if>"
            + "<if test = 'sysId != null and sysId != &apos;&apos; '> AND " + ProductionManageStock.COL_SYS_ID + " = #{sysId}</if>"
            + END_WHERE
            + END_SCRIPT)
    ProductionManageStock sumByProductIdAndLevel(@Param("productId") String productId, @Param("productLevelCode") String productLevelCode, @Param("organizationId") String organizationId, @Param("sysId") String sysId);

//    @Select(START_SCRIPT +
//            "select sum(BoxNum) boxNum,sum(Quantity) quantity,sum(Weight) weight, " +
//            "sum(TotalInboundBoxNum) as inboundBoxNum, sum(TotalInboundQuantity) as inboundQuantity, sum(TotalInboundWeight) as inboundWeight, " +
//            "sum(TotalOutboundBoxNum) as outboundBoxNum, sum(TotalOutboundQuantity) as outboundQuantity, sum(TotalOutboundWeight) as outboundWeight " +
//            " from t_production_manage_stock " +
//            START_WHERE
//            + "<if test='baseName != null and baseName != &apos;&apos;'> BaseName = #{baseName}</if>"
//            + "<if test = 'createUserName != null and createUserName != &apos;&apos; '> AND CreateUserName = #{createUserName}</if>"
//            + "<if test = 'greenhouseName != null and greenhouseName != &apos;&apos; '> AND GreenhouseName = #{greenhouseName}</if>"
//            + "<if test = 'plantBatchName != null and plantBatchName != &apos;&apos; '> AND PlantBatchName = #{plantBatchName}</if>"
//            + "<if test = 'productLevelName != null and productLevelName != &apos;&apos; '> AND ProductLevelName = #{productLevelName}</if>"
//            + "<if test = 'productName != null and productName != &apos;&apos; '> AND ProductName = #{productName}</if>"
//            + "<if test = 'organizationId != null and organizationId != &apos;&apos; '> AND OrganizationId = #{organizationId}</if>"
//            + "<if test = 'sysId != null and sysId != &apos;&apos; '> AND SysId = #{sysId}</if>"
//            + " ${authFilter} "
//            + END_WHERE
//            + END_SCRIPT)

    @Select("SELECT IFNULL(SUM(" + ProductionManageStock.COL_BOX_NUM + "), 0) AS boxNum, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_QUANTITY + "), 0) AS quantity, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_WEIGHT + "), 0) AS weight, IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_INBOUND_BOX_NUM + "), 0) AS inboundBoxNum, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_INBOUND_QUANTITY + "), 0) AS inboundQuantity, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_INBOUND_WEIGHT + "), 0) AS inboundWeight, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_OUTBOUND_BOX_NUM + "), 0) AS outboundBoxNum, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_OUTBOUND_QUANTITY + "), 0) AS outboundQuantity, " +
            "IFNULL(SUM(" + ProductionManageStock.COL_TOTAL_OUTBOUND_WEIGHT + "), 0) AS outboundWeight " +
            "FROM t_production_manage_stock ${ew.customSqlSegment}")
    StockListOther listStatistics(@Param(Constants.WRAPPER) Wrapper queryWrapper);


    /**
     * 获取产品等级库存信息列表
     *
     * @param page         分页参数
     * @param queryWrapper sql包装器
     * @return
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     */
    @Select("SELECT s." + ProductionManageStock.COL_PRODUCT_ID + " AS productId, s." + ProductionManageStock.COL_PRODUCT_NAME + " AS productName, " +
            "s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " AS productLevelCode, " +
            "s." + ProductionManageStock.COL_PRODUCT_LEVEL_NAME + " AS productLevelName, " +
            "s." + ProductionManageStock.COL_PRODUCT_SPEC_CODE + " AS productSpecCode, " +
            "s." + ProductionManageStock.COL_PRODUCT_SPEC_NAME + " AS productSpecName, " +
            "s." + ProductionManageStock.COL_SORTING_SPEC_CODE + " AS sortingSpecCode, " +
            "s." + ProductionManageStock.COL_SORTING_SPEC_NAME + " AS sortingSpecName, " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_BOX_NUM + "), 0) AS boxNum, " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_QUANTITY + "), 0) AS quantity, " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_WEIGHT + "), 0) AS weight, " +
            "w." + ProductionManageInventoryWarning.COL_DEPARTMENT_ID + " AS departmentId, " +
            "w." + ProductionManageInventoryWarning.COL_DEPARTMENT_NAME + " AS DepartmentName, " +
            "w." + ProductionManageInventoryWarning.COL_WARNING_BOX_NUM + " AS warningBoxNum," +
            " w." + ProductionManageInventoryWarning.COL_WARNING_QUANTITY + " AS warningQuantity, " +
            "w." + ProductionManageInventoryWarning.COL_WARNING_WEIGHT + " AS warningWeight " +
            "FROM t_production_manage_stock s " +
            " LEFT JOIN t_production_manage_inventory_warning w " +
            " ON w." + ProductionManageInventoryWarning.COL_PRODUCT_ID + " = s." + ProductionManageStock.COL_PRODUCT_ID +
            " AND w." + ProductionManageInventoryWarning.COL_PRODUCT_LEVEL_CODE + " = s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " ${ew.customSqlSegment} ")
    IPage<SearchPLSResponseVO> listWithProductLevel(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取产品等级库存信息列表
     *
     * @param requestDTO     请求体
     * @param sysId          系统id
     * @param organizationId 组织id
     * @return
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     */
    default IPage<SearchPLSResponseVO> listWithProductLevel(ProductLevelRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStock> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStock> queryWrapper = getWrapper(requestDTO, sysId, organizationId);
        queryWrapper.groupBy("s." + ProductionManageStock.COL_PRODUCT_ID, "s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE)
                .orderByDesc("s." + ProductionManageStock.COL_UPDATE_DATE);

        return listWithProductLevel(page, queryWrapper);
    }

    /**
     * 获取产品等级库存sql包装器
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    default QueryWrapper<ProductionManageStock> getWrapper(ProductLevelRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStock> queryWrapper = new QueryWrapper<>();
        String search = requestDTO.getSearch();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), "s." + ProductionManageStock.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "s." + ProductionManageStock.COL_ORGANIZATION_ID, organizationId)
                // 这里做的处理是为了保证数据最终的准确性，防止出现数据串行，这种操作会极大的降低查询性能
                .and(wrapper -> wrapper
                        .and(condition -> condition
                                .eq(StringUtils.isNotBlank(sysId), "w." + ProductionManageInventoryWarning.COL_SYS_ID, sysId)
                                .eq(StringUtils.isNotBlank(organizationId), "w." + ProductionManageInventoryWarning.COL_ORGANIZATION_ID, organizationId))
                        .or(condition -> condition.isNull("w." + ProductionManageInventoryWarning.COL_SYS_ID).isNull("w." + ProductionManageInventoryWarning.COL_ORGANIZATION_ID)));

        if (StringUtils.isBlank(search)) {
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getProductName()), "s." + ProductionManageStock.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(Objects.nonNull(requestDTO.getProductLevelName()), "s." + ProductionManageStock.COL_PRODUCT_LEVEL_NAME, requestDTO.getProductLevelName());
        } else {
            queryWrapper.and(wrapper ->
                    wrapper.or().like("s." + ProductionManageStock.COL_PRODUCT_NAME, search)
                            .or().like("s." + ProductionManageStock.COL_PRODUCT_LEVEL_NAME, search));
        }

        return queryWrapper;
    }

    /**
     * 获取产品等级库存数值统计数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    default SearchPLSSResponseVO listPLStatistics(ProductLevelRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStock> queryWrapper = getWrapper(requestDTO, sysId, organizationId);
        return listPLStatistics(queryWrapper);
    }

    @Select("SELECT " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_BOX_NUM + "), 0) AS totalBoxNum, " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_QUANTITY + "), 0) AS totalQuantity, " +
            "IFNULL(SUM(s." + ProductionManageStock.COL_WEIGHT + "), 0) AS totalWeight " +
            " FROM t_production_manage_stock s " +
            " LEFT JOIN t_production_manage_inventory_warning w " +
            " ON w." + ProductionManageInventoryWarning.COL_PRODUCT_ID + " = s." + ProductionManageStock.COL_PRODUCT_ID +
            " AND w." + ProductionManageInventoryWarning.COL_PRODUCT_LEVEL_CODE + " = s." + ProductionManageStock.COL_PRODUCT_LEVEL_CODE + " ${ew.customSqlSegment} ")
    SearchPLSSResponseVO listPLStatistics(@Param(Constants.WRAPPER) Wrapper queryWrapper);


    /**
     * 获取产品库存列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    default IPage<ProductionManageStock> list(ProductionManageStockListDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStock> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStock> queryWrapper = getWrapper(requestDTO, sysId, organizationId);

        queryWrapper.orderByDesc(OLD_ID);
        return selectPage(page, queryWrapper);
    }

    /**
     * 获取批次库存总数值信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-13
     * @updateDate 2019-12-13
     * @updatedBy shixiongfei
     */
    default StockListOther listStatistics(ProductionManageStockListDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStock> queryWrapper = getWrapper(requestDTO, sysId, organizationId);

        return listStatistics(queryWrapper);
    }

    /**
     * 获取产品库存sql包装器
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    default QueryWrapper<ProductionManageStock> getWrapper(ProductionManageStockListDTO stockListDTO, String sysId, String organizationId) {

        QueryWrapper<ProductionManageStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId);

        String search = stockListDTO.getSearch();
        if (StringUtils.isBlank(search)) {
            String[] lossInterval = LocalDateTimeUtil.substringDate(stockListDTO.getCreateDate());
            queryWrapper.eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getBaseName()), OLD_BASE_NAME, stockListDTO.getBaseName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getCreateUserName()), OLD_CREATE_USER_NAME, stockListDTO.getCreateUserName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getGreenhouseName()), OLD_GREENHOUSE_NAME, stockListDTO.getGreenhouseName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getPlantBatchName()), OLD_PLANT_BATCH_NAME, stockListDTO.getPlantBatchName())
                    .ge(org.apache.commons.lang.StringUtils.isNotBlank(lossInterval[0]), OLD_CREATE_DATE, lossInterval[0])
                    .le(org.apache.commons.lang.StringUtils.isNotBlank(lossInterval[1]), OLD_CREATE_DATE, lossInterval[1])
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getProductLevelName()), OLD_PRODUCT_LEVEL_NAME, stockListDTO.getProductLevelName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(stockListDTO.getProductName()), OLD_PRODUCT_NAME, stockListDTO.getProductName());
        } else {
            queryWrapper.and(wrapper ->
                    wrapper.like(OLD_BASE_NAME, search)
                            .or().like(OLD_CREATE_USER_NAME, search)
                            .or().like(OLD_GREENHOUSE_NAME, search)
                            .or().like(OLD_PLANT_BATCH_NAME, search)
                            .or().like(OLD_PRODUCT_LEVEL_NAME, search)
                            .or().like(OLD_PRODUCT_NAME, search)
            );
        }

        return queryWrapper;
    }

    /**
     * 更新库存
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     */
    default void update(ProductionManageStock stock, Integer updateBoxNum, Integer updateNum, BigDecimal updateWeight) {
        log.info("[DAO]编辑包装信息时,进行更新库存入参 => 库存信息:{}, 更新箱数:{}, 更新个数:{}, 更新重量:{}", stock, updateBoxNum, updateNum, updateWeight);
        UpdateWrapper<ProductionManageStock> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(OLD_QUANTITY, Optional.of(stock.getQuantity() + updateNum).filter(num -> num >= 0).orElse(0))
                .set(OLD_BOX_NUM, Optional.of(stock.getBoxNum() - updateBoxNum).filter(num -> num >= 0).orElse(0))
                .set(OLD_WEIGHT, Optional.of(stock.getWeight().subtract(updateWeight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                .set(TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + updateBoxNum)
                .set(TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + updateNum)
                .set(TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(updateWeight))
                // 这里采用乐观锁的方式来处理，主要是为了防止并发情况下造成最终数据不一致
                // 这里的数值信息不可以为空，所以需要在新增库存时为其参数授予默认值，
                // 数据库表中并未设置默认值，因此可能会出现这种问题
                .eq(OLD_QUANTITY, stock.getQuantity())
                .eq(OLD_BOX_NUM, stock.getBoxNum())
                .eq(OLD_WEIGHT, stock.getWeight())
                .eq(OLD_ID, stock.getId());
        CustomAssert.greaterThanOne2Error(update(null, updateWrapper), UPDATE_INVENTORY_MSG_FAILED);
    }

}