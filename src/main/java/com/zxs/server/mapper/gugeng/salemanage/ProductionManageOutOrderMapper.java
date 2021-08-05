package com.zxs.server.mapper.gugeng.salemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOutOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.CommonSql;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOutOrderListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-06-13
 */
public interface ProductionManageOutOrderMapper extends CommonSql<ProductionManageOutOrder> {

    @Select(START_SCRIPT
            +"select * from t_production_manage_out_order " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    List<ProductionManageOutOrderListVO> selectByIds(@Param(Constants.WRAPPER) QueryWrapper<ProductionManageOutOrderListVO> queryWrapper);

    @Select(START_SCRIPT
            +"select * from t_production_manage_out_order " +
            " ${ew.customSqlSegment}"
            + END_SCRIPT
    )
    IPage<ProductionManageOutOrderListVO> pageList(Page<ProductionManageOutOrderListVO> page, @Param(Constants.WRAPPER) QueryWrapper<ProductionManageOutOrder> queryWrapper);
}
