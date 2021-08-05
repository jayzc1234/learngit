package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.SearchSTCRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTaskComparison;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-11-04
 */
public interface ProductionManageSaleTaskComparisonMapper extends BaseMapper<ProductionManageSaleTaskComparison> {


    @Select("SELECT department_id AS departmentId, department_name AS departmentName, " +
            "product_id AS productId, product_name AS productName, sale_date AS saleDate, " +
            "SUM(dep_sale_target_amount) AS depSaleTargetAmount, SUM(per_sale_target_amount) AS perSaleTargetAmount " +
            "FROM t_production_manage_sale_task_comparison ${ew.customSqlSegment}")
    IPage<ProductionManageSaleTaskComparison> list(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 月度销售任务对比列表
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param requestDTO 请求体
     * @param sysId 系统id
     * @param organizationId 组织id
     * @return
     */
    default IPage<ProductionManageSaleTaskComparison> monthSalesTaskComparison(SearchSTCRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageSaleTaskComparison> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSaleTaskComparison> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.orderByDesc("sale_date");
        return selectPage(page, wrapper);
    }

    /**
     * 获取sql拼接器, 过滤掉部门目标销售额和个人目标销售额都为0的数据
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    default QueryWrapper<ProductionManageSaleTaskComparison> getWrapper(SearchSTCRequestDTO requestDTO, String sysId, String organizationId) {
        QueryWrapper<ProductionManageSaleTaskComparison> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), "sys_id", sysId)
                .eq(StringUtils.isNotBlank(organizationId), "organization_id", organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getSearch()), "product_name", requestDTO.getSearch())
                // 过滤掉部门目标销售额和个人目标销售额都为0的数据
                .and(filter -> filter.ne("dep_sale_target_amount", 0)
                                .or().ne("per_sale_target_amount", 0));
        return wrapper;
    }

    /**
     * 季度销售任务对比列表
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param requestDTO 请求体
     * @param sysId 系统id
     * @param organizationId 组织id
     * @return
     */
    default IPage<ProductionManageSaleTaskComparison> quarterSalesTaskComparison(SearchSTCRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageSaleTaskComparison> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSaleTaskComparison> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.groupBy("department_id", "YEAR(sale_date)", "QUARTER(sale_date)", "product_id")
                .orderByDesc("sale_date");
        return list(page, wrapper);
    }

    /**
     * 年度销售任务对比列表
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param requestDTO 请求体
     * @param sysId 系统id
     * @param organizationId 组织id
     * @return
     */
    default IPage<ProductionManageSaleTaskComparison> yearSalesTaskComparison(SearchSTCRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManageSaleTaskComparison> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageSaleTaskComparison> wrapper = getWrapper(requestDTO, sysId, organizationId);
        wrapper.groupBy("department_id", "YEAR(sale_date)", "product_id")
                .orderByDesc("sale_date");
        return list(page, wrapper);
    }

    /**
     * 获取指定条件的销售任务对比数据
     *
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     * @return
     */
    @Select("SELECT department_id, department_name, product_id, product_name, dep_sale_target_amount, per_sale_target_amount " +
            "FROM t_production_manage_sale_task_comparison ${ew.customSqlSegment}")
    ProductionManageSaleTaskComparison get(@Param(Constants.WRAPPER) Wrapper queryWrapper);
}