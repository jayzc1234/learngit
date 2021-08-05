package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffSort;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffSortVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
public interface ProductionManageStuffSortMapper extends CommonSql<ProductionManageStuffSort> {
    @Select(START_SCRIPT
            + "select * from t_production_manage_stuff_sort ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageStuffSortVO> selectVoList(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageStuffSortVO> queryWrapper);
}
