package com.zxs.server.service.gugeng.synchronous_data;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.TraceBatchBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.LossConfirmationStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengPlantStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.GuGengProductLossMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.GuGengPlantStockService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.ORGANIZATION_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.SYS_ID;


/**
 * 数据同步实现类
 *
 * @author shixiongfei
 * @date 2019-09-26
 * @since
 */
@Slf4j
@Service
public class SynchronousDataService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private GuGengPlantStockService plantStockService;

    @Autowired
    private ProductionManageHarvestPlanMapper harvestPlanMapper;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper packageMessageMapper;

    @Autowired
    private GuGengProductLossMapper productLossMapper;

    @Autowired
    private ProductionManageStockLossMapper stockLossMapper;


    /**
     * 数据统计
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void plantStockStatisticsData() {
        // 获取古耕的superToken
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(this::dataSynchronization);
    }

    /**
     * 数据同步
     *
     * 种植存量 = 产量预测（采收计划中的）-出库的重量
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void dataSynchronization(ProductionManageSuperToken superToken) {

        // 设置时间段
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        String token = superToken.getToken();

        LocalDate startDate = LocalDate.parse("2018-11-01");
        LocalDate endDate =  LocalDate.now().plusDays(1);
        List<TraceBatchBO> bos = commonUtil.getTraceInfoByCreateDate(startDate.toString(), endDate.toString(), token);
        if (CollectionUtils.isEmpty(bos)) {
            return;
        }

        // 调用溯源接口获取批次列表
        List<GuGengPlantStock> plantStockList = bos.stream().map(bo -> {
            GuGengPlantStock plantStock = new GuGengPlantStock();
            plantStock.setPlantBatchId(bo.getTraceBatchInfoId());
            plantStock.setPlantBatchName(bo.getTraceBatchName());
            plantStock.setGreenhouseId(bo.getMassId());
            plantStock.setGreenhouseName(bo.getMassIfName());
            plantStock.setBatchCreateDate(bo.getCreateTime());
            plantStock.setProductId(bo.getProductId());
            plantStock.setProductName(bo.getProductName());
            BeanUtils.copyProperties(bo, plantStock);

            String plantBatchId = bo.getTraceBatchInfoId();

            // 获取采收计划的产量预测重量
            BigDecimal predictedWeight = harvestPlanMapper.getWeightByBatchId(plantBatchId, sysId, organizationId);

            // 获取出库的重量
            BigDecimal outboundWeight = packageMessageMapper.getWeightByBatchId(plantBatchId, sysId, organizationId);

            // 获取库存报损的重量
            BigDecimal stockLossWeight = stockLossMapper.getWeightByBatchId(plantBatchId, sysId, organizationId, LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED.getKey());

            // 获取产品报损的重量
            BigDecimal productLossWeight = productLossMapper.getWeightByBatchId(plantBatchId, sysId, organizationId,
                    LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED.getKey(), DeleteOrNotEnum.NOT_DELETED.getKey());

            // 获取种植存量 => 种植存量=产量预测（采收计划中的）- 包装出库值 - 库存报损值 - 产品报损值
            BigDecimal stockWeight = predictedWeight.subtract(outboundWeight)
                    .subtract(stockLossWeight)
                    .subtract(productLossWeight)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            plantStock.setStockWeight(stockWeight);
            plantStock.setSysId(sysId);
            plantStock.setOrganizationId(organizationId);

            return plantStock;
        }).collect(Collectors.toList());

        // 移除种植存量数据
        QueryWrapper<GuGengPlantStock> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ORGANIZATION_ID, organizationId);
        plantStockService.remove(queryWrapper);

        // 添加种植存量数据
        plantStockService.saveBatch(plantStockList);
    }
}