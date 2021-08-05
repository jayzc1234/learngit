package com.zxs.server.service.fuding.common;

import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;

public interface ISynchronizeDataService {

    /**
     * 同步联合体数据
     * @param cooperativeDO
     */
    void synchronizeCooperativeInfo(CooperativeDO cooperativeDO);

    /**
     * 同步茶农数据
     * @param teaFarmerDO
     */
    void synchronizeTeaFarmerInfo(TeaFarmerDO teaFarmerDO);
}
