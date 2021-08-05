package com.zxs.server.mapper.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.bo.gugeng.BatchWeightBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.storage.EXProductionManageSortingLoss;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductInStorageDropDownDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchSortingStorageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.CodeRelationDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageCodeIn;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSortInstorageStatisticsResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.SORTING_INBOUND;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InboundConstants.GG_PACKING_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InStorageErrorMsgConstants.UPDATE_INBOUND_MSG_FAILED;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
public interface ProductionManageSortInstorageMapper extends CommonSql<ProductionManageSortInstorage> {

//    default IPage<ProductionManageSortInstorage> page(Page<ProductionManageSortInstorage> page, ProductSortInstoragePageDto param, String organizationId, String sysId){
//        Wrapper<ProductionManageSortInstorage> pageWrapper = new QueryWrapper<>();
//        QueryWrapper<ProductionManageHarvestDamage> queryWrapper = new QueryWrapper();
//        queryWrapper.eq(StringUtils.isNotBlank(organizationId), "organizationId", organizationId)
//                .eq(StringUtils.isNotBlank(sysId), "sysId", sysId);
//
//        String search = param.getSearch();
//        if (StringUtils.isBlank(search)) {
//            /**
//             *  分拣类型（种植分拣、外采分拣）、批次名称、产品名称、区域/基地名称、产品等级、入库时间（时间段）、操作人
//             * // todo 精准还是模糊等测试提
//             */
//            queryWrapper.eq((param.getType() != null ), "Type", param.getType())
//                    .like(StringUtils.isNotBlank(param.getPlantBatchId()), "PlantBatchId",param.getPlantBatchId())
//                    .like(StringUtils.isNotBlank(param.getProductName()), "ProductName",param.getProductName())
//                    .like(StringUtils.isNotBlank(param.getBaseName()), "BaseName",param.getBaseName())
//                    .like(StringUtils.isNotBlank(param.getGreenhouseName()), "GreenhouseName",param.getGreenhouseName())
//                    .like(StringUtils.isNotBlank(param.getCreateDate()) , "CreateDate",param.getCreateDate())
//                    .like(StringUtils.isNotBlank(param.getCreateUserName()), "CreateUserName",param.getCreateUserName())
//                    .like(StringUtils.isNotBlank(param.getLevelSpecificationId()), "LevelSpecificationId",param.getLevelSpecificationId());
//        } else {
//            queryWrapper.and(condition -> condition
//                    .like("PlantBatchId", search)
//                    .or().like("ProductName", search)
//                    .or() .like("BaseName", search)
//                    .or().like("GreenhouseName", search)
//                    .or().like("CreateDate", search)
//                    .or().like("CreateUserName", search)
//                    .or().like("LevelSpecificationId", search));
//        }
//        queryWrapper.orderByDesc("CreateDate");
//        IPage<ProductionManageSortInstorage> productWeightIPage = this.selectPage(page, pageWrapper);
//        return productWeightIPage;
//    }

    /**
     * 通过码id来获取入库产品信息
     * @param queryWrapper
     * @return
     */
    @Select("SELECT si.* FROM t_production_manage_sort_instorage si LEFT JOIN " + ProductionManageCodeIn.TABLE_NAME + " ci ON ci." + ProductionManageCodeIn.COL_BUSINESS_UNIQUE_ID + " = si." + ProductionManageSortInstorage.COL_ID + " ${ew.customSqlSegment}")
    ProductionManageSortInstorage getByOutCodeIdAndType(@Param(Constants.WRAPPER) Wrapper<ProductionManageSortInstorage> queryWrapper);


    @Select(START_SCRIPT
            + "SELECT SUM(IFNULL(si." + ProductionManageSortInstorage.COL_WEIGHT + ",0)) as weight from t_production_manage_sort_instorage si "
            + START_WHERE
            + "<if test='startQueryDate !=null and startQueryDate != &apos;&apos; and endQueryDate !=null and endQueryDate != &apos;&apos;'> DATE_FORMAT(" + ProductionManageSortInstorage.COL_CREATE_DATE + ",'%Y-%m-%d') &gt;=#{startQueryDate} and DATE_FORMAT(" + ProductionManageSortInstorage.COL_CREATE_DATE + ",'%Y-%m-%d') &lt;= #{endQueryDate}</if>"
            + "<if test='organizationId !=null and organizationId != &apos;&apos;'> and si." + ProductionManageSortInstorage.COL_ORGANIZATION_ID + " =#{organizationId}</if>"
            + "<if test='sysId !=null and sysId != &apos;&apos;'> and si." + ProductionManageSortInstorage.COL_SYS_ID + " =#{sysId}</if>"
            + END_WHERE
            + END_SCRIPT)
    Double statisticsSortInWeight(DateIntervalDTO dateIntervalDTO);


    @Select(START_SCRIPT
            + "SELECT truncate(SUM(" + ProductionManageSortInstorage.COL_WEIGHT + "),2) from t_production_manage_sort_instorage "
            + START_WHERE
            + "<if test='plantBatchId !=null and plantBatchId != &apos;&apos;'> and " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " =#{plantBatchId}</if>"
            + END_WHERE
            + END_SCRIPT)
    BigDecimal sumWeightByPlantBatchId(@Param("plantBatchId") String plantBatchId);

