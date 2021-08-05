package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.gugeng.WeighingBO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageWeight;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchNewestYCDataVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchWeightResponseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
public interface ProductionManageWeightMapper extends CommonSql<ProductionManageWeight> {
        // 搜索域
        List<String> ADVACCED_SEARCH = Arrays.asList(ProductionManageWeight.COL_WEIGHT,
                ProductionManageWeight.COL_PLANT_BATCH_ID, ProductionManageWeight.COL_PRODUCT_NAME,
                ProductionManageWeight.COL_BASE_NAME, ProductionManageWeight.COL_GREENHOUSE_NAME,
                ProductionManageWeight.COL_WEIGHING_DATE, ProductionManageWeight.COL_UPDATE_USER_NAME);

	@Select(START_SCRIPT
            + "select * from t_production_manage_weighting"
            + START_WHERE
            + ProductionManageWeight.COL_ID + "  in"
            + "<foreach item='item' collection='list' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + END_WHERE
            + END_SCRIPT)
	List<SearchWeightResponseVO> listExcelByIds(List<? extends Serializable> ids);

    /**
     * 获取采收称重列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-11
     * @updateDate 2019-12-11
     * @updatedBy shixiongfei
     */
    @Select("select " + ProductionManageWeight.COL_GREENHOUSE_ID + " as greenhouseId," +
            ProductionManageWeight.COL_GREENHOUSE_NAME + "  as greenhouseName, " +
            ProductionManageWeight.COL_CREATE_DATE + " as createDate," +
            " IFNULL(SUM(" + ProductionManageWeight.COL_WEIGHT + "), 0) as weight from t_production_manage_weighting " +
            "where " + ProductionManageWeight.COL_SYS_ID + " = #{sysId} and " + ProductionManageWeight.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "group by DAY(" + ProductionManageWeight.COL_CREATE_DATE + "), " + ProductionManageWeight.COL_GREENHOUSE_ID)
    List<ProductionManageWeight> listAllWeightByDate(@Param("sysId") String sysId, @Param("organizationId") String organizationId);

    @Select("SELECT DATE_FORMAT(" + ProductionManageWeight.COL_WEIGHING_DATE + ",'%Y-%m') as month, " +
            "IFNULL(SUM(" + ProductionManageWeight.COL_WEIGHT + "), 0) as harvestQuantity " +
            "FROM t_production_manage_weighting ${ew.customSqlSegment}")
    List<WeighingBO> listByHalfYearMsg(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取指定称重类型的最新批次的总称重信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-23
     * @updateDate 2019-12-23
     * @updatedBy shixiongfei
     */
    @Select("SELECT " + ProductionManageWeight.COL_PLANT_BATCH_ID + " AS plantBatchId," +
            ProductionManageWeight.COL_PLANT_BATCH_NAME + "  AS plantBatchName, " +
            "IFNULL(SUM(" + ProductionManageWeight.COL_WEIGHT + "), 0) AS totalProduceWeight FROM t_production_manage_weighting " +
            "WHERE " + ProductionManageWeight.COL_TYPE + " = #{type} AND " + ProductionManageWeight.COL_SYS_ID + " = #{sysId} " +
            "AND " + ProductionManageWeight.COL_ORGANIZATION_ID + " = #{organizationId} " +
            "GROUP BY " + ProductionManageWeight.COL_PLANT_BATCH_ID + " ORDER BY " + ProductionManageWeight.COL_WEIGHING_DATE + " DESC limit 15")
    List<SearchNewestYCDataVO> listNewestBatchMsg(@Param("sysId") String sysId, @Param("organizationId") String organizationId, @Param("type") Integer type);
}