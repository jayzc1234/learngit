package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.base.MaterialFieldListDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInventoryListDTO;
import net.app315.hydra.intelligent.planting.dto.base.SetMaterialWarningDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.base.*;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInventoryMapper;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInventoryService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.base.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 物料库存表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-24
 */
@Service
public class MaterialInventoryServiceImpl extends ServiceImpl<MaterialInventoryMapper, MaterialInventory> implements MaterialInventoryService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public IPage<MaterialInfoInventoryListVO> pageList(MaterialInfoInventoryListDTO inventoryListDTO) {
        QueryWrapper<MaterialInventory> queryWrapper = new QueryWrapper<>();
        final String SPEC_PREFIX = "spec.";
        final String M_PREFIX = "m.";
        final String IN_PREFIX = "mi.";
        if (inventoryListDTO.isAdvanceSearch()){
            queryWrapper.eq(StringUtils.isNotBlank(inventoryListDTO.getMaterialCode()), MaterialSpecification.COL_MATERIAL_CODE,inventoryListDTO.getMaterialCode());
            queryWrapper.eq(StringUtils.isNotBlank(inventoryListDTO.getMaterialName()), MaterialInfo.COL_MATERIAL_NAME,inventoryListDTO.getMaterialName());
            queryWrapper.eq(StringUtils.isNotBlank(inventoryListDTO.getMaterialSortName()), MaterialInfo.COL_MATERIAL_SORT_NAME,inventoryListDTO.getMaterialSortName());
        }else if (StringUtils.isNotBlank(inventoryListDTO.getSearch())){
            String search = inventoryListDTO.getSearch();
            queryWrapper.and(w->w.like(SPEC_PREFIX+MaterialSpecification.COL_MATERIAL_CODE,search).
                                      or().like(M_PREFIX+MaterialInfo.COL_MATERIAL_NAME,search).
                                      or().like(M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_NAME,search).
                                      or().like(SPEC_PREFIX+MaterialSpecification.COL_SPECIFICATION_INFO,search));
        }
        queryWrapper.orderByDesc(IN_PREFIX+MaterialInventory.COL_ID);
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        Page<MaterialInfoInventoryListVO> page = new Page<>(inventoryListDTO.getDefaultCurrent(),inventoryListDTO.getDefaultPageSize());
        IPage<MaterialInfoInventoryListVO> iPage = baseMapper.pageList(page,queryWrapper);
        return iPage;
    }

    @Override
    public IPage<WarehouseOutAndInInfoListVO> warehouseOutAndInInfo(DaoSearch params) {
        Page<WarehouseOutAndInInfoListVO> page = new Page<>(params.getDefaultCurrent(),params.getDefaultPageSize());
        QueryWrapper<MaterialInventory> queryWrapper = new QueryWrapper<>();
        final String MATERIAL_PREFIX = "b.";
        if (StringUtils.isNotBlank(params.getSearch())){
            String search = params.getSearch();
            final String SPEC_PREFIX = "spec.";
            queryWrapper.and(w->w.like(SPEC_PREFIX+MaterialSpecification.COL_MATERIAL_CODE,search).
                    or().like(SPEC_PREFIX+MaterialSpecification.COL_SPECIFICATION_INFO,search).
                    or().like(MATERIAL_PREFIX+MaterialInfo.COL_MATERIAL_SORT_NAME,search).
                    or().like(MATERIAL_PREFIX+MaterialInfo.COL_MATERIAL_NAME,search));
        }
        queryWrapper.eq(MATERIAL_PREFIX+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(MATERIAL_PREFIX+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        IPage<WarehouseOutAndInInfoListVO> iPage=baseMapper.warehouseOutAndInInfo(page,queryWrapper);
        return iPage;
    }


    @Override
    public IPage<WarehouseMaterialInfoListVO> listSingleMaterialWarehouseInOutFlow(String publicMaterialId, Long materialSpecificationId, DaoSearch daoSearch) {
        Page<WarehouseMaterialInfoListVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        QueryWrapper<MaterialOutWarehouse> queryWrapper = new QueryWrapper<>();
        final String OUT_PREFIX = "a.";
        final String M_PREFIX = "b.";
        if (StringUtils.isNotBlank(daoSearch.getSearch())){
            String search = daoSearch.getSearch();
            queryWrapper.and(w->w.like(OUT_PREFIX+MaterialOutWarehouse.COL_MATERIAL_BATCH,search).
                    or().like(OUT_PREFIX+MaterialOutWarehouse.COL_STOREHOUSE_NAME,search));
        }
        queryWrapper.eq(OUT_PREFIX+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(OUT_PREFIX+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        queryWrapper.eq(OUT_PREFIX+MaterialInfo.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
        IPage<WarehouseMaterialInfoListVO> iPage=baseMapper.listSingleMaterialWarehouseInOutFlow(page,queryWrapper,publicMaterialId,commonUtil.getOrganizationId());
        return iPage;
    }

    @Override
    public IPage<SingleMaterialInOutWarehouseListVO> listSingleMaterialOutWarehouseInfo(String publicMaterialId, Long materialSpecificationId, DaoSearch daoSearch) {
        Page<SingleMaterialInOutWarehouseListVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        QueryWrapper<MaterialOutWarehouse> queryWrapper = new QueryWrapper<>();
        final String M_PREFIX = "b.";
        final String A_PREFIX = "a.";
        final String SPEC_PREFIX = "spec.";
        if (StringUtils.isNotBlank(daoSearch.getSearch())){
            String search = daoSearch.getSearch();
            queryWrapper.and(w->w.like(M_PREFIX+MaterialInfo.COL_MATERIAL_NAME,search).
                    or().like(A_PREFIX+MaterialOutWarehouse.COL_MATERIAL_BATCH,search));
        }
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
//        queryWrapper.eq(!Objects.isNull(materialSpecificationId),M_PREFIX+MaterialSpecification.COL_ID,materialSpecificationId);
        IPage<SingleMaterialInOutWarehouseListVO> iPage=baseMapper.listSingleMaterialOutWarehouseInfo(page,queryWrapper);
        return iPage;
    }

    @Override
    public void setWarning(SetMaterialWarningDTO params) {
        QueryWrapper<MaterialInventory> queryWrapper = commonUtil.queryTemplate(MaterialInventory.class);
        queryWrapper.eq(MaterialInventory.COL_PUBLIC_MATERIAL_ID,params.getPublicMaterialId());
        queryWrapper.eq(MaterialInventory.COL_MATERIAL_SPECIFICATION_ID,params.getMaterialSpecificationId());
        MaterialInventory materialInventory = baseMapper.selectOne(queryWrapper);
        CustomAssert.isNull(materialInventory,"库存不存在");
        materialInventory.setWarningTotalInventory(params.getWarningTotalInventory());
        baseMapper.updateById(materialInventory);
    }

    @Override
    public IPage<WarehouseMaterialInfoListVO> listBatchByMaterialId(String materialBatch,String publicMaterialId, Long materialSpecificationId, DaoSearch daoSearch) {

        Page<WarehouseMaterialInfoListVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        QueryWrapper<MaterialOutWarehouse> queryWrapper = new QueryWrapper<>();

        final String IN_PREFIX ="a.";
        queryWrapper.eq(!Objects.isNull(materialSpecificationId),IN_PREFIX+MaterialInWarehouse.COL_MATERIAL_SPECIFICATION_ID,materialSpecificationId);
        queryWrapper.eq(StringUtils.isNotBlank(publicMaterialId),IN_PREFIX+MaterialInWarehouse.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
        queryWrapper.eq(StringUtils.isNotBlank(materialBatch),IN_PREFIX+MaterialInWarehouse.COL_MATERIAL_BATCH,materialBatch);
        IPage<WarehouseMaterialInfoListVO> iPage=baseMapper.listBatchByMaterialId(page,queryWrapper,publicMaterialId,commonUtil.getOrganizationId());
        return iPage;
    }

    @Override
    public IPage<PublicMaterialInfoListViewVO> listMaterialOutInfo(String materialName, String materialSortId, Long materialSpecificationId,String publicMaterialId, DaoSearch daoSearch) {

        Page<PublicMaterialInfoListViewVO> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        QueryWrapper<MaterialInWarehouse> queryWrapper = new QueryWrapper<>();
        final String IN_PREFIX ="a.";
        final String M_PREFIX ="b.";
        queryWrapper.eq(StringUtils.isNotBlank(materialSortId),M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_ID,materialSortId);
        queryWrapper.eq(StringUtils.isNotBlank(publicMaterialId),IN_PREFIX+MaterialInWarehouse.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
        queryWrapper.eq(!Objects.isNull(materialSpecificationId),IN_PREFIX+MaterialInWarehouse.COL_MATERIAL_SPECIFICATION_ID,materialSpecificationId);
        queryWrapper.eq(IN_PREFIX+MaterialInWarehouse.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(IN_PREFIX+MaterialInWarehouse.COL_SYS_ID,commonUtil.getSysId());
        IPage<PublicMaterialInfoListViewVO> iPage=baseMapper.listMaterialOutInfo(page,queryWrapper);
        return iPage;
    }


    @Override
    public IPage<PublicMaterialInfoListViewVO> enableMaterialsByField(MaterialFieldListDTO params) {

        Page<PublicMaterialInfoListViewVO> page = new Page<>(params.getDefaultCurrent(),params.getDefaultPageSize());
        QueryWrapper<MaterialInWarehouse> queryWrapper = new QueryWrapper<>();
        final String SPEC_PREFIX ="b.";
        final String M_PREFIX ="a.";
        queryWrapper.eq(!Objects.isNull(params.getDisableFlag()),M_PREFIX+MaterialInfo.COL_DISABLE_FLAG,params.getDisableFlag());

        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(M_PREFIX+MaterialInfo.COL_SYS_ID,commonUtil.getSysId());
        if (null != params.getGroupBySameBatchId() && params.getGroupBySameBatchId().equals(1)){
            queryWrapper.groupBy(M_PREFIX+MaterialInfo.COL_MATERIAL_SORT_ID,M_PREFIX+MaterialInfo.COL_SAME_BATCH_MATERIAL_ID);
        }

        if (StringUtils.isNotBlank(params.getSearch())){
            String search = params.getSearch();
            queryWrapper.and(w->w.like(M_PREFIX+MaterialInfo.COL_MATERIAL_NAME,search).
                    or().like(MaterialSpecification.COL_SPECIFICATION_INFO,search).
                    or().like(MaterialSpecification.COL_MATERIAL_CODE,search)
            );
        }
        IPage<PublicMaterialInfoListViewVO> iPage=baseMapper.enableMaterialsByField(page,queryWrapper);
        return iPage;
    }


    @Override
    public void checkMaterialIsAdequacy(String publicMaterialId, String materialBatch, BigDecimal outboundNum, Long materialSpecificationId) {
        BigDecimal last;
        if (materialBatch == null || "".equals(materialBatch)) {
            //批次为空  对比该物料总库存剩余量
            MaterialInventory select = baseMapper.selectByPublicMaterialId(publicMaterialId, materialSpecificationId);
            if (null == select) {
                CommonUtil.throwSuperCodeExtException(500, "不存在该物料规格库存");
            }
            last = select.getTotalInventory();
        } else {
            //批次不为空，对比该批次的物料库存剩余量
            DaoSearch daoSearch = new DaoSearch();
            IPage<WarehouseMaterialInfoListVO> warehouseMaterialInfoListVOIPage = listBatchByMaterialId(materialBatch, publicMaterialId, materialSpecificationId, daoSearch);
            List<WarehouseMaterialInfoListVO> records = warehouseMaterialInfoListVOIPage.getRecords();
            if (CollectionUtils.isEmpty(records)){
                CommonUtil.throwSuperCodeExtException(500,"不存在入库记录");
            }
            last = records.get(0).getTotalInventory();
        }
        if (last.subtract(outboundNum).longValue() < 0) {
            CommonUtil.throwSuperCodeExtException(500, "库存不足!");
        }
    }

    @Override
    public String getOutAndInCode() {
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append("CRK");
        stringBuilder.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        long n = redisUtil.generate(RedisKey.getOrgSerialnumberMaterialBound(commonUtil.getOrganizationId(),commonUtil.getSysId()), CommonUtil.getTodayEndTime());
        int count = 6 - String.valueOf(n).length();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                stringBuilder.append("0");
            }
            stringBuilder.append(n);
        } else {
            stringBuilder.append(n);
        }
        return stringBuilder.toString();
    }
}
