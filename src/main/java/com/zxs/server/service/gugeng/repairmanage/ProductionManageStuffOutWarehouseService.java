package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.RedisLockUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffOutWarehousePrivateDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffOutWarehousePublicDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.repairmanage.StuffOutBoundTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.*;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffOutWarehouseMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 * 材料出库表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageStuffOutWarehouseService extends ServiceImpl<ProductionManageStuffOutWarehouseMapper, ProductionManageStuffOutWarehouse> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManageStuffStockService stuffStockService;

    @Autowired
    private ProductionManageStuffInWarehouseService stuffInWarehouseService;

    @Autowired
    private ProductionManageStuffSpecificationService stuffSpecificationService;

    @Autowired
    private ProductionManageStuffService stuffService;

    @Autowired
    private RedisLockUtil lock;

    @Transactional(rollbackFor = Exception.class)
    public void add(ProductionManageStuffOutWarehousePublicDTO outWarehousePublicDTO) throws SuperCodeException, ParseException {

        List<ProductionManageStuffOutWarehousePrivateDTO> outWarehousePrivateDTOList = outWarehousePublicDTO.getOutWarehousePrivateDTOList();
        if (CollectionUtils.isEmpty(outWarehousePrivateDTOList)){
            CommonUtil.throwSuperCodeExtException(500,"出库材料不能为空");
        }
        String outboundCode = outWarehousePublicDTO.getOutboundCode();
        String outboundDate = outWarehousePublicDTO.getOutboundDate();
        Date outboundDate_d = CommonUtil.formatStringToDate(outboundDate, DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);
        Integer outboundType = outWarehousePublicDTO.getOutboundType();
        Employee employee=commonUtil.getEmployee();
        String employeeId=employee.getEmployeeId();
        String employeeName=employee.getName();
        Date currentDate=CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);

        QueryWrapper<ProductionManageStuffStock> stuffStockQueryWrapper=commonUtil.queryTemplate(ProductionManageStuffStock.class);
        final BigDecimal zeroBigDecimal=new BigDecimal(0);

        List<ProductionManageStuffOutWarehouse> outWarehouseList=new ArrayList<>();
        String orgId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        String key = RedisKey.STUFF_OUT_IN_REDIS_LOCK_KEY+orgId+":"+sysId ;
        try {
            boolean getLock = lock.lock(key, 1000L, 6, 200L);
            if (getLock) {
                for (ProductionManageStuffOutWarehousePrivateDTO productionManageStuffOutWarehousePrivateDTO : outWarehousePrivateDTOList) {
                    QueryWrapper<ProductionManageStuffInWarehouse> inWarehouseQueryWrapper=commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
                    BigDecimal outboundNum = productionManageStuffOutWarehousePrivateDTO.getOutboundNum();
                    String publicStuffId = productionManageStuffOutWarehousePrivateDTO.getPublicStuffId();
                    Long stuffSpecificationId = productionManageStuffOutWarehousePrivateDTO.getStuffSpecificationId();
                    if (StringUtils.isEmpty(publicStuffId)){
                        ProductionManageStuffSpecification stuffSpecification = stuffSpecificationService.getById(stuffSpecificationId);
                        publicStuffId = stuffSpecification.getPublicStuffId();
                    }
                    ProductionManageStuffStock stuffStock = stuffStockService.selectByPublicStuffIdAndSpecificationId(publicStuffId, stuffSpecificationId);
                    ProductionManageStuff stuff = stuffService.list(Wrappers.<ProductionManageStuff>lambdaQuery().eq(ProductionManageStuff::getPublicStuffId,publicStuffId)).get(0);
                    String stuffName = stuff.getStuffName();
                    if (null == stuffStock) {
                        CommonUtil.throwSuperCodeExtException(500, "不存在材料 " + stuffName + "的库存记录");
                    }

                    inWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID, publicStuffId);
                    inWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
                    inWarehouseQueryWrapper.gt(ProductionManageStuffInWarehouse.COL_INBOUND_REMAINING_NUM, 0);
                    String stuffBatch = productionManageStuffOutWarehousePrivateDTO.getStuffBatch();
                    if (StringUtils.isBlank(stuffBatch)) {
                        checkStockEnough(publicStuffId, stuffSpecificationId, outboundNum,stuffName);
                        List<ProductionManageStuffInWarehouse> inWarehouseList = stuffInWarehouseService.list(inWarehouseQueryWrapper);
                        if (CollectionUtils.isEmpty(inWarehouseList)) {
                            CommonUtil.throwSuperCodeExtException(500, "不存在物料" + stuffName + "入库记录");
                        }
                        BigDecimal leftOutboundNum = outboundNum;
                        List<ProductionManageStuffInWarehouse> usedInWarehouseList = new ArrayList<>();
                        for (ProductionManageStuffInWarehouse productionManageStuffInWarehouse : inWarehouseList) {
                            BigDecimal inboundRemainingNum = productionManageStuffInWarehouse.getInboundRemainingNum();
                            if (leftOutboundNum.compareTo(inboundRemainingNum) >= 0) {
                                leftOutboundNum = CommonUtil.bigDecimalSub(leftOutboundNum, inboundRemainingNum);
                                productionManageStuffInWarehouse.setInboundRemainingNum(zeroBigDecimal);
                                usedInWarehouseList.add(productionManageStuffInWarehouse);
                                //保存出库记录
                                addOutBoundRecord(outboundCode, outboundDate_d, outboundType, employeeId, employeeName, currentDate, outWarehouseList, productionManageStuffOutWarehousePrivateDTO, productionManageStuffInWarehouse.getStuffBatch(),stuffStock);
                            } else {
                                productionManageStuffInWarehouse.setInboundRemainingNum(productionManageStuffInWarehouse.getInboundRemainingNum().subtract(leftOutboundNum));
                                leftOutboundNum = zeroBigDecimal;
                                usedInWarehouseList.add(productionManageStuffInWarehouse);
                                //保存出库记录
                                addOutBoundRecord(outboundCode, outboundDate_d, outboundType, employeeId, employeeName, currentDate, outWarehouseList, productionManageStuffOutWarehousePrivateDTO, productionManageStuffInWarehouse.getStuffBatch(), stuffStock);
                                break;
                            }
                        }
                        //出完所有批次之后如果剩下的出库数还是大于0则表示库存不足
                        if (leftOutboundNum.compareTo(zeroBigDecimal)>0){
                            CommonUtil.throwSuperCodeExtException(500,"物料"+stuffName+"库存不足");
                        }
                        stuffInWarehouseService.updateBatchById(usedInWarehouseList);
                    }else {
                        //保存出库记录
                        addOutBoundRecord(outboundCode, outboundDate_d, outboundType, employeeId, employeeName, currentDate, outWarehouseList, productionManageStuffOutWarehousePrivateDTO, stuffBatch, stuffStock);

                        checkRemainingNumEnough(publicStuffId,stuffSpecificationId,stuffBatch,outboundNum,stuffName);
                        ProductionManageStuffInWarehouse productionManageStuffInWarehouse = stuffInWarehouseService.selectByStuffBatch(publicStuffId, stuffSpecificationId, stuffBatch);
                        productionManageStuffInWarehouse.setInboundRemainingNum(CommonUtil.bigDecimalSub(productionManageStuffInWarehouse.getInboundRemainingNum(),outboundNum));
                        stuffInWarehouseService.updateById(productionManageStuffInWarehouse);
                    }
                    //更新库存
                    stuffStock.setTotalInventory(CommonUtil.bigDecimalSub(stuffStock.getTotalInventory(),outboundNum));
                    stuffStockService.updateById(stuffStock);
                    //保存出库记录
                }
            }else {
                throw new SuperCodeException("系统繁忙请稍后再试试", 500);
            }
        }finally {
            lock.releaseLock(key);
        }
        saveBatch(outWarehouseList);
    }

    private void addOutBoundRecord(String outboundCode, Date outboundDate_d, Integer outboundType, String employeeId, String employeeName, Date currentDate, List<ProductionManageStuffOutWarehouse> outWarehouseList, ProductionManageStuffOutWarehousePrivateDTO productionManageStuffOutWarehousePrivateDTO, String stuffBatch, ProductionManageStuffStock stuffStock) {
        ProductionManageStuffOutWarehouse entity = new ProductionManageStuffOutWarehouse();
        BeanUtils.copyProperties(productionManageStuffOutWarehousePrivateDTO, entity);
        entity.setStuffName(stuffStock.getStuffName());
        entity.setStuffSortId(String.valueOf(stuffStock.getStuffSortId()));
        entity.setStuffSortName(stuffStock.getStuffSortName());
        String uuid = UUID.randomUUID().toString();
        entity.setOutboundId(uuid);
        entity.setCreateDate(currentDate);
        entity.setCreateEmployeeId(employeeId);
        entity.setCreateEmployeeName(employeeName);
        entity.setOutboundDate(outboundDate_d);
        entity.setOutboundType(outboundType);
        entity.setOutboundCode(outboundCode);
        entity.setOutboundTypeName(StuffOutBoundTypeEnum.getDesc(outboundType));
        outWarehouseList.add(entity);
    }

    public void checkStockEnough(String publicStuffId, Long stuffSpecificationId, BigDecimal outboundNum, String stuffName){
        ProductionManageStuffStock stuffStock = stuffStockService.selectByPublicStuffIdAndSpecificationId(publicStuffId, stuffSpecificationId);
        if (null==stuffStock){
            CommonUtil.throwSuperCodeExtException(500,"不存在库存记录");
        }
        if (null==outboundNum || outboundNum.compareTo(new BigDecimal(0))<0){
            CommonUtil.throwSuperCodeExtException(500,"出库数量："+outboundNum+"不合法");
        }
        BigDecimal bigDecimal = CommonUtil.bigDecimalSub(stuffStock.getTotalInventory(), outboundNum);
        if (bigDecimal.compareTo(new BigDecimal(0))<0){
            CommonUtil.throwSuperCodeExtException(500,stuffName+" 库存不足");
        }
    }

    public void checkRemainingNumEnough(String publicStuffId, Long stuffSpecificationId, String stuffBatch, BigDecimal outboundNum, String stuffName){
        ProductionManageStuffInWarehouse productionManageStuffInWarehouse = stuffInWarehouseService.selectByStuffBatch(publicStuffId, stuffSpecificationId, stuffBatch);
        if (null==productionManageStuffInWarehouse){
            CommonUtil.throwSuperCodeExtException(500,"不存在入库记录");
        }
        if (null==outboundNum || outboundNum.compareTo(new BigDecimal(0))<0){
            CommonUtil.throwSuperCodeExtException(500,"出库数量："+outboundNum+"不合法");
        }
        BigDecimal bigDecimal = CommonUtil.bigDecimalSub(productionManageStuffInWarehouse.getInboundRemainingNum(), outboundNum);
        if (bigDecimal.compareTo(new BigDecimal(0))<0){
            CommonUtil.throwSuperCodeExtException(500,stuffName+" 库存不足");
        }
    }
}
