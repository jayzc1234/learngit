package com.zxs.server.mapper.fuding.teagreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeYearOutputDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerYearOutputDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionHourStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionRealTimeStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 茶青收购记录 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
@Component
public interface TeaGreenAcquisitionMapper extends BaseMapper<TeaGreenAcquisitionDO> {


    /**
     * 获取茶青收购记录统计数据
     * @param organizationId 组织id
     * @return 茶青收购记录统计数据
     */
    TeaGreenAcquisitionRealTimeStatisticsVO getTodayTeaGreenAcquisitionStatistics(@Param("organizationId") String organizationId);


    /**
     * 获取茶青收购记录每小时统计数据
     * @param organizationId 组织id
     * @return 茶青收购记录每小时统计数据
     */
    List<TeaGreenAcquisitionHourStatisticsVO> getTodayTeaGreenAcquisitionStatisticsByHour(@Param("organizationId") String organizationId);

    /**
     * 统计某一天的的数据
     * @param organizationId 组织id
     * @param statisticTime 时间
     * @return  某一天的的数据
     */
    List<TeaGreenAcquisitionStatisticsDO> dayStatistic(@Param("organizationId") String organizationId,
                                                       @Param("statisticTime") String statisticTime);

    /**
     * 统计某一天的茶农数据
     * @param organizationId 组织id
     * @param statisticTime 时间
     * @return 某一天的的数据
     */
    List<TeaGreenAcquisitionFarmerDO> dayTeaFarmers(@Param("organizationId") String organizationId,
                                                    @Param("statisticTime") String statisticTime);
    /**
     * 查询莫一天的组织
     * @param statisticTime 查询时间
     * @return 组织id列表
     */
    List<String> selectOrg(@Param("statisticTime") String statisticTime);

    @Select("select farmer_id,farmer_name,cooperative_id,cooperative_name,organization_id,organization_name," +
            "sum(quantity) quantity,sum(price * quantity) amount, DATE_FORMAT(now(),'%Y') currentYear " +
            "from t_tea_green_acquisition " +
            "where farmer_id = #{farmerId} and create_time like concat( #{year}, '%') " +
            "group by farmer_id")
    List<TeaFarmerYearOutputDO> selectFarmerYearOutPut(@Param("year") String year, @Param("farmerId") String farmerId);


    @Select("select cooperative_id,cooperative_name,organization_id,organization_name," +
            "sum(quantity) quantity,sum(price * quantity) amount, DATE_FORMAT(now(),'%Y') currentYear " +
            "from t_tea_green_acquisition " +
            "where cooperative_id = #{cooperativeId} and create_time like concat( #{year}, '%') " +
            "group by cooperative_id")
    List<CooperativeYearOutputDO> selectCooperativeYearOutPut(@Param("year") String year, @Param("cooperativeId") String cooperativeId);
}
