package com.zxs.server.service.base.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoBatchOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoOutWarehouseDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoOutWarehouseModel;
import net.app315.hydra.intelligent.planting.pojo.base.*;
import net.app315.hydra.intelligent.planting.server.mapper.base.*;
import net.app315.hydra.intelligent.planting.server.service.base.MaterialOutWarehouseService;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.user.sdk.util.RedisLockUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物料出库表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-08-24
 */
@Service
public class MaterialOutWarehouseServiceImpl extends ServiceImpl<MaterialOutWarehouseMapper, MaterialOutWarehouse> implements MaterialOutWarehouseService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private MaterialSpecificationMapper specificationMapper;

    @Autowired
    private MaterialInfoMapper materialInfoMapper;

    @Autowired
    private MaterialInventoryMapper inventoryMapper;

    @Autowired
    private MaterialInWarehouseMapper baseMaterialInWarehouseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void out(MaterialInfoOutWarehouseDTO params) {
        boolean outSuccess=false;
        MaterialSpecification baseProductionManageMaterialSpecification = specificationMapper.selectById(params.getMaterialSpecificationId());
        if (baseProductionManageMaterialSpecification == null) {
            CommonUtil.throwSuperCodeExtException( 500,"物料规格不存在");
        }

        String publicMaterialId = params.getPublicMaterialId();
        MaterialInfo selectMaterial = materialInfoMapper.selectByPublicMaterialId(publicMaterialId);
        if (selectMaterial == null) {
            CommonUtil.throwSuperCodeExtException(500, "物料不存在");
        }
        String materialName = selectMaterial.getMaterialName();

        //获取到锁
        boolean hasLock = redisLockUtil.lock(params.getPublicMaterialId());
        if (!hasLock) {
            CommonUtil.throwSuperCodeExtException(500, materialName + ":请稍候再试，此物料正出库中");
        }

        try {
            if (selectMaterial == null) {
                CommonUtil.throwSuperCodeExtException(500, materialName + ":出库失败,该物料不存在");
            }

            int resu;
            BigDecimal outboundNum = params.getOutboundNum();
            String materialBatch = params.getMaterialBatch();

            //用户登入信息
            String userId = commonUtil.getUserId();
            String userName = commonUtil.getUserName();
            //用户登入组织信息
            String orgId = commonUtil.getOrganizationId();
            String orgName = commonUtil.getOrganizationName();
            //判断是否选择了批次
            //如果没有选择批次则默认先出库有库存的批次号最小的批次，
            // 然后如果最小的批次数量不够则再出库有库存的第二小的批次的物料，以此类推
            //则出库的数量要大于0小于等于该物料当前总库存数量，支持小数
            if (materialBatch == null || "".equals(materialBatch)) {
                //查询还有库存的物料批次
                QueryWrapper<MaterialInWarehouse> queryWrapper = commonUtil.queryTemplate(MaterialInWarehouse.class);
                queryWrapper.gt(MaterialInWarehouse.COL_INBOUND_REMAINDER_NUM,0);
                queryWrapper.eq(MaterialInWarehouse.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
                queryWrapper.eq(MaterialInWarehouse.COL_MATERIAL_SPECIFICATION_ID,params.getMaterialSpecificationId());
                List<MaterialInWarehouse> ins = baseMaterialInWarehouseMapper.selectList(queryWrapper);
                if (ins == null || ins.size() <= 0) {
                    CommonUtil.throwSuperCodeExtException(500, materialName + ":出库失败,该物料总库存小于此次出库存");
                }
                //获取剩余库存总额
                double totalRemainder = ins.stream()
                        .mapToLong(p -> p.getInboundRemainderNum().longValue())
                        .sum();
                if (totalRemainder < outboundNum.doubleValue()) {
                    CommonUtil.throwSuperCodeExtException(500, materialName + ":出库失败,该物料总库存小于此次出库存");
                }
                ins = ins.stream()
                        .sorted(Comparator.comparing(MaterialInWarehouse::getMaterialBatch))
                        .collect(Collectors.toList());

                List<MaterialInWarehouse> uses = new ArrayList<>(1024);
                BigDecimal v = new BigDecimal(0);
                for (MaterialInWarehouse in : ins) {
                    v = v.add(in.getInboundRemainderNum());
                    uses.add(in);
                    if (v.doubleValue() >= outboundNum.doubleValue()) {
                        break;
                    }
                }

                //初始化出库数
                BigDecimal last = outboundNum;
                //开始进行出库........
                for (MaterialInWarehouse use : uses) {
                    //更新剩余库存
                    //如果剩余出库数大于等于入库剩余数，则更新入库剩余数为0
                    if (last.doubleValue() >= use.getInboundRemainderNum().doubleValue()) {
                        updateInboundRemaind(use.getId(), new BigDecimal(0));
                        //保存出库记录
                        saveOutboundInfo(params, userId, userName, orgId, orgName, selectMaterial, use, use.getInboundRemainderNum());
                        outSuccess=true;
                    }
                    //如果剩余出库数小于入库剩余数，则更新入库剩余数为 use.getInboundRemainderNum() - last
                    if (last.doubleValue() < use.getInboundRemainderNum().doubleValue()) {
                        updateInboundRemaind(use.getId(),
                                use.getInboundRemainderNum().subtract(last));
                        //保存出库记录
                        saveOutboundInfo(params, userId, userName, orgId, orgName, selectMaterial, use, last);
                        outSuccess=true;
                        break;
                    }
                    last = last.subtract(use.getInboundRemainderNum());
                }


            }
            //如果选择了批次，则出库批次
            //则出库的数量要大于0小于等于该批次当前库存数量，支持小数
            if (materialBatch != null && !"".equals(materialBatch)) {
                QueryWrapper<MaterialInWarehouse> queryWrapper =commonUtil.queryTemplate(MaterialInWarehouse.class);
                queryWrapper.eq(MaterialInWarehouse.COL_MATERIAL_BATCH,materialBatch);
                queryWrapper.eq(MaterialInWarehouse.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
                MaterialInWarehouse in = baseMaterialInWarehouseMapper.selectOne(queryWrapper);

                if (in == null) {
                    CommonUtil.throwSuperCodeExtException(500, materialName + ":出库失败,该物料批次不存在");
                }

                //获取最终入库剩余库存
                BigDecimal inboundRemainderNum = in.getInboundRemainderNum();

                //比较是否符合出库数量条件
                if (inboundRemainderNum.doubleValue() < outboundNum.doubleValue()) {
                    CommonUtil.throwSuperCodeExtException(500, materialName + ":出库失败,该批次物料剩余总库存小于此次出库库存");
                }

                //更新入库剩余数
                updateInboundRemaind(in.getId(), inboundRemainderNum.subtract(outboundNum));

                //序列化将要插入数据库的实体
                //保存出库流水
                saveOutboundInfo(params, userId, userName, orgId, orgName, selectMaterial, in, params.getOutboundNum());
                outSuccess=true;
            }

            BigDecimal outboundDecimal = params.getOutboundNum();
            QueryWrapper<MaterialInventory> inventoryQueryWrapper =commonUtil.queryTemplate(MaterialInventory.class);
            inventoryQueryWrapper.eq(MaterialInventory.COL_PUBLIC_MATERIAL_ID,publicMaterialId);
            inventoryQueryWrapper.eq(MaterialInventory.COL_MATERIAL_SPECIFICATION_ID,params.getMaterialSpecificationId());
            MaterialInventory baMaterialInventory = inventoryMapper.selectOne(inventoryQueryWrapper);
            if (null == baMaterialInventory) {
                throw new SuperCodeException("出库失败，库存不存在", 5003);
            } else {
                BigDecimal bigDecimalspecial=new BigDecimal(baseProductionManageMaterialSpecification.getSpecification());
                baMaterialInventory.setTotalInventory(baMaterialInventory.getTotalInventory().subtract(outboundNum));
                baMaterialInventory.setTotalStock(baMaterialInventory.getTotalStock().subtract(outboundDecimal.multiply(bigDecimalspecial)));
                inventoryMapper.updateById(baMaterialInventory);
            }
            //更新规格使用状态
            if (null != params.getMaterialSpecificationId()) {
                //TODO 更新规格状态
            }

            if (outSuccess && StringUtils.isNotBlank(params.getBatchId())){
                BigDecimal bigDecimalSpecial=new BigDecimal(baseProductionManageMaterialSpecification.getSpecification());
                BigDecimal bigDecimalquilty=bigDecimalSpecial.multiply(params.getOutboundNum());
                String quilty="";
                if (null!=bigDecimalquilty){
                    quilty=bigDecimalquilty.toString()+baseProductionManageMaterialSpecification.getSpecificationUnitName();
                }
            }
        } catch (SuperCodeException e) {
            e.printStackTrace();
        } finally {
            boolean releaseLock = redisLockUtil.releaseLock(params.getPublicMaterialId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOut(MaterialInfoBatchOutWarehouseDTO params) {
        List<MaterialInfoOutWarehouseModel> paramsList = params.getMaterialSingleOutWarehouses();
        if (paramsList != null && paramsList.size() > 0) {
            for (MaterialInfoOutWarehouseModel materialSingleOutWarehouseModel : paramsList) {
                out(params.getMaterialOutWarehouseModel(materialSingleOutWarehouseModel));
            }
        }
    }

    /**
     * @return
     * @Author corbett
     * @Description //TODO 保存出库流水
     * @Date 12:23 2019/3/31
     * @Param
     **/
    private void saveOutboundInfo(MaterialInfoOutWarehouseDTO params,
                                  String userId, String userName,
                                  String orgId, String orgName,
                                  MaterialInfo selectMaterial,
                                  MaterialInWarehouse in,
                                  BigDecimal outboundNum) throws SuperCodeException {
        MaterialOutWarehouse insert =
                JSONObject.parseObject(JSON.toJSONString(params), MaterialOutWarehouse.class);
        insert.setOutboundNum(outboundNum);
        String outId = commonUtil.getUUID();
        insert.setOutboundId(outId);
        insert.setMaterialBatch(in.getMaterialBatch());
        //获取操作人
        insert.setUserId(userId);
        insert.setUserName(userName);

        //设置组织信息
        insert.setOrganizationId(orgId);
        insert.setOrganizationName(orgName);

        //出库编码，自动生成
        MaterialSpecification baseProductionManageMaterialSpecification = specificationMapper.selectById(params.getMaterialSpecificationId());
        if (baseProductionManageMaterialSpecification == null) {
            throw new SuperCodeException("物料规格不存在", 500);
        }
        //获取物料信息，填入规格参数
        BigDecimal bigDecimalspecial=new BigDecimal(baseProductionManageMaterialSpecification.getSpecification());
        insert.setSpecification(bigDecimalspecial);
        insert.setSpecificationInfo(baseProductionManageMaterialSpecification.getSpecificationInfo());
        insert.setSpecificationUnit(baseProductionManageMaterialSpecification.getSpecificationUnit());
        insert.setSpecificationUnitName(baseProductionManageMaterialSpecification.getSpecificationUnitName());
        insert.setSpecificationType(baseProductionManageMaterialSpecification.getSpecificationType());
        insert.setSpecificationTypeName(baseProductionManageMaterialSpecification.getSpecificationTypeName());
        //设置出库仓库信息
        insert.setWareHouseId(in.getWareHouseId());
        insert.setWareHouseName(in.getWareHouseName());
        insert.setStoreHouseId(in.getStoreHouseId());
        insert.setStoreHouseName(in.getStoreHouseName());

        //保存出库流水
        int resu = baseMapper.insert(insert);
        if (resu <= 0) {
            throw new SuperCodeException("出库失败", 500);
        }
    }

    /**
     * @return
     * @Author corbett
     * @Description //TODO 更新当前入库物料批次剩余库存
     * @Date 23:01 2019/3/31
     * @Param
     **/
    private void updateInboundRemaind(Long id, BigDecimal inboundRemainderNum) {
        int resu;
        MaterialInWarehouse update = new MaterialInWarehouse();
        update.setInboundRemainderNum(inboundRemainderNum);
        update.setId(id);
        resu = baseMaterialInWarehouseMapper.updateById(update);
        if (resu <= 0) {
            throw new SuperCodeExtException("更新入库的剩余库存失败", 500);
        }
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
