package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.BasicMassBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsGreenhouse;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageWeight;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageWeightMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsGreenhouseService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.ORGANIZATION_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.SYS_ID;


/**
 * 区域采收重量统计job
 *
 * @author shixiongfei
 * @date 2019-10-20
 * @since
 */
@Slf4j
@Component
public class GreenhouseHarvestWeightStatisticsJob {

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageStatisticsGreenhouseService greenhouseService;

    @Autowired
    private ProductionManageWeightMapper weightMapper;

    /**
     * 统计每天的区域采收重量
     *
     * @author shixiongfei
     * @date 2019-10-20
     * @updateDate 2019-10-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void statisticsData() {
        log.info("区域采收重量定时任务执行开始");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
        superTokenList.forEach(this::dataSynchronization);
        log.info("区域采收重量定时任务执行结束");
    }

    /**
     * 进行数据同步
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void dataSynchronization(ProductionManageSuperToken superToken) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        // 调用基础数据接口获取所有大棚分区名称
        List<BasicMassBO> bos = commonUtil.listAllMassMsg(superToken.getToken());
        if (CollectionUtils.isEmpty(bos)) {
            log.info("不存在如何的分区区域信息");
            return;
        }
        // 获取所有采收称重的信息
        List<ProductionManageWeight> weighingWeights = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils.elementIsNull(weightMapper.listAllWeightByDate(sysId, organizationId));

        if (CollectionUtils.isNotEmpty(weighingWeights)) {
            List<ProductionManageStatisticsGreenhouse> list = weighingWeights.stream().map(harvestWeight -> {
                ProductionManageStatisticsGreenhouse statisticsGreenhouse = new ProductionManageStatisticsGreenhouse();
                BasicMassBO bo = bos.stream().filter(t -> t.getMassId().equals(harvestWeight.getGreenhouseId())).findFirst().orElse(null);
                if (Objects.nonNull(bo)) {
                    statisticsGreenhouse.setPartitionId(bo.getAssociationTypeId());
                    statisticsGreenhouse.setPartitionName(bo.getPartitionName());
                }
                statisticsGreenhouse.setGreenhouseId(harvestWeight.getGreenhouseId());
                statisticsGreenhouse.setGreenhouseName(harvestWeight.getGreenhouseName());
                statisticsGreenhouse.setHarvestWeight(harvestWeight.getWeight());
                statisticsGreenhouse.setHarvestDate(harvestWeight.getCreateDate());
                statisticsGreenhouse.setSysId(sysId);
                statisticsGreenhouse.setOrganizationId(organizationId);
                return statisticsGreenhouse;
            }).collect(Collectors.toList());

            QueryWrapper<ProductionManageStatisticsGreenhouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(SYS_ID, superToken.getSysId())
                    .eq(ORGANIZATION_ID, superToken.getOrganizationId());
            greenhouseService.remove(queryWrapper);
            greenhouseService.saveBatch(list);
        }
    }
}