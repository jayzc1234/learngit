package com.zxs.server.mapper.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoDetailVO;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-20
 */
public interface MaterialInfoMapper extends CommonSql<MaterialInfo> {

    /**
     *             "select a.id, a.public_material_id, a.organization_id, " +
     *                     "a.organization_name, a.total_inventory, a.total_stock, " +
     *                     "a.warning_total_inventory, a.create_time, a.update_time,a.material_specification_id,",
     *             "b.material_sort_id,b.material_sort_name,b.same_batch_material_id,",
     *             "spec.specification_info,b.material_name,",
     *             "spec.material_code,spec.specification,spec.id as materialSpecificationId,",
     *             "spec.specification_unit,spec.specification_unit_name,",
     *             "spec.specification_type,spec.specification_type_name,",
     *             "CONCAT(a.total_inventory,spec.specification_type_name) AS totalInventoryValue ,",
     *             "CONCAT(a.total_stock,spec.specification_unit_name) AS totalStockValue, ",
     *             "(a.total_inventory - a.warning_total_inventory) as warningInventory,   ",
     *             "IF(a.total_inventory &lt;= a.warning_total_inventory,\"! 库存不足\",\"√ 库存充足\") AS warning ",
     *             " FROM t_material_inventory a INNER JOIN t_material_info b ON a.public_material_id = b.public_material_id INNER JOIN t_material_specification spec on spec.Id=a.material_specification_id",
     * 根据publicMaterialId查询单个物料规格
     * @param id
     * @return
     */
    @Select({START_SCRIPT,
            "select * from t_material_info",
            "where id =#{id}",
            END_SCRIPT
            })
    @ResultMap("materialInfoDetailMap")
    MaterialInfoDetailVO selectDetailById(@Param("id") Long id);

    /**
     * 根据publicMaterialId查询单个物料规格
     * @param publicMaterialId
     * @return
     */
    @Select("select  * from  t_material_specification where public_material_id=#{publicMaterialId}")
    MaterialSpecification selectSpecificationByMId(@Param("publicMaterialId") String publicMaterialId);

    /**
     * 物料列表
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select m.*,spec.material_code,spec.id as materialSpecificationId,spec.unit_price," +
            "spec.specification_info,spec.specification,spec.specification_type_name,spec.specification_type,spec.specification_unit,spec.specification_unit_name"
            +" from t_material_info m left join t_material_specification spec on m.public_material_id=spec.public_material_id"
            +" ${ew.customSqlSegment}"
            +END_SCRIPT
    )
    IPage<MaterialInfoListVO> pageList(Page<MaterialInfoListVO> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInfo> queryWrapper);

    /**
     * 根据publicMaterialId查物料
     * @param publicMaterialId
     * @return
     */
    @Select("select  * from  t_material_info where public_material_id=#{publicMaterialId}")
    MaterialInfo selectByPublicMaterialId(@Param("publicMaterialId") String publicMaterialId);
}