    /**
     * 获取分拣入库的总重量
     *
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     * @param queryWrapper 查询包装器
     * @return
     */
    @Select("SELECT IFNULL(SUM(" + ProductionManageSortInstorage.COL_WEIGHT + "), 0) AS weight FROM t_production_manage_sort_instorage ${ew.customSqlSegment}")
    @Results(
            @Result(column = "weight", property = "weight", javaType = BigDecimal.class, jdbcType = JdbcType.DECIMAL)
    )
    BigDecimal getInboundWeight(@Param(Constants.WRAPPER) Wrapper<ProductionManageSortInstorage> queryWrapper);

    @Select("select TotalWeight, damageWeight, b." + ProductionManageSortInstorage.COL_WEIGHT + ", b." + ProductionManageSortInstorage.COL_BOX_NUM + ", b." + ProductionManageSortInstorage.COL_QUANTITY + "," +
            " t." + ProductionManageSortInstorage.COL_TYPE + ",  t." + ProductionManageSortInstorage.COL_ID + ", t." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + "," +
            " t." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + ", t." + ProductionManageSortInstorage.COL_PRODUCT_ID + ", t." + ProductionManageSortInstorage.COL_PRODUCT_NAME + "," +
            " t." + ProductionManageSortInstorage.COL_BASE_ID + ", t." + ProductionManageSortInstorage.COL_BASE_NAME + ", t." + ProductionManageSortInstorage.COL_GREENHOUSE_ID + ",\n" +
            " t." + ProductionManageSortInstorage.COL_GREENHOUSE_NAME + ", t." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + ", t." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_NAME + "," +
            " t." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE + ", t." + ProductionManageSortInstorage.COL_PRODUCT_SPEC_NAME + ", t." + ProductionManageSortInstorage.COL_SORTING_SPEC_CODE + "," +
            " t." + ProductionManageSortInstorage.COL_SORTING_SPEC_NAME + ",t." + ProductionManageSortInstorage.COL_CREATE_DATE + ", t." + ProductionManageSortInstorage.COL_CREATE_USER_ID + "," +
            " t." + ProductionManageSortInstorage.COL_CREATE_USER_NAME +
            " from ( SELECT a." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", min(a." + ProductionManageSortInstorage.COL_ID + ") Id, sum(a." + ProductionManageSortInstorage.COL_WEIGHT + ") Weight, " +
            " sum(a." + ProductionManageSortInstorage.COL_BOX_NUM + ") BoxNum, sum(a." + ProductionManageSortInstorage.COL_QUANTITY + ") Quantity, a." + ProductionManageSortInstorage.COL_TYPE +
            " FROM `t_production_manage_sort_instorage` a GROUP BY a." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID +
            ", a." + ProductionManageSortInstorage.COL_TYPE + ", a." + ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE + " ) b \n" +
            "left join ( SELECT a." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ", sum(a." + ProductionManageSortInstorage.COL_WEIGHT + ") TotalWeight," +
            " min(a." + ProductionManageSortInstorage.COL_ID + "Id)  FROM `t_production_manage_sort_instorage` a GROUP BY a." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + ") c " +
            "on b." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + "=c." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " \n" +
            "left join t_production_manage_sort_instorage t on t." + ProductionManageSortInstorage.COL_ID + " = b." + ProductionManageSortInstorage.COL_ID + "\n" +
            "left join (select " + EXProductionManageSortingLoss.COL_PLANT_BATCH_ID + ", sum(c." + EXProductionManageSortingLoss.COL_WEIGHT + ") damageWeight from " + EXProductionManageSortingLoss.TABLE_NAME + " c GROUP BY c." + EXProductionManageSortingLoss.COL_PLANT_BATCH_ID + ") l on t." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + "=l." + EXProductionManageSortingLoss.COL_PLANT_BATCH_ID +
            "${ew.customSqlSegment} ")
    IPage<SearchSortInstorageStatisticsResponseVO> listSortInstorageStatistics(IPage<SearchSortInstorageStatisticsResponseVO> page, @Param(Constants.WRAPPER) Wrapper queryWrapper);


