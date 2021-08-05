package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuff;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffDetailVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-29
 */
public interface ProductionManageStuffMapper extends CommonSql<ProductionManageStuff> {

    @Select(START_SCRIPT
            + "select s.*,ss." + ProductionManageStuffSpecification.COL_STUFF_NO + ",ss.price," +
            " ss." + ProductionManageStuffSpecification.COL_SPECIFICATION + ",ss." +
            ProductionManageStuffSpecification.COL_ID + " as stuffSpecificationId,ss." + ProductionManageStuffSpecification.COL_USE_NUM +
            " from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." +
            ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID
            + " where s." + ProductionManageStuff.COL_ID + "=#{id}"
            + END_SCRIPT
    )
    ProductionManageStuffDetailVO detail(@Param("id") Long id);

    @Select(START_SCRIPT
            + "select s.*,ss." + ProductionManageStuffSpecification.COL_STUFF_NO + ",ss." + ProductionManageStuffSpecification.COL_SPECIFICATION + ",ss.price," +
            "ss." + ProductionManageStuffSpecification.COL_ID + " as stuffSpecificationId from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageStuffListVO> pageList(Page<ProductionManageStuff> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuff> queryWrapper);

    @Select(START_SCRIPT
            + "select s.*,ss." + ProductionManageStuffSpecification.COL_STUFF_NO + ",ss." + ProductionManageStuffSpecification.COL_SPECIFICATION + ",ss.price" +
            " from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageStuffListVO> selectVoByIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuff> queryWrapper);

    @Select(START_SCRIPT
            + "select s.*,ss." + ProductionManageStuffSpecification.COL_STUFF_NO + ",ss." + ProductionManageStuffSpecification.COL_SPECIFICATION +
            ",ss.price,ss." + ProductionManageStuffSpecification.COL_ID + " as stuffSpecificationId,ss." + ProductionManageStuffSpecification.COL_USE_NUM + " from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID
            + " where s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=#{publicStuffId}"
            + END_SCRIPT
    )
    ProductionManageStuffDetailVO selectByPublicStuffId(@Param("publicStuffId") String publicStuffId);

    @Select(START_SCRIPT
            + "select s.*,ss." + ProductionManageStuffSpecification.COL_STUFF_NO + ",ss." + ProductionManageStuffSpecification.COL_SPECIFICATION + ",ss." + ProductionManageStuffSpecification.COL_ID + " as stuffSpecificationId "
            +",ss.price"
            + " from t_production_manage_stuff s left join t_production_manage_stuff_specification ss on s." + ProductionManageStuff.COL_PUBLIC_STUFF_ID + "=ss." + ProductionManageStuffSpecification.COL_PUBLIC_STUFF_ID +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageStuffListVO> dropDown(Page<ProductionManageStuff> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuff> queryWrapper);
}
