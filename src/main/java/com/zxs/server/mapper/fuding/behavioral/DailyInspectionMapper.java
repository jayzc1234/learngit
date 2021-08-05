package com.zxs.server.mapper.fuding.behavioral;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.DailyInspectionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 日常巡检 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface DailyInspectionMapper extends BaseMapper<DailyInspectionDO> {

    /**
     * 获取当日最后一条巡检记录的编号
     * @param organizationId 组织id
     * @return 当日最后一条巡检记录的编号
     */
    String getTodayLastDailyInspectionNo(@Param("organizationId") String organizationId);

    List<String> getAllOrganization();
}
