package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndBatchBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndGreenhouseBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.HarvestStatisticsResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PlantingBatchResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchDateRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.ProductionDataStatisticsEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageProductDataStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageProductDataStatisticsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.GreenhouseStatisticsResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductDataResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * <p>
 * 生产数据统计表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-08-20
 */
@Slf4j
@Service
public class ProductionManageProductDataStatisticsService extends ServiceImpl<ProductionManageProductDataStatisticsMapper, ProductionManageProductDataStatistics> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 新增数据统计
     *
     * @param weight         重量
     * @param operateDate    操作时间
     * @param statisticsEnum 统计类型枚举
     * @throws SuperCodeException
     */
    @Transactional
    public void add(BigDecimal weight, Date operateDate, ProductionDataStatisticsEnum statisticsEnum) throws SuperCodeException {
        ProductionManageProductDataStatistics statistics = new ProductionManageProductDataStatistics();
        statistics.setOperateDate(operateDate);
        // 设置统计类型
        statistics.setStatisticsType(statisticsEnum.getKey());
        statistics.setWeight(weight);
        try {
            baseMapper.insert(statistics);
        } catch (Exception e) {
            /*
            不要阻塞实际的流程即可，如果后续需拓展，可采用异步方式来处理，
            如果项目体量大，优化时推荐使用消息队列的方式来处理
            */
            log.error("新增生产数据统计失败");
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 更新数据统计
     *
     * @param weight         重量
     * @param operateDate    操作时间
     * @param statisticsEnum 统计类型枚举
     * @throws SuperCodeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(BigDecimal weight, Date operateDate, ProductionDataStatisticsEnum statisticsEnum) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 校验生产数据是否已存在，存在则更新，不存在则新增（为了防止更新时数据错误导致数据未插入）
        List<ProductionManageProductDataStatistics> list = query().eq(ProductionManageProductDataStatistics.COL_STATISTICS_TYPE, statisticsEnum.getKey())
                .eq(ProductionManageProductDataStatistics.COL_OPERATE_DATE, operateDate)
                .eq(ProductionManageProductDataStatistics.COL_SYS_ID, sysId)
                .eq(ProductionManageProductDataStatistics.COL_ORGANIZATION_ID, organizationId)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            add(weight, operateDate, statisticsEnum);
        } else {
            if (list.size() > 1) {
                log.error("同一时间下的同一类型的生产数据统计存在多个");
                return;
            }
            try {
                update().set(ProductionManageProductDataStatistics.COL_WEIGHT, weight)
                        .eq(ProductionManageProductDataStatistics.COL_STATISTICS_TYPE, statisticsEnum.getKey())
                        .eq(ProductionManageProductDataStatistics.COL_OPERATE_DATE, operateDate)
                        .eq(ProductionManageProductDataStatistics.COL_SYS_ID, sysId)
                        .eq(ProductionManageProductDataStatistics.COL_ORGANIZATION_ID, organizationId)
                        .update();
            } catch (Exception e) {
                /*
                 不要阻塞实际的流程即可，如果后续需拓展，可采用异步方式来处理，
                 如果项目体量大，优化时推荐使用消息队列的方式来处理
                 */
                log.error("更新生产数据统计失败");
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 获取生产数据统计
     *
     * @param requestDTO
     * @return
     * @throws SuperCodeException
     */
    public ProductDataResponseVO list(DateIntervalDTO requestDTO) throws SuperCodeException {
        // 校验开始时间是否小于结束时间
        boolean isTrue = LocalDate.parse(requestDTO.getStartQueryDate()).isAfter(LocalDate.parse(requestDTO.getEndQueryDate()));
        if (isTrue) {
            CustomAssert.throwException("开始时间不可大于结束时间");
        }

        // 获取初始化生产数据
        ProductDataResponseVO responseVO = getProductData();

        // 获取时间区间
        List<String> dateInterval = LocalDateTimeUtil.getDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate());

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<ProductionManageProductDataStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("SUM(" + ProductionManageProductDataStatistics.COL_WEIGHT + ") AS weight", "DATE_FORMAT(" + ProductionManageProductDataStatistics.COL_OPERATE_DATE + ", '%Y-%m-%d') AS operateDate, " + ProductionManageProductDataStatistics.COL_STATISTICS_TYPE + " AS statisticsType");
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageProductDataStatistics.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProductDataStatistics.COL_ORGANIZATION_ID, organizationId)
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), "DATE_FORMAT(" + ProductionManageProductDataStatistics.COL_OPERATE_DATE + ", '%Y-%m-%d')", requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), "DATE_FORMAT(" + ProductionManageProductDataStatistics.COL_OPERATE_DATE + ", '%Y-%m-%d')", requestDTO.getEndQueryDate())
                .groupBy(ProductionManageProductDataStatistics.COL_STATISTICS_TYPE, "DATE_FORMAT(operate_date, '%Y-%m-%d')")
                .orderByDesc("DATE_FORMAT(" + ProductionManageProductDataStatistics.COL_OPERATE_DATE + ", '%Y-%m-%d')");
        // 获取分拣报损，库存报损统计数据
        List<ProductionManageProductDataStatistics> dataStatistics = baseMapper.selectList(queryWrapper);

        // 获取采收重量，采收报损重量统计数据
    /*    CustomAssert.isNotBlank(startTime, "开始时间不可为空");
        CustomAssert.isNotBlank(endTime, "结束时间不可为空");*/
        List<HarvestStatisticsResponseDTO> harvestStatistics = commonUtil.listHarvestStatisticsByDateInterval(requestDTO.getStartQueryDate(), requestDTO.getEndQueryDate(), commonUtil.getSuperToken(), 1);

        // 统计产量
        handleProductAndHarvestStatistics(dateInterval, responseVO, harvestStatistics, ProductionDataStatisticsEnum.PRODUCTION_WEIGHT);

        // 统计采收报损
        handleProductAndHarvestStatistics(dateInterval, responseVO, harvestStatistics, ProductionDataStatisticsEnum.HARVEST_LOSSES_WEIGHT);

        // 统计分拣报损
        handleSortingAndStockStatistics(dateInterval, responseVO, dataStatistics, ProductionDataStatisticsEnum.SORTING_LOSSES_WEIGHT);

        // 统计库存报损
        handleSortingAndStockStatistics(dateInterval, responseVO, dataStatistics, ProductionDataStatisticsEnum.STOCK_LOSSES_WEIGHT);

        return responseVO;
    }

    /**
     * 获取区域销售排名
     *
     * @return
     */
    public PageResults<List<GreenhouseStatisticsResponseVO>> listGreenhouseRanking(SearchDateRequestDTO requestDTO) throws SuperCodeException {
        // 通过订单完成时间区间来获取订单总额(批次只针对采收批次，非外采批次)
        Page<OrderAndBatchBO> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<GreenhouseStatisticsResponseVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), "DATE_FORMAT(mo." + ProductionManageOrder.COL_DONE_DATE + ", '%Y-%m-%d')", requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), "DATE_FORMAT(mo." + ProductionManageOrder.COL_DONE_DATE + ", '%Y-%m-%d')", requestDTO.getEndQueryDate())
                .eq(StringUtils.isNotBlank(sysId), "mo." + ProductionManageOrder.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "mo." + ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .eq("mo." + ProductionManageOrder.COL_ORDER_STATUS, OrderStatusEnum.DONE.getStatus())
                .eq("si." + ProductionManageSortInstorage.COL_TYPE, SortingInventoryTypeEnum.HARVEST_SORTING.getKey())
                .groupBy("pm." + ProductionManageOutboundPackageMessage.COL_GREENHOUSE_ID)
                .orderByDesc("SUM(mo." + ProductionManageOrder.COL_RECEIVED_ORDER_MONEY + ")");

        IPage<OrderAndBatchBO> iPage = baseMapper.listTotalAmount(page, queryWrapper);
        PageResults<List<GreenhouseStatisticsResponseVO>> pageResult = new PageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResult.setPagination(pagination);

        List<OrderAndBatchBO> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResult;
        }

        String[] batchInfoIds = records.stream().map(OrderAndBatchBO::getPlantBatchId).toArray(String[]::new);
        List<PlantingBatchResponseDTO> plantingBatch = commonUtil.listBatchMsgByBatchIds(batchInfoIds);
        // 设置排名值
        long rank = (iPage.getCurrent() - 1) * iPage.getSize();
        AtomicLong ranking = new AtomicLong(rank);

        List<GreenhouseStatisticsResponseVO> list = records.stream().map(record -> {
            PlantingBatchResponseDTO plant = plantingBatch.stream().filter(batch -> batch.getTraceBatchInfoId().equals(record.getPlantBatchId())).findFirst().get();
            GreenhouseStatisticsResponseVO responseVO = new GreenhouseStatisticsResponseVO();
            responseVO.setGreenhouseId(plant.getMassId());
            responseVO.setGreenhouseName(plant.getMassIfName());
            responseVO.setPlantArea(plant.getMassArea());
            responseVO.setProductWeight(plant.getHarvestQuantity());
            responseVO.setPrincipalName(plant.getPrincipalName());
            responseVO.setRank(ranking.addAndGet(1));
            responseVO.setSaleAmount(record.getReceivedOrderMoney());
            return responseVO;
        }).collect(Collectors.toList());

        pageResult.setList(list);

        return pageResult;
    }


    /**
     * 生成生产数据vo
     *
     * @return
     */
    private ProductDataResponseVO getProductData() {
        ProductDataResponseVO responseVO = new ProductDataResponseVO();
        responseVO.setValues(new ArrayList<>(4));
        return responseVO;
    }

    /**
     * 生产数据统计初始化
     *
     * @return
     */
    private LineChartVO initProductData(ProductDataResponseVO responseVO, ProductionDataStatisticsEnum statisticsEnum) {

        // 初始化相关数据
        List<LineChartVO> values = responseVO.getValues();
        LineChartVO vo = new LineChartVO();
        LineChartVO.Option option = vo.new Option();
        option.setName(statisticsEnum.getValue());
        vo.setOption(option);
        values.add(vo);
        return vo;
    }

    /**
     * 统计分拣报损, 库存报损
     */
    private void handleSortingAndStockStatistics(List<String> dateInterval, ProductDataResponseVO responseVO, List<ProductionManageProductDataStatistics> dataStatistics, ProductionDataStatisticsEnum dataStatisticsEnum) {
        // 过滤指定类型下的生产数据
        List<ProductionManageProductDataStatistics> filterStatistics = dataStatistics.stream().filter(data -> dataStatisticsEnum.getKey() == data.getStatisticsType()).collect(Collectors.toList());
        // 初始化分拣/库存报损相关数据
        LineChartVO vo = initProductData(responseVO, dataStatisticsEnum);

        List<LineChartVO.NameAndValueVO> list;

        if (CollectionUtils.isEmpty(filterStatistics)) {
            list = addDefaultNav(vo, dateInterval);
        } else {
            Map<String, ProductionManageProductDataStatistics> map = filterStatistics.stream()
                    .collect(Collectors.toMap(data -> LocalDateTimeUtil.getYYD(data.getOperateDate()), data -> data));

            list = dateInterval.stream().map(date -> {
                ProductionManageProductDataStatistics statistics = map.get(date);
                if (Objects.isNull(statistics)) {
                    LineChartVO.NameAndValueVO nav = vo.new NameAndValueVO();
                    nav.setName(date);
                    nav.setValue(0);
                    return nav;
                } else {
                    LineChartVO.NameAndValueVO nav = vo.new NameAndValueVO();
                    nav.setName(LocalDateTimeUtil.getYYD(statistics.getOperateDate()));
                    nav.setValue(statistics.getWeight());
                    return nav;
                }
            }).collect(Collectors.toList());
        }

        vo.setValues(list);
        BigDecimal totalWeight = filterStatistics.stream().map(statistics -> Optional.ofNullable(statistics.getWeight()).orElse(new BigDecimal(0))).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (dataStatisticsEnum == ProductionDataStatisticsEnum.STOCK_LOSSES_WEIGHT) {
            responseVO.setStockLossesWeight(totalWeight);
        } else {
            responseVO.setSortingLossesWeight(totalWeight);
        }
    }

    /**
     * 统计采收报损, 产量
     */
    private void handleProductAndHarvestStatistics(List<String> dateInterval, ProductDataResponseVO responseVO, List<HarvestStatisticsResponseDTO> harvestStatistics, ProductionDataStatisticsEnum dataStatisticsEnum) {
        // 初始化分拣/库存报损相关数据
        LineChartVO vo = initProductData(responseVO, dataStatisticsEnum);

        List<LineChartVO.NameAndValueVO> list;

        if (CollectionUtils.isEmpty(harvestStatistics)) {
            list = addDefaultNav(vo, dateInterval);
        } else {
            Map<String, HarvestStatisticsResponseDTO> map = harvestStatistics.stream().collect(Collectors.toMap(harvest -> harvest.getHarvestDate(), harvest -> harvest));
            list = dateInterval.stream().map(date -> {
                HarvestStatisticsResponseDTO statistic = map.get(date);
                if (Objects.isNull(statistic)) {
                    LineChartVO.NameAndValueVO nav = vo.new NameAndValueVO();
                    nav.setName(date);
                    nav.setValue(0);
                    return nav;
                } else {
                    LineChartVO.NameAndValueVO nav = vo.new NameAndValueVO();
                    nav.setName(statistic.getHarvestDate());
                    nav.setValue(dataStatisticsEnum == ProductionDataStatisticsEnum.HARVEST_LOSSES_WEIGHT ? statistic.getHarvestDamagedQuantity() : statistic.getHarvestQuantity());
                    return nav;
                }
            }).collect(Collectors.toList());
        }

        vo.setValues(list);

        if (dataStatisticsEnum == ProductionDataStatisticsEnum.HARVEST_LOSSES_WEIGHT) {
            responseVO.setProductionWeight(harvestStatistics.stream().map(statistics -> Optional.ofNullable(statistics.getHarvestQuantity()).orElse(new BigDecimal(0))).reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            responseVO.setHarvestLossesWeight(harvestStatistics.stream().map(statistics -> Optional.ofNullable(statistics.getHarvestDamagedQuantity()).orElse(new BigDecimal(0))).reduce(BigDecimal.ZERO, BigDecimal::add));
        }
    }

    /**
     * 生成默认的生产数据
     */
    private List<LineChartVO.NameAndValueVO> addDefaultNav(LineChartVO vo, List<String> dateInterval) {
        return dateInterval.stream().map(date -> {
            LineChartVO.NameAndValueVO nav = vo.new NameAndValueVO();
            nav.setName(date);
            nav.setValue(0);
            return nav;
        }).collect(Collectors.toList());
    }

    /**
     * 区域排名导出
     * @param requestDTO
     * @param response
     */
    public void greenhouseRankExport(SearchDateRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> ids = requestDTO.getIdList();
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(commonUtil.getExportNumber());
        PageResults<List<GreenhouseStatisticsResponseVO>> results = listGreenhouseRanking(requestDTO);
        List<GreenhouseStatisticsResponseVO> list = results.getList();
        List<GreenhouseStatisticsResponseVO> exportList;

        // 如果ids为空则全部导出
        if (CollectionUtils.isNotEmpty(ids)) {
            exportList = ids.stream().map(id -> list.stream()
                            .filter(responseVO -> id.equals(responseVO.getRank().toString())).findFirst().get()).collect(Collectors.toList());
        } else {
            exportList = list;
        }

        ExcelUtils.listToExcel(exportList, requestDTO.exportMetadataToMap(), "区域排名列表", response);
    }

    /**
     * 获取当天完成订单下的订单销售额，实付总额以及区域相关信息
     *
     * @author shixiongfei
     * @date 2019-09-19
     * @updateDate 2019-09-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<OrderAndGreenhouseBO> listByCurrentDate(String currentDate, String endDate) {
        return baseMapper.listByCurrentDate(currentDate, endDate, (byte) OrderStatusEnum.DONE.getStatus());
    }
}