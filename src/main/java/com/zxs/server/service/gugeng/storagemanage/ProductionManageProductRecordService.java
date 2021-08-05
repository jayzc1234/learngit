package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.update.impl.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.OutboundBO;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.base.BaseMassDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchProductRecordStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReceived;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageProductRecord;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSaleOutRecord;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageProductRecordMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageProducePlanService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageSeedPlanService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.ProducePlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SeedPlanResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductRecordDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductRecordResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductStockStatisticsResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductRecordConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.ProductRecordErrorMsgConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil.DESC;

/**
 * <p>
 * 生产档案服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-31
 */
@Slf4j
@Service
public class ProductionManageProductRecordService extends ServiceImpl<ProductionManageProductRecordMapper, ProductionManageProductRecord> implements BaseService<ProductionManageProductRecord> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper messageMapper;

    @Autowired
    private ProductionManageSaleOutRecordService outRecordService;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageProducePlanService producePlanService;

    @Autowired
    private ProductionManageSeedPlanService seedPlanService;

    /**
     * 分拣入库时新增产品档案信息，
     *
     * @since 1.1版本在产品档案信息中添加了获取分拣报损的数据（因为产品说分拣报损和分拣入库没有前后顺序）
     */
    @Transactional(rollbackFor = Exception.class)
    public void sortAddRecord(String plantBatchId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        //校验是否已存在此产品档案信息
        List<ProductionManageProductRecord> list = query().eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                .list();
        // 如果产品档案信息存在则更新，不存在则新增
        if (CollectionUtils.isEmpty(list)) {
            try {
                ProductionManageProductRecord productRecord = commonUtil.getBatchMessage(plantBatchId);
                // 设置首次分拣时间
                productRecord.setFirstSortDate(new Date());
                // 设置报损总重量 => 库存报损重量
                BigDecimal harvestDamageWeight = Optional.ofNullable(productRecord.getHarvestDamageWeight()).orElse(BigDecimal.ZERO);
                productRecord.setDamageTotalWeight(harvestDamageWeight);
                // v1.7新加内容，添加区域所在的区域负责人
                BaseMassDTO mass = commonUtil.getMassMsgByMassId(productRecord.getGreenhouseId(), commonUtil.getSuperToken());
                if (Objects.nonNull(mass)) {
                    productRecord.setPrincipalName(mass.getPrincipalName());
                }
                if (baseMapper.insert(productRecord) < 1) {
                    log.warn(ADD_PRODUCT_RECORD_MSG_FAILED);
                } else {
                    log.info(ADD_PRODUCT_RECORD_MSG_SUCCEED);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        } else {
            // do nothing
        }
    }

    /**
     * 更新分拣报损重量
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSortDamageWeight(String plantBatchId, BigDecimal sortDamageWeight) throws SuperCodeException {
        ProductionManageProductRecord record = getProductionManageProductRecord(plantBatchId);
        if (Objects.isNull(record)) {
            log.error(PRODUCT_RECORD_MSG_IS_NULL);
            return;
        }

        BigDecimal damageWeight = record.getSortDamageWeight();
        BigDecimal damageTotalWeight = record.getDamageTotalWeight();
        UpdateChainWrapper<ProductionManageProductRecord> update = update();

        if (Objects.isNull(damageWeight) || damageWeight.doubleValue() == 0) {
            update.set(SORT_DAMAGE_WEIGHT, sortDamageWeight);
        } else {
            update.set(SORT_DAMAGE_WEIGHT, damageWeight.add(sortDamageWeight));
        }

        update.set(DAMAGE_TOTAL_WEIGHT, damageTotalWeight.add(sortDamageWeight))
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId);

        CustomAssert.isSuccess(update.update(), UPDATE_SORT_WEIGHT_FAILED);
    }

    /**
     * 通过批次id获取产品档案信息,校验是否为空或存在多个结果
     *
     * @param plantBatchId
     * @return
     * @throws SuperCodeException
     */
    protected ProductionManageProductRecord getProductionManageProductRecord(String plantBatchId) {
        QueryWrapper<ProductionManageProductRecord> queryWrapper = commonUtil.queryTemplate(ProductionManageProductRecord.class);
        queryWrapper.eq(OLD_PLANT_BATCH_ID, plantBatchId);
        ProductionManageProductRecord productRecord = baseMapper.selectOne(queryWrapper);
        return productRecord;
    }

    /**
     * 更新库存报损重量
     */
    @Transactional
    public void updateStockLossWeight(String plantBatchId, BigDecimal stockLossWeight) throws SuperCodeException {
        ProductionManageProductRecord record = getProductionManageProductRecord(plantBatchId);
        if (Objects.isNull(record)) {
            log.error(PRODUCT_RECORD_MSG_IS_NULL);
            return;
        }
        BigDecimal storageDamageWeight = record.getStorageDamageWeight();
        BigDecimal damageTotalWeight = record.getDamageTotalWeight();
        UpdateChainWrapper<ProductionManageProductRecord> update = update();

        if (Objects.isNull(storageDamageWeight) || storageDamageWeight.doubleValue() == 0) {
            update.set(STORAGE_DAMAGE_WEIGHT, stockLossWeight);
        } else {
            update.set(STORAGE_DAMAGE_WEIGHT, storageDamageWeight.add(stockLossWeight));
        }

        update.set(DAMAGE_TOTAL_WEIGHT, damageTotalWeight.add(stockLossWeight))
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId);

        CustomAssert.isSuccess(update.update(), UPDATE_SORT_WEIGHT_FAILED);
    }

    /**
     * 更新销售总额，首次出库时间
     * *
     *
     * @param outboundId 出库主键id
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateSaleAmountAndOutDate(Long outboundId, Date now) throws SuperCodeException {

        // 添加销售客户信息
        List<ProductionManageSaleOutRecord> list = addSaleOutRecord(outboundId, now);
        if (CollectionUtils.isEmpty(list)) {
            log.error(UPDATE_SALE_AMOUNT_AND_FIRST_OUT_DATE_FAILED);
            return;
        }

        /*
         更新销售总额和首次出库时间
         1. 通过批次id获取产品档案信息
         2. 更新产品档案中的销售总额和首次出库时间
        */
        Map<String, List<ProductionManageSaleOutRecord>> maps = list.stream().collect(Collectors.groupingBy(ProductionManageSaleOutRecord::getPlantBatchId));
        for (Map.Entry<String, List<ProductionManageSaleOutRecord>> entry : maps.entrySet()) {
            ProductionManageProductRecord record = getProductionManageProductRecord(entry.getKey());
            if (Objects.isNull(record)) {
                log.error(PRODUCT_RECORD_MSG_IS_NULL);
                return;
            }
            BigDecimal totalAmount = record.getSaleTotalAmount();
            Date firstOutStorageDate = record.getFirstOutStorageDate();
            UpdateChainWrapper<ProductionManageProductRecord> update = update();
            // 如果首次出库时间为空，则更新，否则不更新
            if (Objects.isNull(firstOutStorageDate)) {
                update.set(FIRST_OUT_STORAGE_DATE, now);
            }

            // 获取批次订单金额志和
            BigDecimal amount = entry.getValue().stream().map(ProductionManageSaleOutRecord::getSaleAmount).reduce(BigDecimal::add).get();
            // 通过批次id 和 订单id来获取销售总额
            if (Objects.isNull(totalAmount) || totalAmount.doubleValue() == 0) {
                update.set(SALE_TOTAL_AMOUNT, amount);
            } else {
                update.set(SALE_TOTAL_AMOUNT, totalAmount.add(amount));
            }
            update.eq(OLD_SYS_ID, commonUtil.getSysId())
                    .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                    .eq(OLD_PLANT_BATCH_ID, entry.getKey());

            CustomAssert.isSuccess(update.update(), UPDATE_PRODUCT_RECORD_SALE_AMOUNT_FAILED);
        }
    }

    /**
     * 添加销售客户信息
     *
     * @param outboundId
     * @param now
     * @return
     */
    public List<ProductionManageSaleOutRecord> addSaleOutRecord(Long outboundId, Date now) throws SuperCodeException {
        List<OutboundBO> outboundBOS = messageMapper.listByOutboundId(outboundId);
        if (CollectionUtils.isEmpty(outboundBOS)) {
            log.error("添加销售客户信息失败");
            return Collections.emptyList();
        }

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        /*
        通过批次id + 订单id + 产品id + 产品等级code作为唯一标识，如果后续需求存在一个订单对应多次
        出库发货，则需要通过此标识来校验是否存在销售客户信息，存在则更新，不存在则新增
        */
        List<ProductionManageSaleOutRecord> list = outboundBOS.stream().map(outboundBO -> {
            outboundBO.initOutboundBO(outboundBO);
            ProductionManageSaleOutRecord outRecord = new ProductionManageSaleOutRecord();
            BeanUtils.copyProperties(outboundBO, outRecord);
            // 1 => 重量 2 => 数量  3 => 箱数 4 => 份数
            if (OrderTypeEnum.WEIGHT.getStatus() == outboundBO.getOrderType()) {
                outRecord.setWeightPrice(outboundBO.getUnitPrice());
                // 设置销售金额
                outRecord.setSaleAmount(outboundBO.getUnitPrice().multiply(outboundBO.getPackingWeight()));
            } else if (OrderTypeEnum.NUM.getStatus() == outboundBO.getOrderType()) {
                outRecord.setNumPrice(outboundBO.getUnitPrice());
                // 设置销售金额
                outRecord.setSaleAmount(outboundBO.getUnitPrice().multiply(BigDecimal.valueOf(outboundBO.getPackingNum())));
            } else if (OrderTypeEnum.PORTION.getStatus() == outboundBO.getOrderType()) {
                // 设置份数单价
                outRecord.setServingsPrice(outboundBO.getUnitPrice());
                // 设置销售金额
                outRecord.setSaleAmount(outboundBO.getUnitPrice().multiply(BigDecimal.valueOf(outboundBO.getPackingServings())));
            } else {
                outRecord.setBoxNumPrice(outboundBO.getUnitPrice());
                outRecord.setSaleAmount(outboundBO.getUnitPrice().multiply(BigDecimal.valueOf(outboundBO.getPackingBoxNum())));
            }
            outRecord.setSaleWeight(outboundBO.getPackingWeight());
            outRecord.setSaleBoxNum(outboundBO.getPackingBoxNum());
            outRecord.setSaleNum(outboundBO.getPackingNum());
            outRecord.setSaleServings(outboundBO.getPackingServings());
            outRecord.setOrderDate(outboundBO.getOrderDate());
            outRecord.setOutStorageDate(now);
            outRecord.setSysId(sysId);
            outRecord.setOrganizationId(organizationId);
            return outRecord;
        }).collect(Collectors.toList());
        // 添加销售客户信息
        CustomAssert.isSuccess(outRecordService.saveBatch(list), ADD_SALE_CLIENT_MSG_FAILED);

        return list;
    }

    /**
     * 获取生产档案列表
     *
     * @param daosearch
     */
    @Override
    public PageResults list(DaoSearch daosearch) throws SuperCodeException {
        SearchProductRecordRequestDTO requestDTO = (SearchProductRecordRequestDTO) daosearch;
        QueryWrapper<ProductionManageProductRecord> queryWrapper = commonUtil.queryTemplate(ProductionManageProductRecord.class);
        Page<ProductionManageProductRecord> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        SearchProductStockStatisticsResponseVO statistics = null;
        String search = requestDTO.getSearch();
        // 如果search为空，则进行高级检索，不为空则进行普通检索
        if (StringUtils.isBlank(search)) {
            getWrapper(requestDTO, queryWrapper);
            // 进行高级检索时，获取统计数据
            statistics = baseMapper.getStatistics(queryWrapper);
        } else {
            queryWrapper.and(wrapper -> wrapper.or().like(OLD_PLANT_BATCH_NAME, search)
                    .or().like(OLD_PRODUCT_NAME, search)
                    .or().like(OLD_GREENHOUSE_NAME, search));
        }

        queryWrapper.orderByDesc(FIRST_SORT_DATE);
        IPage<ProductionManageProductRecord> iPage = baseMapper.selectPage(page, queryWrapper);
        List<ProductionManageProductRecord> records = iPage.getRecords();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        CommonUtil.PMPageResults pageResults = new CommonUtil.PMPageResults();
        pageResults.setPagination(pagination);
        pageResults.setOther(statistics);

        if (CollectionUtils.isEmpty(records)) {
            return pageResults;
        }

        List<SearchProductRecordResponseVO> list = records.stream().map(record -> {
            SearchProductRecordResponseVO responseVO = new SearchProductRecordResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 初始化数值
            responseVO.setProduceTotalWeight(Optional.ofNullable(responseVO.getProduceTotalWeight()).orElse(BigDecimal.ZERO));
            responseVO.setDamageTotalWeight(Optional.ofNullable(responseVO.getDamageTotalWeight()).orElse(BigDecimal.ZERO));
            responseVO.setHarvestDamageWeight(Optional.ofNullable(responseVO.getHarvestDamageWeight()).orElse(BigDecimal.ZERO));
            responseVO.setSortDamageWeight(Optional.ofNullable(responseVO.getSortDamageWeight()).orElse(BigDecimal.ZERO));
            responseVO.setStorageDamageWeight(Optional.ofNullable(responseVO.getStorageDamageWeight()).orElse(BigDecimal.ZERO));
            responseVO.setSaleTotalAmount(Optional.ofNullable(responseVO.getSaleTotalAmount()).orElse(BigDecimal.ZERO));

            return responseVO;
        }).collect(Collectors.toList());

        pageResults.setList(list);

        return pageResults;
    }

    @Override
    public List<ProductionManageProductRecord> listExcelByIds(List<? extends Serializable> ids) {
        List<ProductionManageProductRecord> list = baseMapper.selectBatchIds(ids);
        list.forEach(record -> {
            // 初始化数值
            record.setProduceTotalWeight(Optional.ofNullable(record.getProduceTotalWeight()).orElse(BigDecimal.ZERO));
            record.setDamageTotalWeight(Optional.ofNullable(record.getDamageTotalWeight()).orElse(BigDecimal.ZERO));
            record.setHarvestDamageWeight(Optional.ofNullable(record.getHarvestDamageWeight()).orElse(BigDecimal.ZERO));
            record.setStorageDamageWeight(Optional.ofNullable(record.getStorageDamageWeight()).orElse(BigDecimal.ZERO));
            record.setSortDamageWeight(Optional.ofNullable(record.getSortDamageWeight()).orElse(BigDecimal.ZERO));
            record.setSaleTotalAmount(Optional.ofNullable(record.getSaleTotalAmount()).orElse(BigDecimal.ZERO));
        });

        return list;
    }

    /**
     * 获取高级检索查询条件
     *
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    private QueryWrapper<ProductionManageProductRecord> getWrapper(SearchProductRecordRequestDTO requestDTO, QueryWrapper<ProductionManageProductRecord> queryWrapper) throws SuperCodeException {
        String[] plantStartInterval = LocalDateTimeUtil.substringDate(requestDTO.getPlantStartDate());
        String[] plantEndInterval = LocalDateTimeUtil.substringDate(requestDTO.getPlantEndDate());
        String[] sortInterval = LocalDateTimeUtil.substringDate(requestDTO.getFirstSortDate());
        String[] outStorageInterval = LocalDateTimeUtil.substringDate(requestDTO.getFirstOutStorageDate());
        queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getPlantBatchName()), OLD_PLANT_BATCH_NAME, requestDTO.getPlantBatchName())
                .eq(StringUtils.isNotBlank(requestDTO.getProductName()), OLD_PRODUCT_NAME, requestDTO.getProductName())
                .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseName()), OLD_GREENHOUSE_NAME, requestDTO.getGreenhouseName())
                .ge(StringUtils.isNotBlank(plantStartInterval[0]), PLANT_START_DATE, plantStartInterval[0])
                .le(StringUtils.isNotBlank(plantStartInterval[1]), PLANT_START_DATE, plantStartInterval[1])
                .ge(StringUtils.isNotBlank(plantEndInterval[0]), PLANT_END_DATE, plantEndInterval[0])
                .le(StringUtils.isNotBlank(plantEndInterval[1]), PLANT_END_DATE, plantEndInterval[1])
                .ge(StringUtils.isNotBlank(sortInterval[0]), FIRST_SORT_DATE, sortInterval[0])
                .le(StringUtils.isNotBlank(sortInterval[1]), FIRST_SORT_DATE, sortInterval[1])
                .ge(StringUtils.isNotBlank(outStorageInterval[0]), FIRST_OUT_STORAGE_DATE, outStorageInterval[0])
                .le(StringUtils.isNotBlank(outStorageInterval[1]), FIRST_OUT_STORAGE_DATE, outStorageInterval[1]);

        // 校验排序字段是否为空
        if (StringUtils.isBlank(requestDTO.getOrderField())) {
            return queryWrapper;
        }

        if (StringUtils.equalsIgnoreCase(requestDTO.getOrderType(), DESC)) {
            queryWrapper.orderByDesc(requestDTO.getOrderField());
        } else {
            queryWrapper.orderByAsc(requestDTO.getOrderField());
        }

        return queryWrapper;
    }

    /**
     * 获取产品档案详情信息
     * v1.7 => 产品档案详情中新增了生产计划，育苗计划等
     *
     * @param id 产品档案主键id
     */
    public SearchProductRecordDetailResponseVO getDetail(Long id) {
        ProductionManageProductRecord productRecord = baseMapper.selectById(id);

        SearchProductRecordDetailResponseVO responseVO = new SearchProductRecordDetailResponseVO();
        BeanUtils.copyProperties(productRecord, responseVO);
        // 获取报损总重量 = 采收报损总重量 + 分拣报损重量 + 库存报损总重量
        BigDecimal harvestDamageWeight = Optional.ofNullable(responseVO.getHarvestDamageWeight()).orElse(BigDecimal.ZERO);
        BigDecimal sortDamageWeight = Optional.ofNullable(responseVO.getSortDamageWeight()).orElse(BigDecimal.ZERO);
        BigDecimal stockDamageWeight = Optional.ofNullable(responseVO.getStorageDamageWeight()).orElse(BigDecimal.ZERO);
        responseVO.setDamageTotalWeight(harvestDamageWeight.add(sortDamageWeight).add(stockDamageWeight));
        // 初始化数值
        responseVO.setSaleTotalAmount(Optional.ofNullable(responseVO.getSaleTotalAmount()).orElse(BigDecimal.ZERO));

        /*
         以下内容皆为1.7版本更新内容
         */
        String plantBatchId = productRecord.getPlantBatchId();

        // 调用溯源接口，获取基质准备中的生产计划编号
        String productionNo = commonUtil.getProducePlanNo(plantBatchId);

        // 添加生产计划集合
        List<ProducePlanResponseVO> producePlans = producePlanService.listByProductionNo(productionNo);
        responseVO.setProducePlans(producePlans);

        // 添加育苗计划集合
        List<SeedPlanResponseVO> seedPlans = seedPlanService.listByProductionNo(productionNo);
        responseVO.setSeedPlans(seedPlans);
        return responseVO;
    }

    /**
     * mq推送最新的种植批次相关信息，
     * 对其产品档案中的相关数据进行更新
     *
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateByMq(PlantingBatchResponseDTO dto) {
        log.info("消息入参：{}", dto);
        // 获取当前产品档案的批次信息，存在则更新，不存在则不做如何处理
        List<ProductionManageProductRecord> list = query().eq(OLD_PLANT_BATCH_ID, dto.getTraceBatchInfoId()).list();
        if (CollectionUtils.isNotEmpty(list)) {
            ProductionManageProductRecord productRecord = list.get(0);
            // 报损总重量 = 采收报损总重量 + 分拣报损总重量 + 库存报损总重量
            BigDecimal damagedQuantity = dto.getHarvestDamagedQuantity();
            BigDecimal sortDamageWeight = Optional.ofNullable(productRecord.getSortDamageWeight()).orElse(new BigDecimal(0));
            BigDecimal stockDamageWeight = Optional.ofNullable(productRecord.getStorageDamageWeight()).orElse(new BigDecimal(0));
            BigDecimal damageTotalWeight = damagedQuantity.add(sortDamageWeight).add(stockDamageWeight);

            // 更新
            update().set(PRODUCE_TOTAL_WEIGHT, dto.getHarvestQuantity())
                    .set(HARVEST_DAMAGE_WEIGHT, dto.getHarvestDamagedQuantity())
                    .set(DAMAGE_TOTAL_WEIGHT, damageTotalWeight)
                    .eq(OLD_ID, productRecord.getId())
                    .update();
            log.debug("mq的消息消费成功");
        }
    }

    /**
     * 通过批次id更新实收总金额
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-17
     * @updateDate 2019-09-17
     * @updatedBy shixiongfei
     */
    public void updateTotalAmountReceived(String plantBatchId, BigDecimal totalAmountReceived) {
        boolean result = update().setSql("TotalAmountReceived = TotalAmountReceived + " + totalAmountReceived)
                .eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .update();

        if (!result) {
            CustomAssert.printErrorLog(UPDATE_PRODUCT_RECORD_TOTAL_AMOUNT_FAILED);
        }
    }

    /**
     * 批量更新产品档案中的实收总金额
     * 1. 通过订单来获取订单类型， 1 => 按重量 2 => 按数量
     * 2. 将相同的产品id + 产品等级下的订单产品实收金额汇聚成一条，这么做的目的是为了防止数据重录入
     * 3. 通过产品id + 产品等级code + 订单id + 出库id来获取包装信息列表，对相同的产品等级下的批次信息进行比例划分实收总金额
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-17
     * @updateDate 2019-09-17
     * @updatedBy shixiongfei
     */
    public void updateTotalAmountReceived(ProductionManageOrder order, List<ProductionManageOrderProductReceived> productReceives) {
        Byte orderType = order.getOrderType();

        // 通过订单id获取包装信息列表
        List<ProductionManageOutboundPackageMessage> packageMessages = messageMapper.selectByOrderId(order.getId());
        // 校验包装信息列表是否为空,不为空时才进行更新产品档案
        if (CollectionUtils.isEmpty(packageMessages)) {
            return;
        }

        Map<String, List<ProductionManageOrderProductReceived>> listMap = productReceives.stream()
                .collect(Collectors.groupingBy(received -> received.getProductId() + ":::" + received.getProductLevelCode()));
        listMap.forEach((k, v) -> {
            String[] split = k.split(DELIMITER);
            String productId = split[0];
            String productLevelCode = split[1];
            // 获取实收总金额
            BigDecimal totalReceivedAmount = v.stream().map(received -> new BigDecimal(Optional.ofNullable(received.getReceivedProMoney()).orElse("0")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 通过产品id + 产品等级code来获取包装信息列表
            List<ProductionManageOutboundPackageMessage> plants = packageMessages.stream()
                    .filter(packageMessage -> packageMessage.getProductId().equals(productId) && packageMessage.getProductLevelCode().equals(productLevelCode))
                    .collect(Collectors.toList());
            /*
            校验订单是按重量卖还是按数量卖，如果是按重量卖则进行比例划分
            1. 按重量卖时：统计当前相同批次下的总重量，（这么做的目的是为了减少与数据库交互的负载）然后按重量比例划分实收总金额
            2. 按数量卖时: 统计当前相同批次下的总数量，（这么做的目的是为了减少与数据库交互的负载) 然后按数量比例划分实收总金额
            （订单类型是否修改成枚举类型而并非一个魔法值）
             */
            if (orderType == 1) {
                // 获取当前相同产品等级code的总重量
                BigDecimal totalWeight = plants.stream().map(plant -> Optional.ofNullable(plant.getPackingWeight()).orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // 校验总重量是否大于0，大于0时才对产品档案进行更新
                if (totalWeight.signum() != 1) {
                    return;
                }
                // 获取当前相同批次下的总重量
                Map<String, BigDecimal> plantMaps = plants.stream()
                        .collect(Collectors.groupingBy(ProductionManageOutboundPackageMessage::getPlantBatchId,
                                Collectors.collectingAndThen(Collectors.toList(), list -> list.stream().map(t -> Optional.ofNullable(t.getPackingWeight())
                                        .orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add))));
                // 更新产品档案实收总金额
                plantMaps.forEach((key, value) -> {
                    // 获取该批次比例下的实收总金额
                    BigDecimal result = value.multiply(totalReceivedAmount).divide(totalWeight, 2, BigDecimal.ROUND_HALF_UP);
                    if (result.signum() != 0) {
                        updateTotalAmountReceived(key, result);
                    }
                });
            } else if (orderType == 2) {
                // 获取当前相同产品等级code的总数量
                Integer totalQuantity = plants.stream().mapToInt(plant -> Optional.ofNullable(plant.getPackingNum()).orElse(0)).sum();
                // 校验总数量是否大于0， 大于时才进行更新
                if (totalQuantity <= 0) {
                    return;
                }
                // 获取当前相同批次下的总数量
                Map<String, Integer> plantMaps = plants.stream().collect(Collectors.groupingBy(ProductionManageOutboundPackageMessage::getPlantBatchId,
                        Collectors.summingInt(t -> Optional.ofNullable(t.getPackingNum()).orElse(0))));

                // 更新产品档案实收总金额
                plantMaps.forEach((key, value) -> {
                    // 获取该批次比例下的实收总金额
                    BigDecimal result = totalReceivedAmount.multiply(BigDecimal.valueOf(value / totalQuantity))
                            .setScale(2, BigDecimal.ROUND_HALF_UP);
                    if (result.signum() != 0) {
                        updateTotalAmountReceived(key, result);
                    }
                });
            }
        });
    }

    /**
     * 获取产品档案统计数据
     *
     * @param requestDTO 请求dto
     * @return 返回一个响应vo
     * @author shixiongfei
     * @date 2019-09-20
     * @updateDate 2019-09-20
     * @updatedBy shixiongfei
     */
    public SearchProductStockStatisticsResponseVO statistics(SearchProductRecordStatisticsRequestDTO requestDTO) {
        return baseMapper.statistics(requestDTO, commonUtil.getSysId(), commonUtil.getOrganizationId());
    }

    /**
     * 区域负责人数据同步，这里不应该将其嵌入在业务层，应当单独抽离到
     * 指定的数据同步模块中
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-06
     * @updateDate 2019-11-06
     * @updatedBy shixiongfei
     */
    public void principalNameDataSync() {
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(superToken -> {
            String sysId = superToken.getSysId();
            String organizationId = superToken.getOrganizationId();
            String token = superToken.getToken();

            // 获取产品档案中所有区域id集合
            List<ProductionManageProductRecord> list = query().select(OLD_GREENHOUSE_ID)
                    .eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                    .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                    .groupBy(OLD_GREENHOUSE_ID)
                    .list();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            list.forEach(record -> {
                String greenhouseId = record.getGreenhouseId();
                BaseMassDTO mass = commonUtil.getMassMsgByMassId(greenhouseId, token);
                if (Objects.isNull(mass) || StringUtils.isBlank(mass.getPrincipalName())) {
                    return;
                }

                // 更新区域负责人名称
                update().set(PRINCIPAL_NAME, mass.getPrincipalName())
                        .eq(OLD_GREENHOUSE_ID, greenhouseId)
                        .eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                        .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                        .update();
            });

        });
    }

    /**
     * 获取所有的产品档案信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-16
     * @updateDate 2019-12-16
     * @updatedBy shixiongfei
     */
    public List<ProductionManageProductRecord> listAll(String sysId, String organizationId) {
        QueryWrapper<ProductionManageProductRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId);
        return list(queryWrapper);
    }

    /**
     * 更新产品档案中的总成本和单位成本信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-16
     * @updateDate 2019-12-16
     * @updatedBy shixiongfei
     */
    public void updateCostAmount(Long id, BigDecimal totalCostAmount, BigDecimal unitCostAmount, String sysId, String organizationId) {
        boolean isSuccess = update().set(TOTAL_COST_AMOUNT, totalCostAmount)
                .set(UNIT_COST_AMOUNT, unitCostAmount)
                .eq(OLD_ID, id)
                .eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .update();

        CustomAssert.false2Error(isSuccess, UPDATE_PRODUCT_RECORD_COST_AMOUNT_FAILED);
    }

}