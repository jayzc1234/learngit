package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSTDRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.SaleTaskTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-23
 */
public interface ProductionManageStatisticsSaleTargetDataMapper extends BaseMapper<ProductionManageStatisticsSaleTargetData> {

    String ALL = "全部";

    @Select("select DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m-%d') as saleTargetDate, " + ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID + " as departmentId, " + ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_NAME + " as departmentName, " +
            ProductionManageStatisticsSaleTargetData.COL_PRODUCT_ID + " as productId," + ProductionManageStatisticsSaleTargetData.COL_PRODUCT_NAME + "  as productName, " +
            ProductionManageStatisticsSaleTargetData.COL_SALES_PERSONNEL_ID + " as salesPersonnelId, " + ProductionManageStatisticsSaleTargetData.COL_SALES_PERSONNEL_NAME + " AS salesPersonnelName, " +
            "SUM(" + ProductionManageStatisticsSaleTargetData.COL_TARGET_SALE_AMOUNT + ") as targetSaleAmount, SUM(" + ProductionManageStatisticsSaleTargetData.COL_ACTUAL_SALE_AMOUNT + ") as actualSaleAmount, " +
            ProductionManageStatisticsSaleTargetData.COL_TARGET_ACHIEVEMENT_RATE + " as targetAchievementRate " +
            "from t_production_manage_statistics_sale_target_data ${ew.customSqlSegment} order by DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')")
    IPage<ProductionManageStatisticsSaleTargetData> list(Page<ProductionManageStatisticsSaleTargetData> page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 产品为空，部门为空，销售人员为空
     *
     * @author shixiongfei
     * @date 2019-10-31
     * @updateDate 2019-10-31
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionSix(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.DEPARTMENT.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')", ProductionManageStatisticsSaleTargetData.COL_PRODUCT_ID);

        IPage<ProductionManageStatisticsSaleTargetData> iPage = list(page, wrapper);
        List<ProductionManageStatisticsSaleTargetData> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(record -> {
                record.setSalesPersonnelId(ALL);
                record.setSalesPersonnelName(ALL);
                record.setDepartmentId(ALL);
                record.setDepartmentName(ALL);
            });
        }

        return iPage;
    }

    /**
     * 情况5 => 部门 + 产品 + 销售人员都不为空
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionFive(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.PERSONAL.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')", ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID, ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_NAME, ProductionManageStatisticsSaleTargetData.COL_SALES_PERSONNEL_ID);

        return list(page, wrapper);
    }

    /**
     * 情况4 => 部门 + 销售人员不为空, 产品为空
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionFour(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.PERSONAL.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')", ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID, ProductionManageStatisticsSaleTargetData.COL_SALES_PERSONNEL_ID);

        IPage<ProductionManageStatisticsSaleTargetData> iPage = list(page, wrapper);
        List<ProductionManageStatisticsSaleTargetData> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(record -> {
                record.setProductId(ALL);
                record.setProductName(ALL);
            });
        }

        return iPage;
    }

    /**
     * 情况3 => 部门 + 产品不为空, 销售人员为空
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionThree(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.DEPARTMENT.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')", ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID, ProductionManageStatisticsSaleTargetData.COL_PRODUCT_ID);

        IPage<ProductionManageStatisticsSaleTargetData> iPage = list(page, wrapper);
        List<ProductionManageStatisticsSaleTargetData> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(record -> {
                record.setSalesPersonnelId(ALL);
                record.setSalesPersonnelName(ALL);
            });
        }

        return iPage;
    }

    /**
     * 情况2 => 部门不为空, 产品 + 销售人员为空
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionTwo(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.DEPARTMENT.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')", ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID);

        IPage<ProductionManageStatisticsSaleTargetData> iPage = list(page, wrapper);
        List<ProductionManageStatisticsSaleTargetData> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(record -> {
                record.setProductId(ALL);
                record.setProductName(ALL);
                record.setSalesPersonnelId(ALL);
                record.setSalesPersonnelName(ALL);
            });
        }

        return iPage;
    }

    /**
     * 情况1 => 部门 + 产品 + 销售人员都为空
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default IPage<ProductionManageStatisticsSaleTargetData> listConditionOne(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageStatisticsSaleTargetData> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, SaleTaskTypeEnum.DEPARTMENT.getKey())
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')");

        IPage<ProductionManageStatisticsSaleTargetData> iPage = list(page, wrapper);
        List<ProductionManageStatisticsSaleTargetData> records = iPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(record -> {
                record.setDepartmentId(ALL);
                record.setDepartmentName(ALL);
                record.setProductId(ALL);
                record.setProductName(ALL);
                record.setSalesPersonnelId(ALL);
                record.setSalesPersonnelName(ALL);
            });
        }

        return iPage;
    }

    /**
     * 通用sql包装器
     *
     * @author shixiongfei
     * @date 2019-10-24
     * @updateDate 2019-10-24
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageStatisticsSaleTargetData> getCommonWrapper(SearchSTDRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageStatisticsSaleTargetData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsSaleTargetData.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsSaleTargetData.COL_ORGANIZATION_ID, organizationId)
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE, requestDTO.getEndQueryDate())
                .eq(StringUtils.isNotBlank(requestDTO.getDepartmentId()), ProductionManageStatisticsSaleTargetData.COL_DEPARTMENT_ID, requestDTO.getDepartmentId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStatisticsSaleTargetData.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getSalesPersonnelId()), ProductionManageStatisticsSaleTargetData.COL_SALES_PERSONNEL_ID, requestDTO.getSalesPersonnelId());
        return queryWrapper;
    }

    /**
     * 获取销售目标数据统计列表(不含分页)
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default List<ProductionManageStatisticsSaleTargetData> listHistogram(SearchSTDRequestDTO requestDTO, String sysId, String organizationId, Byte taskType) {
        QueryWrapper<ProductionManageStatisticsSaleTargetData> wrapper = getCommonWrapper(requestDTO, sysId, organizationId);
        wrapper.select("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m-%d') as saleTargetDate", "SUM(" + ProductionManageStatisticsSaleTargetData.COL_TARGET_SALE_AMOUNT + ") as targetSaleAmount", "SUM(" + ProductionManageStatisticsSaleTargetData.COL_ACTUAL_SALE_AMOUNT + ") as actualSaleAmount")
                .eq(ProductionManageStatisticsSaleTargetData.COL_TASK_TYPE, taskType)
                .groupBy("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')")
                .orderByAsc("DATE_FORMAT(" + ProductionManageStatisticsSaleTargetData.COL_SALE_TARGET_DATE + ",'%Y-%m')");
        return selectList(wrapper);
    }
}