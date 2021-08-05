package com.zxs.server.job.gugeng;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.AreaUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.GreenhouseAreaDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.HarvestBatchDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductionYieldData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductionYieldDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageSortInstorageService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageWeightService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 生产产量数据统计定时任务
 * 所有的定时任务中统计的数据在业务代码修改时采用线程池实现伪异步，
 * 后续可采用队列进行业务数据与统计数据解耦合
 * TODO 所有定时任务后续需替换成xxl分布式任务调度框架
 * 相关字段说明：
 * 亩产量 => 种植批次的产品称重的总重量 / 批次关联区域区域面积
 * 商品产品亩产量 => 分拣入库的总重量 / 区域面积
 * 总产量 => 种植批次的产品称重的总重量
 * 商品产品总产量 => 分拣入库的总重量
 * 总产量环比 => (当前批次的总产量 - 上次批次的总产量) / 上次批次的总产量   ps: 这个批次都属于同一个区域
 * 商品产品产量环比 => （当前批次的商品产品总产量 - 上次批次的商品产品总产量）/ 上次批次的商品产品总产量 ps: 这个批次都属于同一个区域
 * 如果不存在上次的批次，也就是当前区域的该批次为第一次采收，则环比记为null,前端显示为中划线
 * <p>
 * 实现逻辑：
 * 每条记录的唯一标识是以批次id作为标识，批次id对应获取到定植时间 + 首次采收时间 + 区域信息，
 * 每天从溯源中检索是否存在新的批次信息录入，存在则存入统计表中
 *
 * @author shixiongfei
 * @date 2019-10-15
 * @since v1.5
 */

@Slf4j
@Component
public class ProductionYieldDataStatisticsJob {


    @Autowired
    private ProductionManageStatisticsProductionYieldDataService productionYieldDataService;

    @Autowired
    private ProductionManageSortInstorageService storageService;

    @Autowired
    private ProductionManageWeightService weightService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    /**
     * 统计当天数据,每天0点开开始执行，
     * 判断当天是否有新的批次录入，存在则新增
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-15
     * @updateDate 2019-10-15
     * @updatedBy shixiongfei
     */
    public void statisticsData() {
        log.info("生产产量数据统计定时任务执行开始");
        try {
            // 首先获取所有的token集合
            List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();
            // 遍历执行，为了应对不同的系统
            superTokenList.forEach(superToken -> {

                String sysId = superToken.getSysId();
                String organizationId = superToken.getOrganizationId();
                String token = superToken.getToken();

                // 获取所有区域区域信息
                List<GreenhouseAreaDTO> areas = commonUtil.listGreenhouseAreaMsg(token);

                List<LocalDate> startAndEndDate = LocalDateTimeUtil.getStartAndEndDate(0);
                LocalDate startDate = startAndEndDate.get(0);
                LocalDate endDate = startAndEndDate.get(1);

                // 这里是为了尽量减少集合自动扩容次数，减少性能开销，默认为1000
                List<ProductionManageStatisticsProductionYieldData> list = new ArrayList<>(1000);

                handleData(startDate.toString(), endDate.plusDays(1).toString(), superToken, areas, list);

                // 移除所有数据
                QueryWrapper<ProductionManageStatisticsProductionYieldData> wrapper = new QueryWrapper<>();
                wrapper.eq("sys_id", sysId)
                        .eq("organization_id", organizationId);
                productionYieldDataService.remove(wrapper);
                // 新增数据
                productionYieldDataService.saveBatch(list);
            });
        } catch (Exception e) {
            log.error("生产产量数据统计定时任务执行失败", e);
        }

        log.info("生产产量数据统计定时任务执行结束");
    }

