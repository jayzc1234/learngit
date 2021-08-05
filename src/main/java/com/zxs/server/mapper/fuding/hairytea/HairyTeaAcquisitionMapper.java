package com.zxs.server.mapper.fuding.hairytea;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeYearOutputDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.vo.fuding.hairytea.HairyTeaAcquisitionHourStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.fuding.hairytea.HairyTeaAcquisitionRealTimeStatisticsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 毛茶收购记录 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface HairyTeaAcquisitionMapper extends BaseMapper<HairyTeaAcquisitionDO> {


    /**
     * 获取当天的毛茶收购统计数据
     * @param organizationId 组织id
     * @return HairyTeaAcquisitionRealTimeStatisticsVO 毛茶收购统计数据
     */
    HairyTeaAcquisitionRealTimeStatisticsVO getTodayHairyTeaAcquisitionStatistics(@Param("organizationId") String organizationId);


    /**
     * 获取当天的毛茶收购每小时的统计数据
     * @param organizationId 组织id
     * @return HairyTeaAcquisitionHourStatisticsVO> 毛茶每小时收购统计数据
     */
    List<HairyTeaAcquisitionHourStatisticsVO> getTodayHairyTeaAcquisitionStatisticsByHour(@Param("organizationId") String organizationId);


    /**
     * 统计某一天的的数据
     * @param organizationId 组织id
     * @param statisticTime 查询日期
     * @return 某一天的的数据
     */
    List<HairyTeaAcquisitionStatisticsDO> dayStatistic(@Param("organizationId") String organizationId,
                                                       @Param("statisticTime") String statisticTime);

    /**
     * 查询莫一天的组织
     * @param statisticTime 查询日期
     * @return 莫一天的组织列表
     */
    List<String> selectOrg(@Param("statisticTime") String statisticTime);

    @Select("select cooperative_id,cooperative_name,organization_id,organization_name," +
            "sum(quantity) quantity,sum(price * quantity) amount, DATE_FORMAT(now(),'%Y') currentYear " +
            "from t_hairy_tea_acquisition " +
            "where cooperative_id = #{cooperativeId} and create_time like concat( #{year}, '%') " +
            "group by cooperative_id")
    List<CooperativeYearOutputDO> selectCooperativeYearOutPut(@Param("year") String year, @Param("cooperativeId") String cooperativeId);
}