    @Select("SELECT DISTINCT si." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " AS productBatchId, si." + ProductionManageSortInstorage.COL_PLANT_BATCH_NAME + " AS productBatchName, " +
            "si." + ProductionManageSortInstorage.COL_PRODUCT_ID + " AS productId, si." + ProductionManageSortInstorage.COL_PRODUCT_NAME + " AS productName, ci." + ProductionManageCodeIn.COL_OUTER_CODE_ID + " AS singleCodes " +
            "FROM " + ProductionManageCodeIn.TABLE_NAME + " ci " +
            "INNER JOIN t_production_manage_sort_instorage si ON ci." + ProductionManageCodeIn.COL_BUSINESS_UNIQUE_ID + " = si." + ProductionManageSortInstorage.COL_ID + " ${ew.customSqlSegment}")
    List<CodeRelationDTO> listStorageAndCodeMsg(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select("SELECT IFNULL(SUM(" + ProductionManageSortInstorage.COL_BOX_NUM + "), 0) AS weight FROM t_production_manage_sort_instorage ${ew.customSqlSegment}")
    Integer getInboundBoxNum(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageSortInstorage> wrapper);

    /**
     * 通过批次类型来获取分拣入库的批次产品信息
     *
     * @author shixiongfei
     * @date 2019-11-27
     * @updateDate 2019-11-27
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageSortInstorage> listBatchByType(ProductInStorageDropDownDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageSortInstorage> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageSortInstorage.COL_TYPE, requestDTO.getBatchType())
                .eq(StringUtils.isNotBlank(requestDTO.getBatchId()), OLD_PLANT_BATCH_ID, requestDTO.getBatchId())
                .eq(StringUtils.isNotBlank(requestDTO.getSearch()), OLD_PLANT_BATCH_NAME, requestDTO.getSearch())
                .groupBy(OLD_PLANT_BATCH_ID);

        return selectPage(page, queryWrapper);
    }

    /**
     * 获取分拣入库列表，含分页
     *
     * @author shixiongfei
     * @date 2019-12-05
     * @updateDate 2019-12-05
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageSortInstorage> list(SearchSortingStorageRequestDTO requestDTO, String sysId, String organizationId, CommonUtil commonUtil) {
        Page<ProductionManageSortInstorage> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId);
        // search为空则进行高级检索，不为空则普通检索
        if (org.apache.commons.lang.StringUtils.isBlank(requestDTO.getSearch())) {
            String[] date = LocalDateTimeUtil.substringDate(requestDTO.getCreateDate());
            queryWrapper.eq(Objects.nonNull(requestDTO.getSortingType()), OLD_INBOUND_TYPE, requestDTO.getSortingType())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getPlantBatchName()), OLD_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getProductName()), OLD_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getBaseName()), OLD_BASE_NAME, requestDTO.getBaseName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getCreateUserName()), OLD_CREATE_USER_NAME, requestDTO.getCreateUserName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getGreenhouseName()), OLD_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getProductLevelName()), OLD_PRODUCT_LEVEL_NAME, requestDTO.getProductLevelName())
                    .eq(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getProductSpecName()), OLD_PRODUCT_SPEC_NAME, requestDTO.getProductSpecName())
                    .ge(org.apache.commons.lang.StringUtils.isNotBlank(date[0]), OLD_CREATE_DATE, date[0])
                    .le(org.apache.commons.lang.StringUtils.isNotBlank(date[1]), OLD_CREATE_DATE, date[1]);

        } else {
            queryWrapper.like(org.apache.commons.lang.StringUtils.isNotBlank(requestDTO.getSearch()), OLD_PLANT_BATCH_NAME, requestDTO.getSearch());
        }

        // 添加数据权限
        commonUtil.roleDataAuthFilter(SORTING_INBOUND, queryWrapper, OLD_CREATE_USER_ID, org.apache.commons.lang.StringUtils.EMPTY);

        queryWrapper.orderByDesc(OLD_CREATE_DATE);

        return selectPage(page, queryWrapper);
    }

    /**
     * 通过包装信息主键id获取分拣入库信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default ProductionManageSortInstorage getByPackingId(Long packingId, CommonUtil commonUtil) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                .eq(GG_PACKING_ID, packingId);
        List<ProductionManageSortInstorage> list = selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    /**
     * 更新入库的数值相关信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default void updateNumber(ProductionManageSortInstorage storage, Integer packingBoxNum, Integer packingNum, BigDecimal packingWeight) {
        UpdateWrapper<ProductionManageSortInstorage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(OLD_BOX_NUM, packingBoxNum)
                .set(OLD_QUANTITY, packingNum)
                .set(OLD_WEIGHT, packingWeight)
                .eq(OLD_ID, storage.getId())
                // 这里采用乐观锁的方式，防止同一时刻对同一条数据在出库和编辑入库造成数据不一致
                // 需要保证数据库中的此字段不可为空
                .eq(OLD_WEIGHT, storage.getWeight());
        int count = update(null, updateWrapper);
        CustomAssert.zero2Error(count, UPDATE_INBOUND_MSG_FAILED);
    }

    /**
     * 通过批次id集合来获取批次入库总重量信息
     *
     * @author shixiongfei
     * @date 2019-12-23
     * @updateDate 2019-12-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<BatchWeightBO> listByBatchIds(List<String> batchIds, String sysId, String organizationId) {
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .in(OLD_PLANT_BATCH_ID, batchIds)
                .groupBy(OLD_PLANT_BATCH_ID);
        return listByBatchIds(queryWrapper);
    }

    @Select("SELECT " + ProductionManageSortInstorage.COL_PLANT_BATCH_ID + " AS plantBatchId, IFNULL(SUM(" + ProductionManageSortInstorage.COL_WEIGHT + "),0) AS totalInboundWeight FROM t_production_manage_sort_instorage ${ew.customSqlSegment} ")
    List<BatchWeightBO> listByBatchIds(@Param(Constants.WRAPPER) Wrapper queryWrapper);
}
