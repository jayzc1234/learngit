package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryWayEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsShippingData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsShippingDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundDeliveryWayService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 发货数据统计job
 *
 * @author shixiongfei
 * @date 2019-10-23
 * @since v1.6
 */
@Component
@Slf4j
public class ShippingDataStatisticsJob {

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageStatisticsShippingDataService shippingDataService;

    @Autowired
    private ProductionManageOutboundDeliveryWayService deliveryWayService;

    @Autowired
    private ProductManageOrderService orderService;

    // @Scheduled(cron = "0 0/1 * * * ?")
    public void statisticsData() {
        log.info("========================发货数据统计定时任务开始=========================");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(superToken -> {
            List<LocalDate> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(0);
            LocalDate startDate = startAndEndDate.get(0);
            LocalDate endDate = startAndEndDate.get(1);
            // 这里是为了尽量减少集合自动扩容次数，减少性能开销，默认为1000
            List<ProductionManageStatisticsShippingData> list = new ArrayList<>(1000);

            while (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                // 统计数据
                LocalDate tempDate = startDate.plusDays(1);
                handleData(startDate.toString(), tempDate.toString(), superToken, list);
                startDate = startDate.plusDays(1);
            }

            QueryWrapper<ProductionManageStatisticsShippingData> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(StringUtils.isNotBlank(superToken.getSysId()), "sys_id", superToken.getSysId())
                    .eq(StringUtils.isNotBlank(superToken.getOrganizationId()), "organization_id", superToken.getOrganizationId());
            shippingDataService.remove(queryWrapper);
            shippingDataService.saveBatch(list);
        });
        log.info("========================发货数据统计定时任务执行结束==========================");
    }

    /**
     * 获取订单相关的统计数
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void handleData(String startDate, String endDate, ProductionManageSuperToken superToken, List<ProductionManageStatisticsShippingData> list) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        // 获取指定时间的快递订单数
        Integer expressNumber = deliveryWayService.getTotalNumber(startDate, endDate, DeliveryWayEnum.EXPRESS, sysId, organizationId);

        // 获取指定时间的配送订单数
        Integer distributionNumber = deliveryWayService.getTotalNumber(startDate, endDate, DeliveryWayEnum.DISTRIBUTION, sysId, organizationId);
        // 获取指定时间的自取订单数
        Integer selfNumber = deliveryWayService.getTotalNumber(startDate, endDate, DeliveryWayEnum.SELF, sysId, organizationId);
        // 获取指定时间的待发货订单数
        Integer waitDeliveryNumber = orderService.countWaitDelivery(startDate, endDate, sysId, organizationId);
        // 如果数据都为0 则返回,不参与数据统计
        if (expressNumber + distributionNumber + selfNumber + waitDeliveryNumber == 0) {
            return;
        }
        ProductionManageStatisticsShippingData shippingData = new ProductionManageStatisticsShippingData();
        shippingData.setExpressDeliveryNumber(expressNumber);
        shippingData.setDeliveryNumber(distributionNumber);
        shippingData.setSelfAcquiredNumber(selfNumber);
        shippingData.setWaitShipNumber(waitDeliveryNumber);
        shippingData.setSysId(sysId);
        shippingData.setOrganizationId(organizationId);
        shippingData.setShippingDate(DateUtils.parse(startDate, LocalDateTimeUtil.DATE_PATTERN));
        list.add(shippingData);
    }
}