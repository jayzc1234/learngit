package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.pojo.common.JsonResult;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchInWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoInWarehouseModel;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInWarehouse;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInventory;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSpecification;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInWarehouseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInfoMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInventoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialSpecificationMapper;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialInWarehouseService;
import net.app315.hydra.intelligent.planting.server.util.BaseMapperUtil;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.nail.common.result.RichResult;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 物料入库表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-24
 */
@Service
public class MaterialInWarehouseServiceImpl extends ServiceImpl<MaterialInWarehouseMapper, MaterialInWarehouse> implements MaterialInWarehouseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MaterialSpecificationMapper specificationMapper;

    @Autowired
    private MaterialInfoMapper materialInfoMapper;

    @Autowired
    private MaterialInventoryMapper inventoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MaterialInfoInWarehouseDTO infoInWarehouseDTO) {

        MaterialInWarehouse inWarehouse = new MaterialInWarehouse();
        BeanUtils.copyProperties(infoInWarehouseDTO,inWarehouse);

        String inId = commonUtil.getUUID();
        inWarehouse.setInboundId(inId);
        //获取操作人
        String userId = commonUtil.getUserId();
        String userName = commonUtil.getUserName();
        inWarehouse.setUserId(userId);
        inWarehouse.setUserName(userName);


        //物料批次不填写自动生成  判断是否需要自动生成
        //生成规则：年月日+三位数流水号，如20190214001
        setMaterialBatch(inWarehouse, commonUtil.getOrganizationId(),commonUtil.getSysId());

        //入库编码，自动生成
//        insert.setInboundCode(getBoundCode(orgId));

        //获取物料信息，填入规格参数
        String publicMaterialId = infoInWarehouseDTO.getPublicMaterialId();
        MaterialInfo selectMaterial = materialInfoMapper.selectByPublicMaterialId(publicMaterialId);
        if (selectMaterial == null) {
            throw new SuperCodeExtException("入库失败,该物料不存在");
        }
        MaterialSpecification specification = specificationMapper.selectById(infoInWarehouseDTO.getMaterialSpecificationId());
        if (specification == null) {
            throw new SuperCodeExtException( "物料规格不存在");
        }
        inWarehouse.setSpecification(new BigDecimal(specification.getSpecification()));
        inWarehouse.setSpecificationInfo(specification.getSpecificationInfo());
        inWarehouse.setSpecificationUnit(specification.getSpecificationUnit());
        inWarehouse.setSpecificationUnitName(specification.getSpecificationUnitName());
        inWarehouse.setSpecificationType(specification.getSpecificationType());
        inWarehouse.setSpecificationTypeName(specification.getSpecificationTypeName());

        //设置入库剩余库存数
        inWarehouse.setInboundRemainderNum(inWarehouse.getInboundNum());

        int resu = baseMapper.insert(inWarehouse);
        if (resu <= 0) {
            throw new SuperCodeExtException("入库失败");
        }

        //库存表插入或者更新
        BigDecimal inboundNum = inWarehouse.getInboundNum();
        QueryWrapper<MaterialInventory> queryWrapper = commonUtil.queryTemplate(MaterialInventory.class);
        queryWrapper.eq(MaterialInventory.COL_PUBLIC_MATERIAL_ID, publicMaterialId);
        queryWrapper.eq(MaterialInventory.COL_MATERIAL_SPECIFICATION_ID, inWarehouse.getMaterialSpecificationId());

        MaterialInventory baMaterialInventory = inventoryMapper.selectOne(queryWrapper);
        BigDecimal bigDecimalspecial=new BigDecimal(specification.getSpecification());
        if (null == baMaterialInventory) {
            baMaterialInventory = new MaterialInventory();
            baMaterialInventory.setTotalInventory(inboundNum);
            baMaterialInventory.setPublicMaterialId(publicMaterialId);
            baMaterialInventory.setTotalStock(inboundNum.multiply(bigDecimalspecial));
            baMaterialInventory.setMaterialSpecificationId(inWarehouse.getMaterialSpecificationId());
            inventoryMapper.insert(baMaterialInventory);
        } else {
            baMaterialInventory.setTotalInventory(baMaterialInventory.getTotalInventory().add(inboundNum));
            baMaterialInventory.setTotalStock(baMaterialInventory.getTotalStock().add(inboundNum.multiply(bigDecimalspecial)));
            inventoryMapper.updateById(baMaterialInventory);
        }
        //TODO 更新物料规格使用状态
    }

    @Override
    public RichResult batchIn(MaterialInfoBatchInWarehouseDTO params) {
        JsonResult jsonResult;
        List<MaterialInfoInWarehouseModel> paramsList = params.getMaterialSingleInWarehouses();
        if (paramsList != null && paramsList.size() > 0) {
            for (MaterialInfoInWarehouseModel materialSingleInWarehouseModel : paramsList) {
                add(params.getMaterialInWarehouseModel(materialSingleInWarehouseModel));
            }
        }
        return new RichResult(200, "入库成功", null);
    }


    /**
     * @return
     * @Author corbett
     * @Description //TODO 设置物料批次
     * @Date 23:00 2019/3/31
     * @Param
     **/
    private void setMaterialBatch(MaterialInWarehouse inWarehouse,String organizationId,String sysId)  {
        if (StringUtils.isBlank(inWarehouse.getMaterialBatch())) {
            //需要自动生成物料批次
            String materialBatch = getMaterialBatch(organizationId,sysId);
            int i = 100;
            while (i-- > 0) {
                boolean b =BaseMapperUtil.singleTableUniqueFieldCheck(null, MaterialInWarehouse.COL_MATERIAL_BATCH, materialBatch, organizationId, sysId, null, baseMapper);
                if (!b) {
                    break;
                }
                materialBatch = getMaterialBatch(organizationId,sysId);
            }
            inWarehouse.setMaterialBatch(materialBatch);
        } else {
            //不需要自动生成物料批次
            boolean b = BaseMapperUtil.singleTableUniqueFieldCheck(inWarehouse.getId(), MaterialInWarehouse.COL_MATERIAL_BATCH, inWarehouse.getMaterialBatch(), organizationId, sysId, null, baseMapper);
            if (!b) {
                throw new SuperCodeExtException("您填的物料批次已经存在,请核实后再试");
            }
        }
    }

    /**
     * @return
     * @Author corbett
     * @Description //TODO 获取物料批次号
     * @Date 19:37 2019/3/29
     * @Param
     **/
    private String getMaterialBatch(String orgId,String sysId) {
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        long n = redisUtil.generate(RedisKey.getOrgSerialnumberMaterialBatch(orgId,sysId), getTodayEndTime());
        int count = 3 - String.valueOf(n).length();
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

    /**
     * @return
     * @Title: getTodayEndTime
     * @Description: Get the cache expire time.
     */
    private static Date getTodayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }
}
