package com.zxs.server.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialOutWarehouse;

/**
 * <p>
 * 物料出库表 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
public interface MaterialOutWarehouseService extends IService<MaterialOutWarehouse> {
    /**
     * 出库
     * @param params
     */
    void out(MaterialInfoOutWarehouseDTO params);

    /**
     * 批量出库
     * @param params
     * @return
     */
    void batchOut(MaterialInfoBatchOutWarehouseDTO params);
}
