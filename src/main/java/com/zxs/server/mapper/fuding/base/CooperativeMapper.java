package com.zxs.server.mapper.fuding.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeBO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 联合体 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface CooperativeMapper extends BaseMapper<CooperativeDO> {

    /**
     * 获取一个组织最大的序号
     * @param organizationId 组织id
     * @return java.lang.Long 最大的序列号
     */
    @Select("select MAX(inrc_no) from t_cooperative where  organization_id = #{organizationId}")
    Long maxIncrNo(@Param("organizationId") String organizationId);

    @Select("select t.*,sum(f.tea_garden) teaGardenArea,sum(f.tea_green_acquisition_quantity) totalTeaGreenAcquisitionQuantity " +
            " from t_cooperative t " +
            " left join t_tea_farmer f on f.cooperative_id=t.cooperative_id ${ew.customSqlSegment}")
    CooperativeBO getCooperativeInfo(@Param(Constants.WRAPPER) Wrapper<CooperativeDO> queryWrapper);
}
