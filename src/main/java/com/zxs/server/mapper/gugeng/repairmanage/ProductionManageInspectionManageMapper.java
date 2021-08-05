package com.zxs.server.mapper.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageInspectionManage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairmanage.ProductionManageInspectionManageListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-09-30
 */
public interface ProductionManageInspectionManageMapper extends CommonSql<ProductionManageInspectionManage> {
    @Select(START_SCRIPT
            + "select " + ProductionManageInspectionManage.COL_ID + "," + ProductionManageInspectionManage.COL_TASK_NO + "," +
            ProductionManageInspectionManage.COL_INSPECTION_START_DATE + "," +
            ProductionManageInspectionManage.COL_INSPECTION_END_DATE + "," +
            ProductionManageInspectionManage.COL_INSPECTION_EMPLOYEE_NAME + "," +
            ProductionManageInspectionManage.COL_INSPECTION_STATUS + "," +
            ProductionManageInspectionManage.COL_INSPECTION_EMPLOYEE_ID + "," +
            ProductionManageInspectionManage.COL_INSPECTION_CONTENT + " from t_production_manage_inspection_manage " +
            "${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageInspectionManageListVO> pageList(Page<ProductionManageInspectionManageListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageInspectionManage> queryWrapper);

    @Select(START_SCRIPT
            + "select * from t_production_manage_inspection_manage ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageInspectionManageListVO> selectVOByIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageInspectionManage> ids);
}
