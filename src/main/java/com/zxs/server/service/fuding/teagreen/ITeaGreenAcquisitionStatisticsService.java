package com.zxs.server.service.fuding.teagreen;


import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionHourDataVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAndHairyTeaStatisticsQuery;

/**
 * <p>
 * 茶青收购统计 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface ITeaGreenAcquisitionStatisticsService extends IService<TeaGreenAcquisitionStatisticsDO> {

    /**
     * 统计数据查询
     * @param model
     * @return
     */
    TeaGreenAcquisitionHourDataVO statisticsQuery(TeaGreenAndHairyTeaStatisticsQuery model);

}
