package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSpecificationListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-29
 */
public interface ProductionManageStuffSpecificationMapper extends CommonSql<ProductionManageStuffSpecification> {
    @Select("select max(" + ProductionManageStuff.COL_SERIAL_NUMBER + ") from t_production_manage_stuff where " + ProductionManageStuff.COL_ORGANIZATION_ID + "=#{organizationId} and " + ProductionManageStuff.COL_SYS_ID + "=#{sysId}")
    Long getMaxSerialNumber(@Param("organizationId") String organizationId, @Param("sysId") String sysId);

    @Select("select s." + ProductionManageStuff.COL_MEASURE_UNIT + ",ss.* from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID + "  ${ew.customSqlSegment}")
    IPage<ProductionManageStuffSpecificationListVO> pageList(Page<ProductionManageStuffSpecificationListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffSpecification> queryWrapper);

    @Update("update t_production_manage_stuff_specification set " + ProductionManageStuffSpecification.COL_USE_NUM + "=" + ProductionManageStuffSpecification.COL_USE_NUM + "+1 where " + ProductionManageStuffSpecification.COL_ID + "=#{id}")
    void updateUseNum(@Param("id") Long stuffSpecificationId);

    @Select("select ss.*,s." + ProductionManageStuff.COL_MEASURE_UNIT + " from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID + " ${ew.customSqlSegment}")
    List<ProductionManageStuffSpecificationListVO> listByStuffNameAndSortId(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffSpecification> queryWrapper);
}
