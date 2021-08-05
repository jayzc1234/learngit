package com.zxs.server.mapper.gugeng.hydra.operationsmanagement;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.pojo.gugeng.hydra.operationsmanagement.GuGengCommonType;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-12-02
 */
public interface GuGengCommonTypeMapper extends BaseMapper<GuGengCommonType> {

    @Select(" select max(sequence) from t_gu_geng_common_type ${ew.customSqlSegment}")
    Integer selectMaxSequence(@Param(Constants.WRAPPER) QueryWrapper<GuGengCommonType> queryWrapper);
}
