package com.zxs.server.mapper.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-20
 */
public interface MaterialSpecificationMapper extends CommonSql<MaterialSpecification> {

    /**
     * 根据物料名称和物料分类查询所有物料规格
     * @param specificationQueryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select  * from t_material_info m left join t_material_specification s on m.public_material_id=s.public_material_id" +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<MaterialSpecification> filterWStIdAndMName(@Param(Constants.WRAPPER) QueryWrapper<MaterialSpecification> specificationQueryWrapper);

    /**
     * 根据物料名称和物料分类查询所有物料规格
     * @param specificationQueryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select  count(*) from t_material_info m left join t_material_specification s on m.public_material_id=s.public_material_id" +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    Integer materialCodeUniqueCheck(@Param(Constants.WRAPPER) QueryWrapper<MaterialSpecification> specificationQueryWrapper);

    /**
     * 列表
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select(START_SCRIPT
            +"select  s.* from t_material_info m left join t_material_specification s on m.public_material_id=s.public_material_id" +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<MaterialSpecification> listPage(Page<MaterialSpecification> page, @Param(Constants.WRAPPER) QueryWrapper<MaterialInfo> queryWrapper);

}
