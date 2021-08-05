package com.zxs.server.service.common;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.dto.common.DictionaryDTO;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-09-04
 */
public interface DictionaryService extends IService<Dictionary> {
    /**
     * 获取重量单位
     * @return
     */
    List<Dictionary> listWeightUnit();

    /**
     * 更新状态
     * @param dictionary
     */
    void updateStatus(DictionaryDTO dictionary);

    /**
     * 首页产量取值
     * @return
     */
    List<Dictionary> listYieldType();

    void updateYieldType(List<String> ids);

    /**
     * 未登陆时的查询
     * @param organizationId
     * @return
     */
    List<Dictionary> selectWeightUnit(String organizationId);

    List<Dictionary> getPlantingRemind();
}
