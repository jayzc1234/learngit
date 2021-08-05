package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoUpdateDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSpecificationDTO;
import net.app315.hydra.intelligent.planting.enums.EnableOrDisEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInfoMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInventoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialSpecificationMapper;
import net.app315.hydra.intelligent.planting.server.service.AbstractPageSearchService;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInfoService;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialSpecificationService;
import net.app315.hydra.intelligent.planting.server.util.BaseMapperUtil;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoDetailVO;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-20
 */
@Service
public class MaterialInfoServiceImpl extends AbstractPageSearchService<MaterialInfoMapper, MaterialInfo> implements MaterialInfoService {

    @Autowired
    private MaterialSpecificationMapper specificationMapper;

    @Autowired
    private MaterialSpecificationService specificationService;

    @Autowired
    private MaterialInventoryMapper inventoryMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CommonUtil commonUtil;
    /**
     * 1.校验规格不可为空
     * 2.校验相同规格，需要检查参数的规格是否有重复，还需要检查与数据库中当前物料类目及该名称下的物料是否有相同规格，后者前提是该物料之前已经添加过规格
     * 3.校验物料编号是否重复，校验范围为组织和系统内
     * 3.1 校验相同类型和物料名称的物料是否存在，存在的话sameBatchId使用已存在的值
     * 4.设置物料编号
     * 5.生成物料id及一批规格唯一id
    * 6.批量插入物料和规格
     * 7.发布异步事件通知基础服务规格被使用了
     * @apiNote 该方法的规格和物料编号唯一性校验在并发环境下将失效，由于事务隔离级别为可重复读，在当前事务中查询校验通过后
     * 如果此时其它事务执行了插入或者在当前事务开启后其它事务执行了插入，当前事务是无法感知的。
     * @param materialInfoDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(MaterialInfoDTO materialInfoDTO) {
        //1.校验规格为空
        List<MaterialSpecificationDTO> list_specificationModels = materialInfoDTO.getMaterialSpecificationModels();
        if (CollectionUtils.isEmpty(list_specificationModels)){
            CommonUtil.throwSuperCodeExtException(500,"规格不可为空");
        }
        //2.校验规格在系统组织及相同物料下唯一性，使用set集合去重与list集合长度比较
        Set<MaterialSpecificationDTO> set_specificationDTOS = new HashSet<>(list_specificationModels);
        if (set_specificationDTOS.size() != list_specificationModels.size()){
            CommonUtil.throwSuperCodeExtException(500,"规格参数不可填写重复数据");
        }
        //3.校验规格与数据库中规格是否有重复
        specUniqueCheck(materialInfoDTO, list_specificationModels,null);

        //4.校验物料编号唯一性
        materialCodeUniqueCheck(list_specificationModels, null);

        //5.遍历规格填充数据准备插入，先校验是否之前已经添加过相同名称和类型的物料

        String sameBatchMaterialId = null;
        LambdaQueryWrapper<MaterialInfo> objectLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<MaterialInfo> materialInfos1 = baseMapper.selectList(objectLambdaQueryWrapper.eq(MaterialInfo::getMaterialName, materialInfoDTO.getMaterialName()).
                eq(MaterialInfo::getMaterialSortId, materialInfoDTO.getMaterialSortId()).
                eq(MaterialInfo::getOrganizationId, commonUtil.getOrganizationId()).
                eq(MaterialInfo::getSysId, commonUtil.getSysId()));

        if (CollectionUtils.isNotEmpty(materialInfos1)){
            sameBatchMaterialId = materialInfos1.get(0).getSameBatchMaterialId();
        }else {
            sameBatchMaterialId = commonUtil.getUUID();
        }
        List<MaterialInfo> materialInfos = new ArrayList<>();
        List<MaterialSpecification> specifications = new ArrayList<>();
        for (MaterialSpecificationDTO specificationModel : list_specificationModels) {
            //封装物料
            MaterialInfo materialInfo = new MaterialInfo();
            BeanUtils.copyProperties(materialInfoDTO,materialInfo);
            String publicMaterialId = commonUtil.getUUID();
            materialInfo.setPublicMaterialId(publicMaterialId);
            materialInfo.setSameBatchMaterialId(sameBatchMaterialId);
            materialInfo.setDisableFlag(EnableOrDisEnum.ENABLE.getStatus());
            materialInfo.initDisableFlagName();
            //封装规格
            MaterialSpecification specification = new MaterialSpecification();
            BeanUtils.copyProperties(specificationModel,specification);
            specification.setSameBatchMaterialId(sameBatchMaterialId);
            specification.setPublicMaterialId(publicMaterialId);
            specification.setSpecificationInfo(getSpecificationInfo(specificationModel.getSpecification(),specificationModel.getSpecificationUnitName(),specificationModel.getSpecificationTypeName()));
            String materialCode = generateMaterialCode(specification.getMaterialCode(),null);
            specification.setMaterialCode(materialCode);
            materialInfos.add(materialInfo);
            specifications.add(specification);
        }
        saveBatch(materialInfos);
        specificationService.saveBatch(specifications);

        //TODO 发布异步事件通知基础平台规格已被使用
    }

    /**
     * 编辑物料
     * 1.校验物料名称在当前物料分类下的唯一性
     * 2.校验单个物料规格在当前物料中是否已存在
     * 3.校验物料编号是否已存在,校验范围是系统企业
     * 4.更新物料和规格
     * @param id
     * @param materialInfoUpdateDTO
     */
    @Override
    public void update(Long id, MaterialInfoUpdateDTO materialInfoUpdateDTO) {
        Hashtable<String,Object> filterMap = new Hashtable<>();
        filterMap.put(MaterialInfo.COL_MATERIAL_SORT_ID,materialInfoUpdateDTO.getMaterialSortId());
        //1.物料名称唯一校验
        BaseMapperUtil.singleTableUniqueFieldCheck(id, MaterialInfo.COL_MATERIAL_NAME, materialInfoUpdateDTO.getMaterialName(), commonUtil.getOrganizationId(), commonUtil.getSysId(), filterMap, baseMapper);
        MaterialInfo materialInfo =BaseMapperUtil.recordExistCheckThrowError(id,baseMapper);
                //2.校验物料规格是否已存在，在当前物料生成的所有规格中使用当前参数的规格与其它的规格做比较，范围使用sameBatchMaterialId来确定
        String sameBatchMaterialId = materialInfo.getSameBatchMaterialId();
        MaterialSpecificationDTO materialSpecificationDTO = materialInfoUpdateDTO.getMaterialSpecificationDTO();
        boolean unique = specificationService.uniqueCheckInSameBatchMaterial(materialSpecificationDTO.getSpecification(),materialSpecificationDTO.getSpecificationUnitName(),materialSpecificationDTO.getSpecificationTypeName(),sameBatchMaterialId,materialInfo.getPublicMaterialId());
        if (!unique){
            CommonUtil.throwSuperCodeExtException(500,"物料规格已存在");
        }

        //3.校验物料编码是否重复
        boolean unique2 =specificationService.uniqueMaterialCodeCheck(materialSpecificationDTO.getId(),materialSpecificationDTO.getMaterialCode());
        if (!unique2){
            CommonUtil.throwSuperCodeExtException(500,"物料编号已存在");
        }

        //4.更新物料与规格
        BeanUtils.copyProperties(materialInfoUpdateDTO,materialInfo);
        MaterialSpecification materialSpecification = new MaterialSpecification();
        BeanUtils.copyProperties(materialSpecificationDTO,materialSpecification);
        materialSpecification.setSpecificationInfo(getSpecificationInfo(materialSpecificationDTO.getSpecification(),materialSpecificationDTO.getSpecificationUnitName(),materialSpecificationDTO.getSpecificationTypeName()));
        materialInfo.initDisableFlagName();
        baseMapper.updateById(materialInfo);
        specificationMapper.updateById(materialSpecification);
    }

