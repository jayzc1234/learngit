package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.SearchSTCRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTask;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.DepartmentSaleTaskStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.PersonalSaleTaskStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface ProductionManageSaleTaskMapper extends BaseMapper<ProductionManageSaleTask> {

    /**
     * 根据唯一id获取部门数据集合
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT SUM(TargetSaleAmount) AS targetSaleAmount, productId, " +
            "productName, YEAR(saleDate) AS year, QUARTER(saleDate) AS quarter " +
            "FROM production_manage_sale_task ${ew.customSqlSegment}")
     List<DepartmentSaleTaskStatisticsVO> lisDepartmentByUniqueIds(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 根据唯一id获取部门数据集合
     *
     * @param queryWrapper
     * @return
     */
    @Select("SELECT SUM(TargetSaleAmount) AS targetSaleAmount, productId, " +
            "productName, YEAR(saleDate) AS year, QUARTER(saleDate) AS quarter, " +
            "SalesPersonnelId AS salesPersonnelId, SalesPersonnelName AS salesPersonnelName " +
            "FROM production_manage_sale_task ${ew.customSqlSegment}")
    List<PersonalSaleTaskStatisticsVO> listPersonalByUniqueIds(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    @Select("SELECT DepartmentId as departmentId, DepartmentName as departmentName, ProductId as productId, " +
            "ProductName as productName, SalesPersonnelId as salesPersonnelId, SalesPersonnelName as salesPersonnelName, " +
            "SUM(IFNULL(TargetSaleAmount, 0)) AS targetSaleAmount, TaskType as taskType, DATE_FORMAT(SaleDate, '%Y-%m-%d') as saleTargetDate " +
            " from production_manage_sale_task ${ew.customSqlSegment}" )
    List<ProductionManageStatisticsSaleTargetData> listByStartAndEndDate(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取所有销售人员id
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Select("SELECT DISTINCT SalesPersonnelId FROM production_manage_sale_task where TaskType = 1")
    List<String> listPersonnelIds();

    /**
     * 月度销售任务对比
     *
     * @author shixiongfei
     * @date 2019-11-04
     * @updateDate 2019-11-04
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Select("")
    void salesTaskComparison(SearchSTCRequestDTO requestDTO);
}