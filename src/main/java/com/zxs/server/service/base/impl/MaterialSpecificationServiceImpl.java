package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInfoMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialSpecificationMapper;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialSpecificationService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-20
 */
@Service
public class MaterialSpecificationServiceImpl extends ServiceImpl<MaterialSpecificationMapper, MaterialSpecification> implements MaterialSpecificationService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private MaterialInfoMapper materialInfoMapper;

    @Override
    public boolean uniqueCheckInSameBatchMaterial(String specification, String specificationUnitName, String specificationTypeName,
                                                  String sameBatchMaterialId, String excludePublicMaterialId) {
        QueryWrapper<MaterialSpecification> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isEmpty(sameBatchMaterialId)) {
            CommonUtil.throwSuperCodeExtException(500,"校验规格是否存在，同一批次不能为空");
        }
        queryWrapper.ne(StringUtils.isNotBlank(excludePublicMaterialId),MaterialSpecification.COL_public_material_id,excludePublicMaterialId);
        queryWrapper.eq(MaterialSpecification.COL_SPECIFICATION,specification);
        queryWrapper.eq(MaterialSpecification.COL_SAME_BATCH_MATERIAL_ID,sameBatchMaterialId);
        //设置specificationUnitName，如果为空则使用isnull查询
        if (StringUtils.isBlank(specificationUnitName)){
            queryWrapper.isNotNull(MaterialSpecification.COL_SPECIFICATION_UNIT_NAME);
        }else {
            queryWrapper.eq(MaterialSpecification.COL_SPECIFICATION_UNIT_NAME,specificationUnitName);
        }

        //设置specificationTypeName，如果为空则使用isnull查询
        if (StringUtils.isBlank(specificationTypeName)){
            queryWrapper.isNotNull(MaterialSpecification.COL_SPECIFICATION_TYPE_NAME);
        }else {
            queryWrapper.eq(MaterialSpecification.COL_SPECIFICATION_TYPE_NAME,specificationTypeName);
        }
        Integer count = baseMapper.selectCount(queryWrapper);

        return count == null || count == 0;
    }

    @Override
    public boolean uniqueMaterialCodeCheck(Long id, String materialCode) {
        if (StringUtils.isBlank(materialCode)){
            return true;
        }
        MaterialSpecification specification = baseMapper.selectById(id);
        //编号没变直接返回
        String materialCode1 = specification.getMaterialCode();
        if (materialCode.equals(materialCode1)){
            return true;
        }
        QueryWrapper<MaterialSpecification> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("s."+MaterialSpecification.COL_ID,id);
        queryWrapper.eq("s."+MaterialSpecification.COL_MATERIAL_CODE,materialCode);

        queryWrapper.eq("m."+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq("m."+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        Integer count = baseMapper.materialCodeUniqueCheck(queryWrapper);
        return null == count || count ==0;
    }

    @Override
    public IPage<MaterialSpecification> listPage(String sameBatchMaterialId, DaoSearch params) {
        QueryWrapper<MaterialInfo> materialInfoQueryWrapper=new QueryWrapper<>();
        materialInfoQueryWrapper.eq(MaterialInfo.COL_SAME_BATCH_MATERIAL_ID, sameBatchMaterialId);
        List<MaterialInfo> materialInfoList= materialInfoMapper.selectList(materialInfoQueryWrapper);
        String materialName = null;
        if (CollectionUtils.isNotEmpty(materialInfoList)){
            materialName = materialInfoList.get(0).getMaterialName();
        }

        Page<MaterialSpecification> page = new Page<>(params.getDefaultCurrent(),params.getDefaultPageSize());
        QueryWrapper<MaterialInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("m."+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq("m."+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        queryWrapper.eq(StringUtils.isNotBlank(materialName),"m."+MaterialInfo.COL_MATERIAL_NAME,materialName);
        queryWrapper.like(StringUtils.isNotBlank(params.getSearch()), MaterialSpecification.COL_SPECIFICATION_INFO, params.getSearch());
        return baseMapper.listPage(page,queryWrapper);

    }
}
