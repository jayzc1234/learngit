package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageFunctionUse;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.FunctionUseResponseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-28
 */
public interface ProductionManageFunctionUseMapper extends BaseMapper<ProductionManageFunctionUse> {

    // 添加自定义接口方法

    @Select("SELECT " + ProductionManageFunctionUse.COL_FUNCTION_NAME + ",Count(*) UseCount FROM `t_production_manage_function_use`\n" +
            " ${ew.customSqlSegment}" +
            "group by " + ProductionManageFunctionUse.COL_FUNCTION_NAME + " order by UseCount desc")
    List<FunctionUseResponseVO> selectUseCount(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageFunctionUse> queryWrapper);

}
