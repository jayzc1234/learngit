package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.ReportLossTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.LossConfirmationStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsReportLoss;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsReportLossService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;


/**
 * 报损数据统计job
 *
 * @author shixiongfei
 * @date 2019-10-21
 * @since v1.6
 */
@Slf4j
@Component
public class ReportLossStatisticsJob {

    @Autowired
    private ProductionManageStatisticsReportLossService reportLossService;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageStockLossMapper stockLossMapper;

    /**
     * 报损数据统计
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void statisticsData() {
        log.info("报损数据统计定时任务执行开始");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(this::dataSynchronization);
        log.info("报损数据统计定时任务执行结束");
    }

    /**
     * 库存报损数据同步
     *
     * @author shixiongfei
     * @date 2019-12-11
     * @updateDate 2019-12-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void dataSynchronization(ProductionManageSuperToken superToken) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        // 3. 处理库存报损数据
        List<ProductionManageStatisticsReportLoss> list = handleStockLoss(sysId, organizationId);

        // 移除报损统计数据
        QueryWrapper<ProductionManageStatisticsReportLoss> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SYS_ID, superToken.getSysId())
                .eq(ORGANIZATION_ID, superToken.getOrganizationId());
        reportLossService.remove(queryWrapper);
        // 新增报损统计数据
        reportLossService.saveBatch(list);
    }

    private List<ProductionManageStatisticsReportLoss> handleStockLoss(String sysId, String organizationId) {
        // 统计所有库存报损数据
        List<ProductionManageStockLoss> stockLosses = stockLossMapper.listAllStatisticsMsg(sysId, organizationId,
                LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED.getKey());
        return stockLosses.stream().map(stockLoss -> {
            ProductionManageStatisticsReportLoss reportLoss = new ProductionManageStatisticsReportLoss();
            reportLoss.setSysId(sysId);
            reportLoss.setOrganizationId(organizationId);
            reportLoss.setReportLossType(ReportLossTypeEnum.STOCK_LOSS.getKey());
            reportLoss.setReportLossWeight(stockLoss.getDamageWeight());
            reportLoss.setPlantBatchId(stockLoss.getPlantBatchId());
            reportLoss.setPlantBatchName(stockLoss.getPlantBatchName());
            reportLoss.setReportLossDate(stockLoss.getDamageDate());
            reportLoss.setDepartmentId(stockLoss.getDepartmentId());
            reportLoss.setDepartmentName(stockLoss.getDepartmentName());
            reportLoss.setProductId(stockLoss.getProductId());
            reportLoss.setProductName(stockLoss.getProductName());
            boolean isTrue = SortingInventoryTypeEnum.OUTSIDE_SORTING.getKey() == stockLoss.getType();
            reportLoss.setSortingType(isTrue ? BatchTypesEnum.OUT_PLANT.getKey() : BatchTypesEnum.PLANT.getKey());
            return reportLoss;
        }).collect(Collectors.toList());
    }
}