    /**
     * 处理生产产量数据统计
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     */
    public void handleData(String startDate, String endDate, ProductionManageSuperToken superToken, List<GreenhouseAreaDTO> areas,
                           List<ProductionManageStatisticsProductionYieldData> list) {
        String sysId = superToken.getSysId();
        String organizationId = superToken.getOrganizationId();
        String token = superToken.getToken();
        // 调用溯源接口, 获取定植相关信息
        List<HarvestBatchDTO> harvests = commonUtil.listHarvestBatch(startDate, endDate, token);
        // 过滤掉批次名称为空的数据
        harvests = harvests.stream()
                .filter(harvest -> StringUtils.isNotBlank(harvest.getTraceBatchName()))
                .collect(Collectors.toList());

        harvests.forEach(harvest -> {
            ProductionManageStatisticsProductionYieldData data = new ProductionManageStatisticsProductionYieldData();
            BeanUtils.copyProperties(harvest, data);
            data.setGreenhouseId(harvest.getMassId());
            data.setGreenhouseName(harvest.getMassIfName());
            data.setPlantBatchId(harvest.getTraceBatchInfoId());
            data.setPlantBatchName(harvest.getTraceBatchName());
            data.setPrevPlantBatchId(harvest.getPrevTraceBatchInfoId());
            data.setSysId(sysId);
            data.setOrganizationId(organizationId);

            // 1. 设置亩面积大小
            GreenhouseAreaDTO areaDTO = areas.stream().filter(t -> t.getMassId().equals(harvest.getMassId())).findFirst().orElse(null);
            data.setMassArea(Objects.isNull(areaDTO) ? BigDecimal.ZERO : AreaUtil.parse2MuArea(areaDTO.getMassArea(), areaDTO.getAreaUnit()));
            // 2. 获取商品产品总产量, 获取当前批次下的总入库重量
            BigDecimal totalInboundWeight = storageService.getInboundWeight(data.getPlantBatchId(), sysId, organizationId);
            // 2.1 设置商品产品总产量
            data.setTotalCommodityProductAreaYield(totalInboundWeight);
            // 3. 获取总产量
            BigDecimal totalWeighingWeight = weightService.getWeighingWeight(data.getPlantBatchId(), sysId, organizationId);
            // 3.1 设置总产量
            data.setTotalAreaYield(totalWeighingWeight);

            // v1.9新加内容，获取当前批次下的所有分拣入库的箱数
            Integer packingBoxNum = storageService.getInboundBoxNum(data.getPlantBatchId(), sysId, organizationId);
            // 设置装箱数
            data.setPackingBoxNum(packingBoxNum);

            // v1.9新加内容，设置当前批次的商品率 商品率=分拣入库总量/产品称重总量
            BigDecimal commodityRate = totalWeighingWeight.signum() == 0
                    ? BigDecimal.valueOf(100)
                    : totalInboundWeight.multiply(BigDecimal.valueOf(100)).divide(totalWeighingWeight, 2, BigDecimal.ROUND_HALF_UP);
            data.setCommodityRate(commodityRate);

            BigDecimal inboundWeight;
            BigDecimal weighingWeight;
            // 3.2 如果亩面积大小为0,则商品产量亩产量为0，亩产量为0
            if (data.getMassArea().signum() == 0) {
                inboundWeight = BigDecimal.ZERO;
                weighingWeight = BigDecimal.ZERO;
            } else {
                inboundWeight = totalInboundWeight.divide(data.getMassArea(), 2, BigDecimal.ROUND_HALF_UP);
                weighingWeight = totalWeighingWeight.divide(data.getMassArea(), 2, BigDecimal.ROUND_HALF_UP);
            }
            // 4. 设置商品产品亩产量
            data.setCommodityProductAreaYield(inboundWeight);
            // 5. 设置亩产量
            data.setAreaYield(weighingWeight);

            // 6. 定义总产量环比
            BigDecimal updateAreaYieldRatio = BigDecimal.valueOf(100);
            // 7. 定义商品产品产量环比， 如果当前批次的上个批次的数据为0或为空，则环比默认为100%
            BigDecimal updateTotalCommodityProductAreaYieldRatio = BigDecimal.valueOf(100);
            // 8. v1.9 定义商品率环比
            BigDecimal updateCommodityRateRatio = BigDecimal.valueOf(100);
            // 9 v1.9 定义装箱数环比
            BigDecimal updatePackingBoxNumRatio = BigDecimal.valueOf(100);
            if (StringUtils.isNotBlank(data.getPrevPlantBatchId())) {
                // 7.1 获取上一个批次的商品产品总产量, 设置商品产品产量环比
                BigDecimal prevInboundWeight = storageService.getInboundWeight(data.getPrevPlantBatchId(), sysId, organizationId);
                // 7.2 获取上一个批次的总产量
                BigDecimal prevWeighingWeight = weightService.getWeighingWeight(data.getPrevPlantBatchId(), sysId, organizationId);
                // v1.9 获取上一个批次的装箱数
                Integer prevPackingBoxNum = storageService.getInboundBoxNum(data.getPrevPlantBatchId(), sysId, organizationId);

                if (prevInboundWeight.signum() != 0) {
                    updateTotalCommodityProductAreaYieldRatio = totalInboundWeight.subtract(prevInboundWeight)
                            .multiply(BigDecimal.valueOf(100)).divide(prevInboundWeight, 2, BigDecimal.ROUND_HALF_UP);
                }

                // 设置总产量环比
                if (prevWeighingWeight.signum() != 0) {
                    updateAreaYieldRatio = totalWeighingWeight.subtract(prevWeighingWeight)
                            .multiply(BigDecimal.valueOf(100)).divide(prevWeighingWeight, 2, BigDecimal.ROUND_HALF_UP);
                }

                // v1.9 设置装箱数环比
                updatePackingBoxNumRatio = prevPackingBoxNum == 0
                        ? BigDecimal.valueOf(100)
                        : BigDecimal.valueOf(packingBoxNum).subtract(BigDecimal.valueOf(prevPackingBoxNum))
                          .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(prevPackingBoxNum),2, BigDecimal.ROUND_HALF_UP);

                // v1.9 设置商品率环比
                BigDecimal prevCommodityRate = prevWeighingWeight.signum() == 0
                        ? BigDecimal.valueOf(100)
                        : prevInboundWeight.multiply(BigDecimal.valueOf(100)).divide(prevWeighingWeight, 2, BigDecimal.ROUND_HALF_UP);
                updateCommodityRateRatio = prevCommodityRate.signum() == 0
                        ? BigDecimal.valueOf(100)
                        : commodityRate.subtract(prevCommodityRate).multiply(BigDecimal.valueOf(100)).divide(prevCommodityRate, 2, BigDecimal.ROUND_HALF_UP);
            }

            data.setCommodityProductAreaYieldRatio(updateTotalCommodityProductAreaYieldRatio);
            data.setAreaYieldRatio(updateAreaYieldRatio);
            data.setPackingBoxNumRatio(updatePackingBoxNumRatio);
            data.setCommodityRateRatio(updateCommodityRateRatio);
            list.add(data);
        });
    }

}