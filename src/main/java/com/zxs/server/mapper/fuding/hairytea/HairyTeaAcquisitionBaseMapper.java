package com.zxs.server.mapper.fuding.hairytea;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionBaseDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 毛茶收购记录 Mapper 接口
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface HairyTeaAcquisitionBaseMapper extends BaseMapper<HairyTeaAcquisitionBaseDO> {

    /**
     * 获取当日最后一笔毛茶收购记录的编号
     * @param organizationId 组织id
     * @return 当日最后一笔毛茶收购记录的编号
     */
    String getTodayLastHairyTeaAcquisition(@Param("organizationId") String organizationId);


    List<String> getAllTodayOrganization();
}
