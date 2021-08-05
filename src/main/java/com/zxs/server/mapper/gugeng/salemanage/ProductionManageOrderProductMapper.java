package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageSaleTask;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ClientSaleOrderStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.DepartmentSaleTaskStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.OrderProductStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOrderProductListVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface ProductionManageOrderProductMapper extends CommonSql<ProductionManageOrderProduct> {

    @Select(START_SCRIPT
            +"select mop.product_name,mo.sale_user_name,mop.order_weight,mop.order_quantity," +
            " mop.total_price,mop.Id,mop.gg_partion_num as ggPartionNum,mo.operator_name as createUserName,mo.order_status,mo.order_no,mo.order_date,mo.order_product_num,mo.order_type,mo.order_weight " +
            "from t_production_manage_order mo left join t_production_manage_order_product mop on mo.Id=mop.order_id left join t_production_manage_client mc on mo.client_id=mc.Id "
            +"${ew.customSqlSegment}"
            + END_SCRIPT)
    IPage<ProductionManageOrderProductListVO> list(Page<ProductionManageOrderProduct> page,
                                                   @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProduct> pOrderProductListDTO);

    @Select(START_SCRIPT
            +"select sum(mop.order_weight) as order_weight,sum(mop.order_quantity) order_quantity,sum(mop.product_num) orderProductNum,sum(mop.total_price) orderMoney ,sum(mop.gg_partion_num) ggTotalPartionNum" +
            " from t_production_manage_order mo left join t_production_manage_order_product mop on mo.Id=mop.order_id left join t_production_manage_client mc on mo.client_id=mc.Id"
            + START_WHERE
             + " <if test='productId !=null and productId != &apos;&apos; '> and mop.product_id = #{productId} </if> "
             + " <if test='clientId !=null and clientId != &apos;&apos; '> and mo.client_id = #{clientId} </if> "
            + END_WHERE
            + END_SCRIPT)
    ClientSaleOrderStatisticsVO statistics(@Param("clientId") Long clientId, @Param("productId") String productId);

    /**
     * 获取完成销量和完成销售额
     */
    @Select("<script>" +
                "SELECT IFNULL(SUM(op.total_price), 0) AS completedSaleAmount, IFNULL(SUM(op.order_weight), 0) AS completedSales " +
                "FROM t_production_manage_order_product op " +
                "INNER JOIN t_production_manage_order o ON o.Id = op.order_id " +
                "WHERE o.sys_id = #{st.sysId} AND o.organization_id = #{st.organizationId} AND o.order_status = 4 " +
                "AND DATE_FORMAT(o.delivery_date, '%Y-%m') = DATE_FORMAT(#{st.saleDate}, '%Y-%m') AND product_id = #{st.productId} " +
                "AND o.department_id = #{st.departmentId} " +
                "<if test = 'st.salesPersonnelId != null and st.salesPersonnelId != &apos;&apos; '> " +
                    "AND o.sale_user_id = #{st.salesPersonnelId} " +
                "</if>" +
            "</script>"
    )
    OrderProductStatisticsVO statisticsOrderProduct(@Param("st") ProductionManageSaleTask saleTask);

    @Select("<script>" +
                "SELECT IFNULL(SUM(op.total_price), 0) AS completedSaleAmount " +
                "FROM t_production_manage_order_product op " +
                "INNER JOIN t_production_manage_order o ON o.Id = op.order_id " +
                "WHERE o.sys_id = #{sysId} AND o.organization_id = #{organizationId} AND o.order_status = 4 " +
                "AND YEAR(o.delivery_date) = #{st.year} AND product_id = #{st.productId} " +
                "AND o.department_id = #{st.departmentId} " +
                "<if test = 'salesPersonnelId != null and salesPersonnelId != &apos;&apos; '> " +
                    "AND o.sale_user_id = #{salesPersonnelId} " +
                "</if>" +
                "<if test = 'st.quarter != null and st.quarter != &apos;&apos; '> " +
                    "AND QUARTER(o.delivery_date) = #{st.quarter} " +
                "</if>" +
            "</script>"
    )
    OrderProductStatisticsVO statisticYearOrQuarterOrderProduct(@Param("st") DepartmentSaleTaskStatisticsVO statisticsVO, @Param("sysId") String sysId, @Param("organizationId") String organizationId, @Param("salesPersonnelId") String salesPersonnelId);

    @Delete("delete from t_production_manage_order_product where order_id=#{orderId}")
	void deleteByOrderId(@Param("orderId") Long orderId);

    @Select(START_SCRIPT
            +"select mop.product_name,mop.order_weight,mop.order_quantity,mop.total_price,mop.Id,mo.create_user_name,mo.order_status,mo.order_no,mo.sale_user_name,mo.order_date,mop.gg_partion_num as ggPartionNum" +
            " from t_production_manage_order mo left join t_production_manage_order_product mop on mo.Id=mop.order_id left join t_production_manage_client mc on mo.client_id=mc.Id"
            + START_WHERE
            +"mop.Id in"
            +"<foreach collection='list' index='index' item='item' open='(' separator=',' close=')'>" +
            "    #{item}  " +
            " </foreach>"
            + END_WHERE
            + END_SCRIPT)
	List<ProductionManageOrderProductListVO> listExcelByIds(List<? extends Serializable> ids);

    @Select(START_SCRIPT
            +"select DISTINCT product_id from t_production_manage_order_product "
            +"${ew.customSqlSegment}"
            + END_SCRIPT)
    List<String> selectProductIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOrderProduct> orderProductQueryWrapper);

    @Select(START_SCRIPT
            +"<foreach collection='list' index='index' item='item'  separator=';' >" +
            "   update  t_production_manage_order_product set product_sort_name=#{item.productSortName},product_sort_id=#{item.productSortId} where product_id=#{item.productId}" +
            " </foreach>"
            + END_SCRIPT)
    void batchUpdate(List<Map<String, String>> list);

    @Select(START_SCRIPT
            +"   update  t_production_manage_order_product set product_sort_name=#{sortName},product_sort_id=#{sortId} where product_id=#{productId}"
            + END_SCRIPT)
    void updateByMap(Map<String, String> map);

    @Select(START_SCRIPT
            +"select * from t_production_manage_order_product "
            +" where order_id=#{orderId}"
            + END_SCRIPT)
    List<ProductionManageOrderProduct> selectByOrderId(@Param("orderId") Long orderId);
}