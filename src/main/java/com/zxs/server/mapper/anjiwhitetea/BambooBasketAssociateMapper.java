package com.zxs.server.mapper.anjiwhitetea;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketAssociate;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.BambooBasketAssociateListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
public interface BambooBasketAssociateMapper  extends BaseMapper<BambooBasketAssociate> {

    /**
     * 列表
     * @param page
     * @param queryWrapper
     * @return
     */
    @Select("SELECT * " +
            "FROM t_bamboo_basket_associate ${ew.customSqlSegment}")
    IPage<BambooBasketAssociateListVO> pageList(Page<BambooBasketAssociateListVO> page, @Param(Constants.WRAPPER) QueryWrapper<BambooBasketAssociate> queryWrapper);

    // 添加自定义接口方法
}
