package com.zxs.server.mapper.fuding.hairytea;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionCooperativeDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 毛茶收购联合体 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-19
 */
public interface HairyTeaAcquisitionCooperativeMapper extends BaseMapper<HairyTeaAcquisitionCooperativeDO> {

    /**
     * 查询联合体的数量
     * @param querySpecifications 数据过滤条件
     * @param   startTime 开始时间
     * @param   endTime 结束时间
     * @return int 联合体数量
     */
    int cooperativeCount(@Param("querySpecifications") List<QuerySpecification> querySpecifications, @Param("startTime") String startTime, @Param("endTime") String endTime);


    /**
     * 查询联合体的数量
     * @param   startTime 开始时间
     * @param   endTime 结束时间
     * @return int 联合体数量
     */
    @Select("select count(DISTINCT(cooperative_id)) from t_hairy_tea_acquisition_cooperative " +
            "where organization_id = #{orgId} and statistics_time between #{startTime} and #{endTime} ")
    int cooperativeCountByOrg(@Param("orgId") String orgId, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
