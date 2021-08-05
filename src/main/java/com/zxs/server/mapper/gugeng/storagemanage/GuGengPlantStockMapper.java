package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlantStockRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengPlantStock;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;


/**
 * <p>
 * 种植存量表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-09
 */
public interface GuGengPlantStockMapper extends BaseMapper<GuGengPlantStock> {


    /**
     * 获取批次sql包装器
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<GuGengPlantStock> getBatchWrapper(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        QueryWrapper<GuGengPlantStock> queryWrapper = new QueryWrapper<>();
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ORGANIZATION_ID, organizationId);

        String search = requestDTO.getSearch();

        if (StringUtils.isBlank(search)) {
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), PLANT_BATCH_NAME, requestDTO.getPlantBatchName());
        } else {
            queryWrapper.like(PLANT_BATCH_NAME, search);
        }

        return queryWrapper;
    }

    /**
     * 获取产品sql包装器
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<GuGengPlantStock> getProductWrapper(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<GuGengPlantStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ORGANIZATION_ID, organizationId);

        String search = requestDTO.getSearch();

        if (StringUtils.isBlank(search)) {
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getProductName()), PRODUCT_NAME, requestDTO.getProductName());
        } else {
            queryWrapper.like(PRODUCT_NAME, search);
        }

        return queryWrapper;
    }

    /**
     * 获取种植存量列表
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<GuGengPlantStock> listBatch(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        Page<GuGengPlantStock> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<GuGengPlantStock> queryWrapper = getBatchWrapper(requestDTO, commonUtil);

        queryWrapper.orderByDesc(CREATE_DATE);
        return selectPage(page, queryWrapper);
    }

    /**
     * 获取指定条件下的批次总存量重量
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default BigDecimal getBatchTotalWeight(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        QueryWrapper<GuGengPlantStock> queryWrapper = getBatchWrapper(requestDTO, commonUtil);
        return getTotalWeight(queryWrapper);
    }

    /**
     * 获取种植存量列表
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<GuGengPlantStock> listProduct(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        Page<GuGengPlantStock> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<GuGengPlantStock> queryWrapper = getProductWrapper(requestDTO, commonUtil);
        queryWrapper.groupBy(PRODUCT_ID);
        queryWrapper.orderByDesc(CREATE_DATE);
        return listProduct(page, queryWrapper);
    }

    /**
     * 获取指定条件下的产品总存量重量
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default BigDecimal getProductTotalWeight(SearchPlantStockRequestDTO requestDTO, CommonUtil commonUtil) {
        QueryWrapper<GuGengPlantStock> queryWrapper = getProductWrapper(requestDTO, commonUtil);
        return getTotalWeight(queryWrapper);
    }


    @Select("SELECT IFNULL(SUM(" + GuGengPlantStock.COL_STOCK_WEIGHT + "), 0) FROM t_gu_geng_plant_stock ${ew.customSqlSegment}")
    BigDecimal getTotalWeight(@Param(Constants.WRAPPER) Wrapper queryWrapper);


    @Select("SELECT " + GuGengPlantStock.COL_PRODUCT_ID + " AS productId, " + GuGengPlantStock.COL_PRODUCT_NAME + " AS productName, IFNULL(SUM(" + GuGengPlantStock.COL_STOCK_WEIGHT + "), 0) AS stockWeight " +
            "FROM t_gu_geng_plant_stock ${ew.customSqlSegment}")
    IPage<GuGengPlantStock> listProduct(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);
}