package com.zxs.server.mapper.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;

import java.util.List;

public interface ProductionManageSuperTokenMapper  extends BaseMapper<ProductionManageSuperToken> {

    Integer SYSTEM_TYPE = 2;

    default List<ProductionManageSuperToken> getSuperTokenList(){
        QueryWrapper<ProductionManageSuperToken> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(ProductionManageSuperToken.COL_TYPE, SYSTEM_TYPE);
        List<ProductionManageSuperToken> superTokenList= selectList(queryWrapper);
        return superTokenList;
    }
}
