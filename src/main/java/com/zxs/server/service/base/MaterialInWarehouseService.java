package com.zxs.server.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchInWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInWarehouseDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInWarehouse;
import net.app315.nail.common.result.RichResult;

/**
 * <p>
 * 物料入库表 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
public interface MaterialInWarehouseService extends IService<MaterialInWarehouse> {

    /**
     * 物料入库
     * @param infoInWarehouseDTO
     */
    void add(MaterialInfoInWarehouseDTO infoInWarehouseDTO);

    /**
     * 批量入库
     * @param params
     * @return
     */
    RichResult batchIn(MaterialInfoBatchInWarehouseDTO params);
}
