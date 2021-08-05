package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageRepairManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageRepairManageListVO;
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
public interface ProductionManageRepairManageMapper extends CommonSql<ProductionManageRepairManage> {

    @Select(START_SCRIPT
            + "select * from t_production_manage_repair_manage ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageRepairManageListVO> pageList(Page<ProductionManageRepairManageListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageRepairManageListVO> queryWrapper);

    @Select(START_SCRIPT
            + "select * from t_production_manage_repair_manage ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageRepairManageListVO> selectVoByIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageRepairManage> queryWrapper);
}
