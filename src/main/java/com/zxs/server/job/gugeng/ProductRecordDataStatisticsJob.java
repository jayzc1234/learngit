package com.zxs.server.job.gugeng;

import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.AreaUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.base.BaseMassDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.BatchCostStatisticsDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageProductRecord;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageProductRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * 产品档案数据同步
 *
 * @author shixiongfei
 * @date 2019-12-16
 * @since
 */
@Slf4j
@Component
public class ProductRecordDataStatisticsJob {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageProductRecordService productRecordService;

    /**
     * 产品档案总成本和单位成本数据统计
     *
     * @author shixiongfei
     * @date 2019-12-16
     * @updateDate 2019-12-16
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void statisticsData() {
        log.info("====产品档案总成本和单位成本数据统计执行开始====");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(this::dataSynchronization);
        log.info("====产品档案总成本和单位成本数据统计执行完成====");
    }

    /**
     * 产品档案总成本和单位成本数据同步
     *
     * @author shixiongfei
     * @date 2019-12-16
     * @updateDate 2019-12-16
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void dataSynchronization(ProductionManageSuperToken superToken) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        String token = superToken.getToken();

        // 获取所有的产品档案信息
        List<ProductionManageProductRecord> list = productRecordService.listAll(sysId, organizationId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(productRecord -> {
            String plantBatchId = productRecord.getPlantBatchId();
            // 获取大棚信息
            BaseMassDTO mass = commonUtil.getMassMsgByMassId(productRecord.getGreenhouseId(), token);

            // 获取大棚面积，单位为亩
            BigDecimal massArea = Objects.isNull(mass) ? BigDecimal.ZERO : AreaUtil.parse2MuArea(mass.getMassArea(), mass.getAreaUnit());

            // 添加总成本
            BatchCostStatisticsDTO costStatistics = commonUtil.getBatchCostStatistics(plantBatchId);
            BigDecimal totalCostAmount = Objects.isNull(costStatistics) ? BigDecimal.ZERO : costStatistics.getTotalCost().setScale(2, BigDecimal.ROUND_HALF_UP);
            // 添加单位成本
            BigDecimal unitCostAmount = massArea.signum() == 0
                    ? BigDecimal.ZERO
                    : totalCostAmount.divide(massArea, 2, BigDecimal.ROUND_HALF_UP);

            // 更新产品档案信息
            productRecordService.updateCostAmount(productRecord.getId(), totalCostAmount, unitCostAmount, sysId, organizationId);
        });

    }
}
