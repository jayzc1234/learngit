package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.HarvestBatchDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsNonProductProductionData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductProductionData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsNonProductProductionDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductProductionDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageSortInstorageService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageWeightService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 产品产量数据统计
 *
 * @author shixiongfei
 * @date 2019-10-29
 * @since v1.7
 */
@Component
@Slf4j
public class ProductProductionStatisticsJob {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageStatisticsProductProductionDataService service;

    @Autowired
    private ProductionManageStatisticsNonProductProductionDataService nonService;

    @Autowired
    private ProductionManageSortInstorageService storageService;

    @Autowired
    private ProductionManageWeightService weightService;

    /**
     * 产品产量数据统计
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     */
    public void statisticsData() {
        log.info("产品产量数据统计定时任务执行开始");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(superToken -> {
            // 获取开始结束时间
            List<LocalDate> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(0);
            LocalDate startDate = startAndEndDate.get(0);
            LocalDate endDate = startAndEndDate.get(1);

            // 根据开始结束时间获取所有的采收批次和采收时间
            List<HarvestBatchDTO> harvestBatchDTOS = commonUtil.listHarvestBatch(startDate.toString(), endDate.plusDays(1).toString(), superToken.getToken());
            if (CollectionUtils.isEmpty(harvestBatchDTOS)) {
                return;
            }

            // v1.9 过滤掉已被移除的批次信息
            harvestBatchDTOS = harvestBatchDTOS.stream()
                    .filter(t -> StringUtils.isNotBlank(t.getTraceBatchName()))
                    .collect(Collectors.toList());

            dataSynchronization(superToken, harvestBatchDTOS);

        });
        log.info("产品产量数据统计定时任务执行结束");
    }

    /**
     * 产品产量数据同步
     * // 如果存在
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     */
    public void dataSynchronization(ProductionManageSuperToken superToken, List<HarvestBatchDTO> harvestBatchDTOS) {

        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();

        // 此集合用于产品 + 产品等级
        List<ProductionManageStatisticsProductProductionData> list = new ArrayList<>(1000);

        // 此集合用于产品
        List<ProductionManageStatisticsNonProductProductionData> nonList = new ArrayList<>(1000);

        harvestBatchDTOS.forEach(harvest -> {
            String plantBatchId = harvest.getTraceBatchInfoId();
            // 获取分拣入库列表
            List<ProductionManageStatisticsProductProductionData> inbounds = storageService.listByBatchId(plantBatchId, sysId, organizationId);
            // 获取分拣入库总重量
            BigDecimal inboundWeight = inbounds.stream()
                    .map(t -> Optional.ofNullable(t.getInboundWeight()).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // v1.9 获取分拣入库的总箱数
            Integer packingBoxNum = inbounds.stream().mapToInt(t -> Optional.ofNullable(t.getPackingBoxNum()).orElse(0)).sum();

            // 获取产品称重重量
            BigDecimal yield = weightService.getByBatchId(plantBatchId, sysId, organizationId);
            // 1. 添加有产品等级的相关数据
            if (CollectionUtils.isNotEmpty(inbounds)) {
                inbounds.forEach(inbound -> {
                    inbound.setProductId(harvest.getProductId());
                    inbound.setProductName(harvest.getProductName());
                    inbound.setYield(yield);
                    inbound.setHarvestDate(harvest.getHarvestDate());
                    inbound.setSysId(sysId);
                    inbound.setOrganizationId(organizationId);
                });
                list.addAll(inbounds);
            }

            // 2. 添加无产品等级的相关数据
            ProductionManageStatisticsNonProductProductionData data = new ProductionManageStatisticsNonProductProductionData();
            data.setProductId(harvest.getProductId());
            data.setProductName(harvest.getProductName());
            data.setYield(yield);
            data.setHarvestDate(harvest.getHarvestDate());
            data.setInboundWeight(inboundWeight);
            data.setSysId(sysId);
            data.setOrganizationId(organizationId);
            // v1.9 新加装箱数
            data.setPackingBoxNum(packingBoxNum);

            nonList.add(data);
        });

        // 移除有产品等级的产品产量统计数据
        QueryWrapper<ProductionManageStatisticsProductProductionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sys_id", superToken.getSysId())
                .eq("organization_id", superToken.getOrganizationId());
        service.remove(queryWrapper);

        // 移除无产品等级的产品产量统计数据
        QueryWrapper<ProductionManageStatisticsNonProductProductionData> wrapper = new QueryWrapper<>();
        wrapper.eq("sys_id", superToken.getSysId())
                .eq("organization_id", superToken.getOrganizationId());
        nonService.remove(wrapper);

        // 新增有产品等级统计数据
        service.saveBatch(list);

        // 新增无产品等级统计数据
        nonService.saveBatch(nonList);
    }
}