    /**
     * 请注意该接口返回的规格是个数组，目的是为了防止查看当前物料所有规格
     * 与之前设计保持一致
     * @param id
     * @return
     */
    @Override
    public MaterialInfoDetailVO getDetailById(Long id) {
        MaterialInfoDetailVO materialInfoDetailVO = baseMapper.selectDetailById(id);
        return materialInfoDetailVO;
    }

    @Override
    public void disable(Long id, Integer disableFlag) {
        MaterialInfo materialInfo = BaseMapperUtil.recordExistCheck(id, baseMapper);
        CustomAssert.isNull(materialInfo,"记录不存在");
        if (disableFlag.equals(0) || disableFlag.equals(1)){
            materialInfo.setDisableFlag(disableFlag);
        }
        materialInfo.initDisableFlagName();
        baseMapper.updateById(materialInfo);
    }

    @Override
    public <P extends DaoSearch> IPage<MaterialInfoListVO> pageList(Integer disableFlag, Long materialSortId,P daoSearch) {
        Page<MaterialInfoListVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        QueryWrapper<MaterialInfo> queryWrapper = listMaterialInfoQueryWrapper(disableFlag, materialSortId, daoSearch);
        IPage<MaterialInfoListVO> iPage = baseMapper.pageList(page,queryWrapper);
        return iPage;
    }

