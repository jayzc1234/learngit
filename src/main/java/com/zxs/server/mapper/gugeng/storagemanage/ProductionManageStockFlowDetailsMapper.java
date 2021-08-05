package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.gugeng.FlowDetailBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockFlowRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.FlowDirectionTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ORGANIZATION_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_SYS_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InventoryFlowConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryFlowErrorMsgConstants.UPDATE_INVENTORY_FLOW_FAILED;

/**
 * <p>
 * 库存流水详情表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
public interface ProductionManageStockFlowDetailsMapper extends BaseMapper<ProductionManageStockFlowDetails> {



    Logger log = LoggerFactory.getLogger(ProductionManageStockFlowDetailsMapper.class);

    /**
     * 获取全部库存流水信息列表
     *
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     * @return
     */
    @Select("SELECT \n" +
            "si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " AS plantBatchId, si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + " AS plantBatchName, \n" +
            "si." + ProductionManageSortInstorage.COL_BASE_ID + " AS baseId,si." + ProductionManageSortInstorage.COL_BASE_NAME + " AS baseName, \n" +
            "si." + ProductionManageSortInstorage.COL_GREENHOUSE_ID + " AS greenhouseId, si." + ProductionManageSortInstorage.COL_GREENHOUSE_NAME + " AS greenhouseName,\n" +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_ID + " AS productId, si." + ProductionManageSortInstorage.COL_PRODUCT_NAME + " AS productName,\n" +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " AS productLevelCode, si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME + " AS productLevelName, \n" +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE + " AS productSpecCode, si." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_NAME + " AS productSpecName, \n" +
            "si." + ProductionManageSortInstorage.COL_SORTING_SPEC_CODE + " AS sortingSpecCode, si." + ProductionManageSortInstorage.COL_SORTING_SPEC_NAME + " AS sortingSpecName, \n" +
            "si." + ProductionManageSortInstorage.COL_TYPE + " AS sortingType, d." + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE + " AS outInType, \n" +
            "IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_BOX_NUM + ", 0) AS outInBoxNum, " +
            "IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_NUM + ", 0) AS outInNum," +
            " IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_WEIGHT + ", 0)  AS outInWeight, \n" +
            "d." + ProductionManageStockFlowDetails.COL_OUT_IN_DATE + " AS outInDate, " +
            "d." + ProductionManageStockFlowDetails.COL_CREATE_USER_ID + " AS createUserId, " +
            "d." + ProductionManageStockFlowDetails.COL_CREATE_USER_NAME + " AS createUserName,  " +
            "d." + ProductionManageStockFlowDetails.COL_BUSINESS_ID + " AS businessId \n" +
            "FROM t_production_manage_stock_flow_details d\n" +
            "INNER JOIN \n" +
            "( \n" +
            "SELECT " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", " + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + ", " +
            ProductionManageSortInstorage.COL_BASE_ID + "," + ProductionManageSortInstorage.COL_BASE_NAME + " ," +
            ProductionManageSortInstorage.COL_GREENHOUSE_ID + " ," + ProductionManageSortInstorage.COL_GREENHOUSE_NAME + ", " +
            ProductionManageSortInstorage.COL_PRODUCT_ID + "," + ProductionManageSortInstorage.COL_PRODUCT_NAME + ", " +
            ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + ", " + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME + ", " + ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE + ", " + ProductionManageSortInstorage.COL_PRODUCT_SPEC_NAME + ", \n" +
            ProductionManageSortInstorage.COL_SORTING_SPEC_CODE + ", " + ProductionManageSortInstorage.COL_SORTING_SPEC_NAME + ", " + ProductionManageSortInstorage.COL_TYPE +
            " FROM t_production_manage_sort_instorage GROUP BY " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", " + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE +
            ") si ON si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = d." + ProductionManageStockFlowDetails.COL_PLANT_BATCH_ID + " AND si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " = d." + ProductionManageStockFlowDetails.COL_PRODUCT_LEVEL_CODE + " ${ew.customSqlSegment}")
    IPage<FlowDetailBO> listAll(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select("SELECT \n" +
            "d." + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE + " AS outInType, \n" +
            "IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_BOX_NUM + ", 0) AS outInBoxNum, IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_NUM + ", 0) AS outInNum, " +
            "IFNULL(d." + ProductionManageStockFlowDetails.COL_OUT_IN_WEIGHT + ", 0)  AS outInWeight, d." + ProductionManageStockFlowDetails.COL_BUSINESS_ID + " AS businessId \n" +
            " FROM t_production_manage_stock_flow_details d\n" +
            " INNER JOIN \n" +
            "( \n" +
            "SELECT " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", " + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + ", " +
            ProductionManageSortInstorage.COL_BASE_ID + ", " + ProductionManageSortInstorage.COL_BASE_NAME + ", " + ProductionManageSortInstorage.COL_GREENHOUSE_ID + ", " +
            ProductionManageSortInstorage.COL_GREENHOUSE_NAME + ", " + ProductionManageSortInstorage.COL_PRODUCT_ID + ", " + ProductionManageSortInstorage.COL_PRODUCT_NAME + "," +
            ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + ", " + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME + ", " +
            ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE + ", " + ProductionManageSortInstorage.COL_PRODUCT_SPEC_NAME + ", \n" +
            ProductionManageSortInstorage.COL_SORTING_SPEC_CODE + ", " + ProductionManageSortInstorage.COL_SORTING_SPEC_NAME + ", " + ProductionManageSortInstorage.COL_TYPE +
            " FROM t_production_manage_sort_instorage GROUP BY " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", " + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE +
            ") si ON si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " = d." + ProductionManageStockFlowDetails.COL_PLANT_BATCH_ID +
            " AND si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " = d." + ProductionManageStockFlowDetails.COL_PRODUCT_LEVEL_CODE + " ${ew.customSqlSegment}")
    List<ProductionManageStockFlowDetails> list(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取sql包装器
     *
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStockFlowDetails> getWrapper(SearchStockFlowRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStockFlowDetails> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), "d." + ProductionManageStockFlowDetails.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "d." + ProductionManageStockFlowDetails.COL_ORGANIZATION_ID, organizationId)
                // 过滤掉类型为null的数据
                .isNotNull("d." + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE);

        String search = requestDTO.getSearch();
        if (StringUtils.isNotBlank(search)) {
            queryWrapper.like("si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME, search);
        } else {
            String[] outInInterval = LocalDateTimeUtil.substringDate(requestDTO.getOutInDate());
            queryWrapper.eq(Objects.nonNull(requestDTO.getOutInType()), "d." + ProductionManageStockFlowDetails.COL_OUT_IN_TYPE, requestDTO.getOutInType())
                    .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), "si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                    .eq(StringUtils.isNotBlank(requestDTO.getBaseName()), "si." + ProductionManageSortInstorage.COL_BASE_NAME, requestDTO.getBaseName())
                    .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), "si." + ProductionManageSortInstorage.COL_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductLevelName()), "si." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME, requestDTO.getProductLevelName())
                    .eq(StringUtils.isNotBlank(requestDTO.getCreateUserName()), "d." + ProductionManageStockFlowDetails.COL_CREATE_USER_NAME, requestDTO.getCreateUserName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductName()), "si.ProductName" + ProductionManageSortInstorage.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .ge(StringUtils.isNotBlank(outInInterval[0]), "d." + ProductionManageStockFlowDetails.COL_OUT_IN_DATE, outInInterval[0])
                    .lt(StringUtils.isNotBlank(outInInterval[1]), "d." + ProductionManageStockFlowDetails.COL_OUT_IN_DATE, outInInterval[1]);
        }

        return queryWrapper;
    }
    /**
     * 获取所有的库存流水数据
     *
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStockFlowDetails> list(SearchStockFlowRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStockFlowDetails> wrapper = getWrapper(requestDTO, sysId, organizationId);
        return list(wrapper);
    }

    /**
     * 更新包装信息的库存流水信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default void updateNumber(Long id, Integer boxNum, Integer quantity, BigDecimal weight, FlowDirectionTypeEnum typeEnum, CommonUtil commonUtil) {
        log.info("[DAO]更新库存流水入参 => 业务主键id:{}, 箱数boxNum:{}, 个数quantity:{}, 重量weight:{}, 流水类型:{}",
                id, boxNum, quantity, weight, typeEnum.getValue());
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        UpdateWrapper<ProductionManageStockFlowDetails> updateWrapper = new UpdateWrapper<>();

        updateWrapper.set(OUT_IN_BOX_NUM, boxNum)
                .set(OUT_IN_NUM, quantity)
                .set(OUT_IN_WEIGHT, weight)
                .eq(BUSINESS_ID, id)
                .eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                .eq(OUT_IN_TYPE, typeEnum.getKey());
        int count = update(null, updateWrapper);
        CustomAssert.zero2Error(count, UPDATE_INVENTORY_FLOW_FAILED);
    }
}

