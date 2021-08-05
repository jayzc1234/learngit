package com.zxs.server.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-20
 */
public interface MaterialSpecificationService extends IService<MaterialSpecification> {

    /**
     * 校验单个规格在物料下是否已存在
     * @param specification
     * @param specificationUnitName
     * @param specificationTypeName
     * @param sameBatchMaterialId 该参数不能为空
     * @return
     */
    boolean uniqueCheckInSameBatchMaterial(String specification, String specificationUnitName, String specificationTypeName, String sameBatchMaterialId, String excludePublicMaterialId);

    /**
     * 校验非当前记录下的物料编码是否唯一
     * @param id
     * @param materialCode
     * @return
     */
    boolean uniqueMaterialCodeCheck(Long id, String materialCode);

    /**
     * 列表
     * @param sameBatchMaterialId
     * @param params
     * @return
     */
    IPage<MaterialSpecification> listPage(String sameBatchMaterialId, DaoSearch params);
}