    private <P extends DaoSearch> QueryWrapper<MaterialInfo> listMaterialInfoQueryWrapper(Integer disableFlag, Long materialSortId, P daoSearch) {
        QueryWrapper<MaterialInfo> queryWrapper = commonUtil.queryTemplate(MaterialInfo.class);
        String search = daoSearch.getSearch();
        final String M_PREFIX = "m.";
        if (StringUtils.isNotBlank(search)){
            final String SPEC_PREFIX = "spec.";
            queryWrapper.and(w->w.like(SPEC_PREFIX+ MaterialSpecification.COL_MATERIAL_CODE,search)
                                .or().like(M_PREFIX+MaterialInfo.COL_MATERIAL_NAME,search).like(M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_NAME,search)
                                .or().like(SPEC_PREFIX+MaterialSpecification.COL_SPECIFICATION_INFO,search));
        }
        queryWrapper.eq(!Objects.isNull(disableFlag),M_PREFIX+MaterialInfo.COL_DISABLE_FLAG,disableFlag);
        queryWrapper.eq(!Objects.isNull(materialSortId),M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_ID,materialSortId);
        queryWrapper.orderByDesc(M_PREFIX+MaterialInfo.COL_ID);
        return queryWrapper;
    }

    @Override
    public IPage<MaterialInfoListVO> dropPage(Long materialSortId, String materialName, DaoSearch daoSearch) {
        Page<MaterialInfoListVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        final String M_PREFIX = "m.";
        QueryWrapper<MaterialInfo> queryWrapper = listMaterialInfoQueryWrapper(1, materialSortId, daoSearch);
        queryWrapper.groupBy(M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_ID,M_PREFIX+MaterialInfo.COL_SAME_BATCH_MATERIAL_ID);
        IPage<MaterialInfoListVO> iPage = baseMapper.pageList(page,queryWrapper);
        return iPage;
    }

