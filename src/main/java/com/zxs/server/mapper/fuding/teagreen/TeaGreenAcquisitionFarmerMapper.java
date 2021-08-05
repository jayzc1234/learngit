package com.zxs.server.mapper.fuding.teagreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgw.supercodeplatform.advancedsearch.common.QuerySpecification;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionFarmerDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 茶青收购茶农 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-19
 */
public interface TeaGreenAcquisitionFarmerMapper extends BaseMapper<TeaGreenAcquisitionFarmerDO> {


    /**
     * 获取茶农数量
     * @param querySpecifications 数据过滤条件
     * @param  startTime 开始时间
     * @param  endTime 结束时间
     * @return int 茶农数量
     */
    int farmerCount(@Param("querySpecifications") List<QuerySpecification> querySpecifications, @Param("startTime") String startTime, @Param("endTime") String endTime);


    /**
     * 茶农数量统计
     * @param orgId
     * @param startTime
     * @param endTime
     * @return
     */
    @Select("select count(DISTINCT(farmer_id)) from t_tea_green_acquisition_farmer " +
            "where organization_id = #{orgId} and statistics_time between #{startTime} and #{endTime} ")
    int farmerCountByOrg(@Param("orgId") String orgId, @Param("startTime") String startTime, @Param("endTime") String endTime);
}
