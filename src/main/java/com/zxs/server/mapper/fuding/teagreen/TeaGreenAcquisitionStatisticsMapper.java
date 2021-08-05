package com.zxs.server.mapper.fuding.teagreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 茶青收购统计 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface TeaGreenAcquisitionStatisticsMapper extends BaseMapper<TeaGreenAcquisitionStatisticsDO> {

    /**
     * 根据品类统计交易笔数 交易重量 交易中金额
     * @param orgId
     * @param startTime
     * @param endTime
     * @return
     */
//    @Select("select product_name,product_sort_name,sum(trading_volume) trading_volume,sum(amount),amount  ,sum(quantity) quantity from t_tea_green_acquisition_statistics " +
//            "where organization_id = #{orgId} and statistics_time between #{startTime} and #{endTime} group by product_id")
    List<TeaGreenAcquisitionStatisticsDO> categoryStstisByOrg(@Param("orgId") String orgId,
                                                              @Param("startTime") String startTime,
                                                              @Param("endTime") String endTime);


    /**
     * 根据品类统计交易笔数 交易重量 交易中金额
     * @param orgId
     * @param startTime
     * @param endTime
     * @return
     */
//    @Select("select sum(trading_volume) trading_volume,sum(amount),amount  ,sum(quantity) quantity,cooperative_name from t_tea_green_acquisition_statistics " +
//            "where organization_id = #{orgId} and statistics_time between #{startTime} and #{endTime} group by cooperative_id")
    List<TeaGreenAcquisitionStatisticsDO> cooperativeStstisByOrg(@Param("orgId") String orgId,
                                                                 @Param("startTime") String startTime,
                                                                 @Param("endTime") String endTime);

    /**
     * 根据品类统计交易笔数 交易重量 交易中金额
     * @param orgId
     * @param startTime
     * @param endTime
     * @return
     */
//    @Select("select sum(trading_volume) trading_volume,sum(amount),amount  ,sum(quantity) quantity,statistics_time from t_tea_green_acquisition_statistics " +
//            "where organization_id = #{orgId} and statistics_time between #{startTime} and #{endTime} group by statistics_time order by statistics_time")
    List<TeaGreenAcquisitionStatisticsDO> dayStstisByOrg(@Param("orgId") String orgId,
                                                         @Param("startTime") String startTime,
                                                         @Param("endTime") String endTime);

}
