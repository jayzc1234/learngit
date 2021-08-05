package com.zxs.server.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.base.MaterialFieldListDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInventoryListDTO;
import net.app315.hydra.intelligent.planting.dto.base.SetMaterialWarningDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInventory;
import net.app315.hydra.intelligent.planting.vo.base.*;

import java.math.BigDecimal;

/**
 * <p>
 * 物料库存表 服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-24
 */
public interface MaterialInventoryService extends IService<MaterialInventory> {

    /**
     *  列表
     * @param materialInfoInventoryListDTO
     * @return
     */
    IPage<MaterialInfoInventoryListVO> pageList(MaterialInfoInventoryListDTO materialInfoInventoryListDTO);

    /**
     * 物料出入库流水
     * @param params
     * @return
     */
    IPage<WarehouseOutAndInInfoListVO> warehouseOutAndInInfo(DaoSearch params);

    /**
     * 单个物料出入库流水
     * @param publicMaterialId
     * @param materialSpecificationId
     * @param daoSearch
     * @return
     */
    IPage<WarehouseMaterialInfoListVO> listSingleMaterialWarehouseInOutFlow(String publicMaterialId, Long materialSpecificationId, DaoSearch daoSearch);

    /**
     * 单个物料出入库详情
     * @param publicMaterialId
     * @param materialSpecificationId
     * @param params
     * @return
     */
    IPage<SingleMaterialInOutWarehouseListVO> listSingleMaterialOutWarehouseInfo(String publicMaterialId, Long materialSpecificationId, DaoSearch params);

    /**
     * 设置库存预警
     * @param params
     */
    void setWarning(SetMaterialWarningDTO params);

    /**
     * 根据物料id获取该物料入库批次列表
     * @param publicMaterialId
     * @param materialSpecificationId
     * @param daoSearch
     * @return
     */
    IPage<WarehouseMaterialInfoListVO> listBatchByMaterialId(String materialBatch, String publicMaterialId, Long materialSpecificationId, DaoSearch daoSearch);

    /**
     * 出库时，获取物料列表详情
     * @param materialName
     * @param materialSortId
     * @param materialSpecificationId
     * @param daoSearch
     * @return
     */
    IPage<PublicMaterialInfoListViewVO> listMaterialOutInfo(String materialName, String materialSortId, Long materialSpecificationId, String publicMaterialId, DaoSearch daoSearch);

    /**
     * 校验物料是否符合出库条件
     * @param publicMaterialId
     * @param materialBatch
     * @param outboundNum
     * @param materialSpecificationId
     */
    void checkMaterialIsAdequacy(String publicMaterialId, String materialBatch, BigDecimal outboundNum, Long materialSpecificationId);

    /**
     * 获取出入库编号
     * @return
     */
    String getOutAndInCode();

    /**
     *根据字段父获取物料列表（包括搜索）
     * @param params
     * @return
     */
    IPage<PublicMaterialInfoListViewVO> enableMaterialsByField(MaterialFieldListDTO params);

}
