package com.zxs.server.mapper.anjiwhitetea;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LaborManage;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.LaborManageListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductPlantingYieldVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用工管理 Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
public interface LaborManageMapper extends BaseMapper<LaborManage> {
    /**
     * 列表
     *
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select("SELECT * " +
            "FROM t_labor_manage ${ew.customSqlSegment}")
    IPage<LaborManageListVO> pageList(Page<LaborManageListVO> page, @Param(Constants.WRAPPER) QueryWrapper<LaborManage> queryWrapper);


    @Select("SELECT DATE_FORMAT(picking_tea_time,'%Y-%m') month, sum(tea_weight) harvestQuantity  FROM `t_labor_manage`\n" +
            " ${ew.customSqlSegment} " +
            "GROUP BY DATE_FORMAT(picking_tea_time,'%Y-%m')")
    List<ProductPlantingYieldVO> getListByMonth(@Param(Constants.WRAPPER) QueryWrapper<LaborManage> queryWrapper);
}
