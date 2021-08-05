package com.zxs.server.mapper.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInWarehouse;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInventory;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialOutWarehouse;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.base.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 物料库存表 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
public interface MaterialInventoryMapper extends CommonSql<MaterialInventory> {
    /**
     * 分页列表
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select  mi.*,m.material_name,m.material_sort_name,m.material_sort_id,m.same_batch_material_id," +
            "spec.specification_info,spec.specification,spec.specification_type_name,spec.specification_type,"
            +"spec.specification_unit,spec.specification_unit_name,spec.id as materialSpecificationId,spec.material_code, "
            + "CONCAT(mi.total_inventory,spec.specification_type_name) AS totalInventoryValue ,"
            +"CONCAT(mi.total_stock,spec.specification_unit_name) AS totalStockValue, "
            +"(mi.total_inventory - mi.warning_total_inventory) as warningInventory, "
            +"IF(mi.total_inventory &lt;= mi.warning_total_inventory,\"! 库存不足\",\"√ 库存充足\") AS warning "
            +"from t_material_inventory mi left join t_material_info m on mi.public_material_id = m.public_material_id left join t_material_specification spec on mi.material_specification_id=spec.id" +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<MaterialInfoInventoryListVO> pageList(Page<MaterialInfoInventoryListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInventory> queryWrapper);

    /**
     * 物料出入库流水
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select  * "
            +"from("
                +" SELECT  a.inbound_code AS boundCode,a.inbound_type AS boundType," +
                          "a.inbound_type_name AS boundTypeName,a.specification_info," +
                          "CONCAT(a.inbound_num,a.specification_type_name) AS boundNumValue," +
                          "CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name) AS boundNumAmount," +
                          "a.inbound_time AS boundTime,a.user_id,a.user_name,a.create_time," +
                          "a.material_batch,a.public_material_id," +
                          "spec.material_code,b.material_name,b.material_sort_name, "+
                          "IFNULL(case when a.unit_price is null then spec.unit_price else a.unit_price end,0) unitPrice,"+
                          " IFNULL(case when a.unit_price is null then spec.unit_price else a.unit_price end,0) * a.inbound_num AS money"
                +" FROM t_material_in_warehouse a INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on b.public_material_id = spec.public_material_id"
                +" ${ew.customSqlSegment}"
            +" UNION ALL"
                + " SELECT a.outbound_code AS boundCode,a.outbound_type AS boundType," +
                      " a.outbound_type_name AS boundTypeName,a.specification_info," +
                        "CONCAT(a.outbound_num,a.specification_type_name) AS boundNumValue," +
                        "CONCAT(ROUND(a.outbound_num*a.specification,2),a.specification_unit_name) AS boundNumAmount," +
                        "a.outbound_time AS boundTime,a.user_id,a.user_name,a.create_time," +
                        "a.material_batch,a.public_material_id," +
                        "spec.material_code,b.material_name,b.material_sort_name, "+
                        "IFNULL(case when inw.unit_price is null then spec.unit_price else inw.unit_price end,0) unitPrice,"+
                        " IFNULL(case when inw.unit_price is null then spec.unit_price else inw.unit_price end,0) * a.outbound_num AS money"
                +" FROM t_material_out_warehouse a INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on b.public_material_id = spec.public_material_id"
                +"  left join t_material_in_warehouse inw on a.material_batch=inw.material_batch"
                +" ${ew.customSqlSegment}"
                +" group by a.id "
            +") aaa"
            +" order by aaa.boundTime desc"
            + END_SCRIPT
    )
    IPage<WarehouseOutAndInInfoListVO> warehouseOutAndInInfo(Page<WarehouseOutAndInInfoListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInventory> queryWrapper);

    /**
     * 单个物料库存流水
     * @param page
     * @param queryWrapper
     * @param publicMaterialId
     * @param organizationId
     * @return
     */
    @Select(START_SCRIPT
            +" select a.material_batch,a.supper_name,a.wareHouse_name,a.wareHouse_id," +
            "CONCAT(a.inbound_num,a.specification_type_name) AS inboundNumValue," +
            "CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name) AS inboundNumAmount," +
            "CONCAT(b.outboundNum,a.specification_type_name) AS outboundNumValue," +
            "CONCAT(ROUND(b.outboundNum*a.specification,2),a.specification_unit_name) AS outboundNumAmount," +
            "IF(b.outboundNum IS NULL,CONCAT(a.inbound_num,a.specification_type_name),CONCAT(ROUND(a.inbound_num - b.outboundNum,2),a.specification_type_name)) AS totalInventoryValue," +
            "IF(b.outboundNum IS NULL,a.inbound_num,ROUND(a.inbound_num - b.outboundNum,2)) AS totalInventory," +
            "IF(b.outboundNum IS NULL,CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name),CONCAT(ROUND((a.inbound_num - b.outboundNum)*a.specification,2),a.specification_unit_name)) AS totalStockValue," +
            "IF(b.outboundNum IS NULL,ROUND(a.inbound_num*a.specification,2),ROUND((a.inbound_num - b.outboundNum)*a.specification,2)) AS totalStock"
            +" FROM  t_material_in_warehouse a "
            +" LEFT JOIN (SELECT SUM(aa.outbound_num) AS outboundNum,aa.public_material_id, aa.material_batch ,aa.organization_id,aa.sys_id"
            +" FROM t_material_out_warehouse aa WHERE aa.organization_id = #{organizationId} and aa.public_material_id = #{publicMaterialId} GROUP BY aa.material_batch) b ON a.public_material_id = b.public_material_id and b.material_batch = a.material_batch "
            + " INNER JOIN t_material_info d ON a.public_material_id = d.public_material_id "
            +" ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<WarehouseMaterialInfoListVO> listSingleMaterialWarehouseInOutFlow(Page<WarehouseMaterialInfoListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialOutWarehouse> queryWrapper, @Param("publicMaterialId") String publicMaterialId, @Param("organizationId") String organizationId);

    /**
     * 单个物料出入库详情
     * @param page
     * @param queryWrapper
     * @return
     *
     */
    String likeWarehouseInInfoPageParam = " SELECT a.inbound_code AS boundCode,a.inbound_type AS boundType," +
            "a.inbound_type_name AS boundTypeName,a.specification_info," +
            "CONCAT(a.inbound_num,a.specification_type_name) AS boundNumValue," +
            "CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name) AS boundNumAmount," +
            "a.inbound_time AS boundTime,a.user_id,a.user_name,a.create_time," +
            "a.material_batch,a.public_material_id," +
            "spec.material_code,b.material_name,b.material_sort_name ";

    String likeWarehouseInInfoPageTable = " FROM t_material_in_warehouse a " +
            "INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on b.public_material_id = spec.public_material_id";

    String likeWarehouseOutInfoPageParam = " SELECT a.outbound_code AS boundCode,a.outbound_type AS boundType," +
            "a.outbound_type_name AS boundTypeName,a.specification_info," +
            "CONCAT(a.outbound_num,a.specification_type_name) AS boundNumValue," +
            "CONCAT(ROUND(a.outbound_num*a.specification,2),a.specification_unit_name) AS boundNumAmount," +
            "a.outbound_time AS boundTime,a.user_id,a.user_name,a.create_time," +
            "a.material_batch,a.public_material_id," +
            "spec.material_code,b.material_name,b.material_sort_name ";

    String likeWarehouseOutInfoPageTable = " FROM t_material_out_warehouse a " +
            "INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on b.public_material_id = spec.public_material_id";

    @Select(START_SCRIPT
            +" select * from ("
            +likeWarehouseInInfoPageParam
            +likeWarehouseInInfoPageTable
            +" ${ew.customSqlSegment}"
            +" UNION ALL"
            +likeWarehouseOutInfoPageParam
            +likeWarehouseOutInfoPageTable
            +" ${ew.customSqlSegment}"
            +") aaa"
            +" order by aaa.boundTime desc "
            + END_SCRIPT
    )
    IPage<SingleMaterialInOutWarehouseListVO> listSingleMaterialOutWarehouseInfo(Page<SingleMaterialInOutWarehouseListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialOutWarehouse> queryWrapper);


    /**
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +" select a.material_batch,a.supper_name,a.wareHouse_name,a.wareHouse_id," +
            "CONCAT(a.inbound_num,a.specification_type_name) AS inboundNumValue," +
            "CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name) AS inboundNumAmount," +
            "CONCAT(b.outboundNum,a.specification_type_name) AS outboundNumValue," +
            "CONCAT(ROUND(b.outboundNum*a.specification,2),a.specification_unit_name) AS outboundNumAmount," +
            "IF(b.outboundNum IS NULL,CONCAT(a.inbound_num,a.specification_type_name),CONCAT(ROUND(a.inbound_num - b.outboundNum,2),a.specification_type_name)) AS totalInventoryValue," +
            "IF(b.outboundNum IS NULL,a.inbound_num,ROUND(a.inbound_num - b.outboundNum,2)) AS totalInventory," +
            "IF(b.outboundNum IS NULL,CONCAT(ROUND(a.inbound_num*a.specification,2),a.specification_unit_name),CONCAT(ROUND((a.inbound_num - b.outboundNum)*a.specification,2),a.specification_unit_name)) AS totalStockValue," +
            "IF(b.outboundNum IS NULL,ROUND(a.inbound_num*a.specification,2),ROUND((a.inbound_num - b.outboundNum)*a.specification,2)) AS totalStock"
            +" FROM  t_material_in_warehouse a "
            +" LEFT JOIN (SELECT SUM(aa.outbound_num) AS outboundNum,aa.public_material_id, aa.material_batch ,aa.organization_id,aa.sys_id"
            +" FROM t_material_out_warehouse aa WHERE aa.organization_id = #{organizationId} and aa.public_material_id = #{publicMaterialId} GROUP BY aa.material_batch) b ON a.public_material_id = b.public_material_id and b.material_batch = a.material_batch "
            + " INNER JOIN t_material_info d ON a.public_material_id = d.public_material_id "
            +" ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<WarehouseMaterialInfoListVO> listBatchByMaterialId(Page<WarehouseMaterialInfoListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialOutWarehouse> queryWrapper, @Param("publicMaterialId") String publicMaterialId, @Param("organizationId") String organizationId);


    /**
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select({START_SCRIPT,
            "select a.id, a.public_material_id, a.organization_id, " +
                    "a.organization_name, a.total_inventory, a.total_stock, " +
                    "a.warning_total_inventory, a.create_time, a.update_time,a.material_specification_id,",
            "b.material_sort_id,b.material_sort_name,b.same_batch_material_id,b.disable_flag,b.disable_flag_name,",
            "spec.specification_info,b.material_name,",
            "spec.material_code,spec.specification,spec.id as materialSpecificationId,",
            "spec.specification_unit,spec.specification_unit_name,",
            "spec.specification_type,spec.specification_type_name,",
            "CONCAT(a.total_inventory,spec.specification_type_name) AS totalInventoryValue ,",
            "CONCAT(a.total_stock,spec.specification_unit_name) AS totalStockValue, ",
            "(a.total_inventory - a.warning_total_inventory) as warningInventory,   ",
            "IF(a.total_inventory &lt;= a.warning_total_inventory,\"! 库存不足\",\"√ 库存充足\") AS warning ",
            " FROM t_material_inventory a INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on spec.Id=a.material_specification_id",
            " ${ew.customSqlSegment}",
            END_SCRIPT
    })
    IPage<PublicMaterialInfoListViewVO> listMaterialOutInfo(Page<PublicMaterialInfoListViewVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInWarehouse> queryWrapper);

    /**
     * 根据规格id和物料id查库存
     * @param publicMaterialId
     * @param materialSpecificationId
     * @return
     */
    @Select({
            "select * ",
            "from t_material_inventory",
            "where public_material_id = #{publicMaterialId} and material_specification_id = #{materialSpecificationId}"
    })
    MaterialInventory selectByPublicMaterialId(@Param("publicMaterialId") String publicMaterialId, @Param("materialSpecificationId") Long materialSpecificationId);





    @Select({START_SCRIPT,
            "select " +
                    "a.id, a.organization_id, " +
                    "organization_name, " +
                    "a.public_material_id, material_name," +
                    "production_unit_name, company_address, " +
                    "contact, contact_details, " +
                    "disable_flag*1 as disableFlag,  " +
                    "material_sort_name, material_sort_id, " +
                    "shelf_life, shelf_life_unit, " +
                    "shelf_life_unit_name, " +
                    "create_time, update_time " +
                    " " +
                    ",s.specification_info,s.material_code,a.same_batch_material_id,s.id as materialSpecificationId,s.use_count,s.unit_price",
            "FROM t_material_info a left join  t_material_specification s on s.public_material_id=a.public_material_id",
            " ${ew.customSqlSegment}",
            END_SCRIPT
    })
    IPage<PublicMaterialInfoListViewVO> enableMaterialsByField(Page<PublicMaterialInfoListViewVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInWarehouse> queryWrapper);
}
