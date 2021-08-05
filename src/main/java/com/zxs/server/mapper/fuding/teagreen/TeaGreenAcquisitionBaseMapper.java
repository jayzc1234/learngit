package com.zxs.server.mapper.fuding.teagreen;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionBaseDO;
import org.apache.ibatis.annotations.Param;
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
public interface TeaGreenAcquisitionBaseMapper extends BaseMapper<TeaGreenAcquisitionBaseDO> {

    /**
     * 获取当日最后一笔茶青收购记录的编号
     * @param organizationId 组织id
     * @return 当日最后一笔茶青收购记录的编号
     */
    String getTodayLastTeaGreenAcquisition(@Param("organizationId") String organizationId);

    /**
     *
     * @return
     */
    List<String> getAllTodayOrganization();
}