    /**
     * 生成物料编码
     * @param materialCode：参数物料编码做了唯一性校验，不为空直接返回
     * @param publicMaterialId：不为空的话则表示需要排除
     * @return
     */
    private String generateMaterialCode(String materialCode,String publicMaterialId) {
        if (StringUtils.isNotBlank(materialCode)){
            return materialCode;
        }
        StringBuilder stringBuilder = null;
        final String table_prefix = "m.";
        QueryWrapper<MaterialSpecification> specificationQueryWrapper = new QueryWrapper<>();
        specificationQueryWrapper.eq(table_prefix+ MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        specificationQueryWrapper.ne(StringUtils.isNotBlank(publicMaterialId),table_prefix+MaterialInfo.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
        while (true) {
            stringBuilder = new StringBuilder(32);
            stringBuilder.append("WL");
            long n = redisUtil.generate(RedisKey.ORG_MATERIAL_SERIALNUMBER + commonUtil.getOrganizationId());
            int count = 6 - String.valueOf(n).length();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    stringBuilder.append("0");
                }
                stringBuilder.append(n);
            } else {
                stringBuilder.append(n);
            }
            specificationQueryWrapper.eq(MaterialSpecification.COL_MATERIAL_CODE,stringBuilder.toString());
            Integer integer = specificationMapper.materialCodeUniqueCheck(specificationQueryWrapper);
            if (null == integer || integer == 0) {
                break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 物料编号唯一性校验
     * @param list_specificationModels
     */
    private void materialCodeUniqueCheck(List<MaterialSpecificationDTO> list_specificationModels,String publicMaterialId) {
        List<String> materialCodeList = new ArrayList<>();
        for (MaterialSpecificationDTO list_specificationModel : list_specificationModels) {
            String materialCode = list_specificationModel.getMaterialCode();
            if (StringUtils.isNotBlank(materialCode)){
                materialCodeList.add(materialCode);
            }
        }
        if (CollectionUtils.isNotEmpty(materialCodeList)){
            Set<String> materialCodeSet = new HashSet<>(materialCodeList);
            if (materialCodeSet.size() != materialCodeList.size()){
                CommonUtil.throwSuperCodeExtException(500,"物料编号不可重复");
            }
            final String table_prefix = "m.";
            QueryWrapper<MaterialSpecification> specificationQueryWrapper = new QueryWrapper<>();
            specificationQueryWrapper.eq(table_prefix+ MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
            specificationQueryWrapper.in(MaterialSpecification.COL_MATERIAL_CODE,materialCodeList);
            specificationQueryWrapper.ne(StringUtils.isNotBlank(publicMaterialId),table_prefix+MaterialInfo.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
            Integer count = specificationMapper.materialCodeUniqueCheck(specificationQueryWrapper);
            if (null != count && count > 0){
                CommonUtil.throwSuperCodeExtException(500,"物料编号不可重复");
            }
        }
    }

    /**
     * 对请求规格参数与数据库中的规格做唯一性校验
     * @param materialInfoDTO
     * @param list_specificationModels
     * @param publicMaterialId
     */
    private void specUniqueCheck(MaterialInfoDTO materialInfoDTO, List<MaterialSpecificationDTO> list_specificationModels,String publicMaterialId) {
        final String table_prefix = "m.";
        QueryWrapper<MaterialSpecification> specificationQueryWrapper = new QueryWrapper<>();
        specificationQueryWrapper.eq(table_prefix+ MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        specificationQueryWrapper.eq(table_prefix+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        specificationQueryWrapper.eq(table_prefix+MaterialInfo.COL_MATERIAL_NAME,materialInfoDTO.getMaterialName());
        specificationQueryWrapper.eq(table_prefix+MaterialInfo.COL_MATERIAL_SORT_ID,materialInfoDTO.getMaterialSortId());
        specificationQueryWrapper.ne(StringUtils.isNotBlank(publicMaterialId),table_prefix+MaterialInfo.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
        List<MaterialSpecification> specifications = specificationMapper.filterWStIdAndMName(specificationQueryWrapper);
        if (CollectionUtils.isNotEmpty(specifications)){
            for (MaterialSpecification specification : specifications) {
                String sp = specification.toString();
                for (MaterialSpecificationDTO list_specificationModel : list_specificationModels) {
                    String sp2 = list_specificationModel.toString();
                    if (sp2.equals(sp)){
                        CommonUtil.throwSuperCodeExtException(500,"规格已存在");
                    }
                }
            }
        }
    }

    /**
     * 封装specificationInfo
     * @param specification
     * @param unitName
     * @param typeName
     * @return
     */
    private String getSpecificationInfo(String specification, String unitName, String typeName) {
        String specificationInfo = null;
        if (StringUtils.isBlank(unitName)) {
            specificationInfo = specification + typeName;
        } else if (StringUtils.isBlank(typeName)) {
            specificationInfo = specification + unitName;
        } else {
            specificationInfo = specification + unitName + "/" + typeName;
        }
        return specificationInfo;
    }

}
