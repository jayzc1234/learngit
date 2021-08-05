package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.bo.gugeng.BatchWeightBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionYieldDataRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.YieldComparisonRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.ProductWeightOperateTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductionYieldData;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsProductionYieldDataMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageWeightMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionYieldDataResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchNewestYCDataVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.YieldComparisonHistogramVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.YieldComparisonResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-16
 */
@Service
public class ProductionManageStatisticsProductionYieldDataService extends ServiceImpl<ProductionManageStatisticsProductionYieldDataMapper, ProductionManageStatisticsProductionYieldData> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageWeightMapper weightMapper;

    @Autowired
    private ProductionManageSortInstorageMapper storageMapper;

    /**
     * 通过时间区间获取生产产量数据列表（不包含分页）
     *
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<ProductionYieldDataResponseVO> listByDateInterval(ProductionYieldDataRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        List<ProductionManageStatisticsProductionYieldData> list = baseMapper.listByDateInterval(requestDTO, sysId, organizationId);
        list = Optional.ofNullable(list).orElse(Collections.emptyList());

        return list.stream().map(entity -> {
            ProductionYieldDataResponseVO responseVO = new ProductionYieldDataResponseVO();
            BeanUtils.copyProperties(entity, responseVO);
            responseVO.setHarvestDate(DateFormatUtils.format(entity.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN));
            if (Objects.nonNull(entity.getPlantingDate())) {
                responseVO.setPlantingDate(DateFormatUtils.format(entity.getPlantingDate(), LocalDateTimeUtil.DATE_PATTERN));
            }
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 获取生产产量数据列表（包含分页）
     *
     * @author shixiongfei
     * @date 2019-10-16
     * @updateDate 2019-10-16
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<ProductionYieldDataResponseVO>> list(ProductionYieldDataRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageStatisticsProductionYieldData> iPage = baseMapper.list(requestDTO, sysId, organizationId);
        Page pagination = new Page((int)iPage.getSize(), (int)iPage.getCurrent(), (int)iPage.getTotal());

        List<ProductionManageStatisticsProductionYieldData> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<ProductionYieldDataResponseVO> list = records.stream().map(record -> {
            ProductionYieldDataResponseVO responseVO = new ProductionYieldDataResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setHarvestDate(DateFormatUtils.format(record.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN));
            if (Objects.nonNull(record.getPlantingDate())) {
                responseVO.setPlantingDate(DateFormatUtils.format(record.getPlantingDate(), LocalDateTimeUtil.DATE_PATTERN));
            }
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 通过批次id获取生产产量数据信息，只可用于定时任务
     *
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public ProductionManageStatisticsProductionYieldData getByPlantBatchId(String plantBatchId, String sysId, String organizationId) {
        ProductionManageStatisticsProductionYieldData data = query().eq("sys_id", sysId)
                .eq(ProductionManageStatisticsProductionYieldData.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageStatisticsProductionYieldData.COL_PLANT_BATCH_ID, plantBatchId)
                .one();
        return data;
    }


    /**
     * 生产产量数据统计导出
     *
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(ProductionYieldDataRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<ProductionYieldDataResponseVO> responseVOS = requestDTO.parseStr2List(ProductionYieldDataResponseVO.class);
        // responseVOS为空则全部导出，否则指定导出
        if (CollectionUtils.isEmpty(responseVOS)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            responseVOS = list(requestDTO).getList();
        }
        ExcelUtils.listToExcel(responseVOS, requestDTO.exportMetadataToMap(), "生产产量数据列表", response);
    }

    /**
     * 更新商品产品亩产量
     *
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Deprecated
    public void updateAreaYield(String plantBatchId, BigDecimal weight, boolean isAreaYield) {
        // 判断是否存在此批次统计信息
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManageStatisticsProductionYieldData yieldData = getByPlantBatchId(plantBatchId, sysId, organizationId);
        if (Objects.nonNull(yieldData)) {
            weight = weight.divide(yieldData.getMassArea(), 2, BigDecimal.ROUND_HALF_UP);

            BigDecimal result = isAreaYield ? yieldData.getAreaYield().add(weight) :
                    yieldData.getCommodityProductAreaYield().add(weight);
            update().eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsProductionYieldData.COL_SYS_ID, sysId)
                    .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsProductionYieldData.COL_ORGANIZATION_ID, organizationId)
                    .eq(isAreaYield ? ProductionManageStatisticsProductionYieldData.COL_AREA_YIELD : ProductionManageStatisticsProductionYieldData.COL_COMMODITY_PRODUCT_AREA_YIELD, result)
                    .update();
        }
    }

    /**
     * 获取产量对比数据统计柱状图数据
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public YieldComparisonHistogramVO listYieldComparisonDataHistogram(YieldComparisonRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageStatisticsProductionYieldData> list = baseMapper.listYieldComparisonDataHistogram(requestDTO, sysId, organizationId);
        YieldComparisonHistogramVO responseVO = new YieldComparisonHistogramVO();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> harvestDates = new ArrayList<>(list.size());
            List<BigDecimal> totalAreaYields = new ArrayList<>(list.size());
            List<BigDecimal> commodityRates = new ArrayList<>(list.size());
            List<BigDecimal> totalCommodityProductAreaYields = new ArrayList<>(list.size());
            list.forEach(vo -> {
                harvestDates.add(DateFormatUtils.format(vo.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN));
                totalAreaYields.add(vo.getTotalAreaYield());
                totalCommodityProductAreaYields.add(vo.getTotalCommodityProductAreaYield());
                commodityRates.add(vo.getCommodityRate());
            });

            responseVO.setHarvestDates(harvestDates);
            responseVO.setTotalAreaYields(totalAreaYields);
            responseVO.setTotalCommodityProductAreaYields(totalCommodityProductAreaYields);
            responseVO.setCommodityRates(commodityRates);
        }

        return responseVO;
    }

    /**
     * 获取产量对比数据统计列表（含分页）
     *
     * @author shixiongfei
     * @date 2019-10-21
     * @updateDate 2019-10-21
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<YieldComparisonResponseVO>> listYieldComparisonData(YieldComparisonRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageStatisticsProductionYieldData> iPage = baseMapper.listYieldComparisonData(requestDTO, sysId, organizationId);

        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageStatisticsProductionYieldData> records = Optional
                .ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<YieldComparisonResponseVO> list = records.stream().map(record -> {
            YieldComparisonResponseVO responseVO = new YieldComparisonResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setHarvestDate(DateFormatUtils.format(record.getHarvestDate(), LocalDateTimeUtil.DATE_PATTERN));
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 产量对比数据导出
     *
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void comparisonExport(YieldComparisonRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<YieldComparisonResponseVO> list = requestDTO.parseStr2List(YieldComparisonResponseVO.class);
        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listYieldComparisonData(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "产量对比数据列表", response);
    }


    /**
     * 获取最新15条种植批次的生产总重量(取自产品称重)和商品率
     *
     * @author shixiongfei
     * @date 2019-12-23
     * @updateDate 2019-12-23
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<SearchNewestYCDataVO> listLastestYieldComparisonData() {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<SearchNewestYCDataVO> vos = weightMapper.listNewestBatchMsg(sysId, organizationId, ProductWeightOperateTypeEnum.HARVEST_WEIGHT.getKey());
        // 获取最新的15条产品称重的批次和重量信息
        if (CollectionUtils.isEmpty(vos)) {
            return vos;
        }
        List<String> batchIds = vos.stream().map(SearchNewestYCDataVO::getPlantBatchId).collect(Collectors.toList());
        // 通过批次id集合来获取分拣入库的总重量
        List<BatchWeightBO> bos = Optional.ofNullable(storageMapper.listByBatchIds(batchIds, sysId, organizationId))
                .orElse(Collections.emptyList());
        vos.forEach(vo -> {
            BatchWeightBO bo = bos.stream()
                    .filter(batchBo -> vo.getPlantBatchId().equals(batchBo.getPlantBatchId()))
                    .findFirst().orElse(null);
            BigDecimal totalInboundWeight = Objects.isNull(bo) ? BigDecimal.ZERO : bo.getTotalInboundWeight();
            vo.setCommodityRate(totalInboundWeight.divide(vo.getTotalProduceWeight(), 2, BigDecimal.ROUND_HALF_UP));
        });

        return vos;
    }
}