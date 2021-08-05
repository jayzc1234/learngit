package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.RedisLockUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffInWarehousePrivateDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffInWarehousePublicDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.repairstuff.ProductionManageStuffStockFlowDetailDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.repairmanage.StuffInBoundTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffInWarehouse;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageStuffStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffInWarehouseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageStuffMapper;
import net.app315.hydra.intelligent.planting.utils.redis.RedisUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.repairstuff.ProductionManageStuffDetailVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 材料入库表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManageStuffInWarehouseService extends ServiceImpl<ProductionManageStuffInWarehouseMapper, ProductionManageStuffInWarehouse> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private RedisLockUtil lock;

    @Autowired
    private ProductionManageStuffStockService stuffStockService;

    @Autowired
    private ProductionManageStuffMapper stuffMapper;

    @Autowired
    private ProductionManageStuffSpecificationService stuffSpecificationService;

    @Transactional(rollbackFor =Exception.class)
    public void add(ProductionManageStuffInWarehousePublicDTO stuffInWarehousePublicDTO) throws SuperCodeException, ParseException {
        List<ProductionManageStuffInWarehousePrivateDTO> inWarehousePrivateDTOList = stuffInWarehousePublicDTO.getInWarehousePrivateDTOList();
        if (CollectionUtils.isEmpty(inWarehousePrivateDTOList)){
            CommonUtil.throwSuperCodeExtException(500,"入库材料不能为空");
        }
        String inboundCode=stuffInWarehousePublicDTO.getInboundCode();
        String inboundDate=stuffInWarehousePublicDTO.getInboundDate();
        Date inboundDate_d=CommonUtil.formatStringToDate(inboundDate, DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);
        String supperId=stuffInWarehousePublicDTO.getSupperId();
        String supperName=stuffInWarehousePublicDTO.getSupperName();
        String wareHouseId=stuffInWarehousePublicDTO.getWareHouseId();
        String wareHouseName=stuffInWarehousePublicDTO.getWareHouseName();
        Integer inboundType = stuffInWarehousePublicDTO.getInboundType();
        Employee employee=commonUtil.getEmployee();
        String employeeId=employee.getEmployeeId();
        String employeeName=employee.getName();

        Date currentDate=CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);
        List<ProductionManageStuffInWarehouse> stuffInWarehouseList=new ArrayList<>();

        String orgId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        String key = RedisKey.STUFF_OUT_IN_REDIS_LOCK_KEY+orgId+":"+sysId ;
        BigDecimal zeroBigDecimal = new BigDecimal(0);
        try {
            boolean getLock = lock.lock(key, 1000L, 6, 200L);
            if (getLock) {
                for (ProductionManageStuffInWarehousePrivateDTO productionManageStuffInWarehousePrivateDTO : inWarehousePrivateDTOList) {
                    String publicStuffId = productionManageStuffInWarehousePrivateDTO.getPublicStuffId();
                    if (StringUtils.isBlank(publicStuffId)){
                        CommonUtil.throwSuperCodeExtException(500,"材料publicStuffId不能为空");
                    }
                    ProductionManageStuffDetailVO productionManageStuff=stuffMapper.selectByPublicStuffId(publicStuffId);
                    if (null==productionManageStuff){
                        CommonUtil.throwSuperCodeExtException(500,"材料"+publicStuffId+"不存在");
                    }
                    productionManageStuffInWarehousePrivateDTO.setStuffNo(productionManageStuff.getStuffNo());
                    String uuid= UUID.randomUUID().toString();
                    ProductionManageStuffInWarehouse entity = new ProductionManageStuffInWarehouse();
                    BeanUtils.copyProperties(productionManageStuffInWarehousePrivateDTO,entity);
                    entity.setInboundRemainingNum(productionManageStuffInWarehousePrivateDTO.getInboundNum());
                    entity.setWareHouseId(wareHouseId);
                    entity.setWareHouseName(wareHouseName);
                    entity.setCreateEmployeeId(employeeId);
                    entity.setCreateEmployeeName(employeeName);
                    entity.setCreateDate(currentDate);
                    entity.setSupperId(supperId);
                    entity.setSupperName(supperName);
                    entity.setInboundDate(inboundDate_d);
                    entity.setInboundId(uuid);
                    entity.setInboundType(inboundType);
                    entity.setInboundTypeName(StuffInBoundTypeEnum.getDesc(inboundType));
                    entity.setInboundDate(inboundDate_d);
                    //生成材料批次
                    entity.setStuffBatch(setStuffBatch(productionManageStuffInWarehousePrivateDTO.getStuffBatch()));
                    entity.setInboundCode(inboundCode);
                    entity.setPublicStuffId(productionManageStuffInWarehousePrivateDTO.getPublicStuffId());
                    stuffInWarehouseList.add(entity);
                    Long stuffSpecificationId = productionManageStuffInWarehousePrivateDTO.getStuffSpecificationId();
                    if (null!=stuffSpecificationId){
                        stuffSpecificationService.updateUseNum(stuffSpecificationId);
                    }
                }
                saveBatch(stuffInWarehouseList);
                List<ProductionManageStuffStock> stuffStockList=genStockList(inWarehousePrivateDTOList,currentDate,employee);
                stuffStockService.saveOrUpdateBatch(stuffStockList);
            } else {
                throw new SuperCodeException("系统繁忙请稍后再试试", 500);
            }
        }finally {
            lock.releaseLock(key);
        }
    }

    private List<ProductionManageStuffStock> genStockList(List<ProductionManageStuffInWarehousePrivateDTO> stuffInWarehouseList,Date currentDate,Employee employee) {
        String employeeId=employee.getEmployeeId();
        String employeeName=employee.getName();
        Map<String,ProductionManageStuffStock> map=new HashMap<>();
        BigDecimal zeroBigDecimal = new BigDecimal(0);
        List<String> stringList=new ArrayList<>();
        for (ProductionManageStuffInWarehousePrivateDTO productionManageStuffInWarehouse : stuffInWarehouseList) {
            String stuffBatch = productionManageStuffInWarehouse.getStuffBatch();
            boolean contains = stringList.contains(stuffBatch);
            if (contains){
                CommonUtil.throwSuperCodeExtException(500,"批次"+stuffBatch+"重复");
            }else {
                stringList.add(stuffBatch);
            }

            String publicStuffId=productionManageStuffInWarehouse.getPublicStuffId();
            Long stuffSpecificationId=productionManageStuffInWarehouse.getStuffSpecificationId();
            if (null==stuffSpecificationId){
                CommonUtil.throwSuperCodeExtException(500,"入库材料编号"+productionManageStuffInWarehouse.getStuffNo()+"规格主键不能为空");
            }
            BigDecimal inboundNum = productionManageStuffInWarehouse.getInboundNum();
            Long stuffSortId = productionManageStuffInWarehouse.getStuffSortId();
            String stuffSortName = productionManageStuffInWarehouse.getStuffSortName();
            String specification = productionManageStuffInWarehouse.getSpecification();
            String stuffName = productionManageStuffInWarehouse.getStuffName();
            String stuffNo = productionManageStuffInWarehouse.getStuffNo();


            ProductionManageStuffStock stuffStock=map.get(publicStuffId+stuffSpecificationId);
            if (null==stuffStock){
                //如果该库存在map中不存在且已保存过该库存则使用已存在库存进行更新
                ProductionManageStuffStock existStuffStock=stuffStockService.selectByPublicStuffIdAndSpecificationId(publicStuffId,stuffSpecificationId);
                if (null!=existStuffStock){
                    stuffStock=existStuffStock;
                    stuffStock.setTotalInventory(CommonUtil.bigDecimalAdd(inboundNum,stuffStock.getTotalInventory()));

                    map.put(publicStuffId+stuffSpecificationId,stuffStock);
                }else {
                    stuffStock=new ProductionManageStuffStock();
                    BeanUtils.copyProperties(productionManageStuffInWarehouse,stuffStock);
                    stuffStock.setStuffSortName(stuffSortName);
                    stuffStock.setSpecification(specification);
                    stuffStock.setStuffSpecificationId(stuffSpecificationId);
                    stuffStock.setWarningTotalInventory(zeroBigDecimal);
                    stuffStock.setStuffSortId(stuffSortId);
                    stuffStock.setStuffName(stuffName);
                    stuffStock.setStuffNo(stuffNo);
                    stuffStock.setCreateDate(currentDate);
                    stuffStock.setAuthDepartmentId(employee.getDepartmentId());
                    stuffStock.setCreateEmployeeId(employeeId);
                    stuffStock.setCreateEmployeeName(employeeName);
                    stuffStock.setTotalInventory(inboundNum);
                    map.put(publicStuffId+stuffSpecificationId,stuffStock);
                }
            }else {
                //进入该分支说明已经进行过existStuffStock判断，不可以再使用existStuffStock进行相加否则会重复增加existStuffStock的库存
                stuffStock.setTotalInventory(CommonUtil.bigDecimalAdd(inboundNum,stuffStock.getTotalInventory()));
            }
        }
        return new ArrayList<>(map.values());
    }

    public BigDecimal selectStuffBatchRemainingNum(String publicStuffId, Long stuffSpecificationId,String stuffBatch) {
        QueryWrapper<ProductionManageStuffInWarehouse> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID, publicStuffId);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatch);
        return baseMapper.selectStuffBatchRemainingNum(queryWrapper);
    }

    public ProductionManageStuffInWarehouse selectByStuffBatch(String publicStuffId, Long stuffSpecificationId, String stuffBatch) {
        QueryWrapper<ProductionManageStuffInWarehouse> queryWrapper = commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID, publicStuffId);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID, stuffSpecificationId);
        queryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatch);
        return baseMapper.selectOne(queryWrapper);
    }


    /**
     * @return
     * @Author corbett
     * @Description //TODO 设置物料批次
     * @Date 23:00 2019/3/31
     * @Param
     **/
    private String setStuffBatch(String stuffBatch) throws SuperCodeException {
        String stuffBatchNew=stuffBatch;
        String orgId=commonUtil.getOrganizationId();
        String sysId=commonUtil.getSysId();
        QueryWrapper<ProductionManageStuffInWarehouse> stuffInWarehouseQueryWrapper=commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
        if (StringUtils.isBlank(stuffBatch)) {
            //需要自动生成物料批次
            stuffBatchNew = getStuffBatch(orgId, sysId);
            stuffInWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatchNew);
            int i = 100;
            while (i-- > 0) {
                if (!baseMapper.isStuffBatchInOrgHas(stuffInWarehouseQueryWrapper)) {
                    break;
                }
                stuffBatchNew = getStuffBatch(orgId, sysId);
                stuffInWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatchNew);
            }
            return stuffBatchNew;
        } else {
            //不需要自动生成物料批次
            stuffInWarehouseQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_BATCH, stuffBatchNew);
            if (baseMapper.isStuffBatchInOrgHas(stuffInWarehouseQueryWrapper)) {
                throw new SuperCodeException("您填的物料批次已经存在,请核实后再试");
            }
        }
        return stuffBatchNew;
    }

    /**
     * @return
     * @Author corbett
     * @Description //TODO 获取物料批次号
     * @Date 19:37 2019/3/29
     * @Param
     **/
    private String getStuffBatch(String orgId,String sysId) {
        StringBuilder stringBuilder = new StringBuilder(1024);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(DateTimePatternConstant.yyyyMMdd);
        stringBuilder.append(simpleDateFormat.format(new Date()));
        long n = redisUtil.generate(RedisKey.getOrgSerialnumberStuffBatch(orgId,sysId), CommonUtil.getSecondsNextEarlyMorning());
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

    public AbstractPageService.PageResults<List<ProductionManageStuffInWarehouse>> dropDownBatch(ProductionManageStuffStockFlowDetailDTO stuffStockFlowDetailDTO) {
        Page<ProductionManageStuffInWarehouse> page = CommonUtil.genPage(stuffStockFlowDetailDTO);
        QueryWrapper<ProductionManageStuffInWarehouse> stuffStockQueryWrapper = commonUtil.queryTemplate(ProductionManageStuffInWarehouse.class);
        stuffStockQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_PUBLIC_STUFF_ID, stuffStockFlowDetailDTO.getPublicStuffId());
        stuffStockQueryWrapper.eq(ProductionManageStuffInWarehouse.COL_STUFF_SPECIFICATION_ID, stuffStockFlowDetailDTO.getStuffSpecificationId());
        IPage<ProductionManageStuffInWarehouse> ipage = baseMapper.selectPage(page, stuffStockQueryWrapper);
        return CommonUtil.iPageToPageResults(ipage, null);
    }
}
