package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductAndSaleDataExcelDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestPlan;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.StatisticsExcelBaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.diagramtransfer.LineChartDataTransfer;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductAndSaleDataResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

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
public class ProductionManageProductAndSaleDataStatisticsService implements StatisticsExcelBaseService {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageHarvestPlanMapper harvestPlanMapper;

    @Autowired
    private ProductionManageStockLossMapper stockLossMapper;

    @Autowired
    private ProductionManageSortInstorageMapper sortInstorageMapper;

    @Autowired
    private ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    private static String defaultNameKey = "dateTime";
    private static String defaultValueKey = "totalValue";


    public ProductAndSaleDataResponseVO lineChart(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        ProductAndSaleDataResponseVO productAndSaleDataResponseVO = new ProductAndSaleDataResponseVO();
        List<LineChartVO> lineChartVOS = new ArrayList<>();
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());

        List<String> dateList = DateUtils.dateZone(startQueryDate, endQueryDate);

        //1.获取计划采收重量统计数据
        List<Map<String, Object>> harvestPlanList = statisticsHarvertPlan(startQueryDate, endQueryDate);
        LineChartVO harvestPlanLineChartVO = LineChartDataTransfer.transfer(dateList, harvestPlanList, defaultNameKey, defaultValueKey, "计划采收重量");

        //2.报损重量
        List<Map<String, Object>> lossWeightList = lossWeight(startQueryDate, endQueryDate);
        LineChartVO lossWeightLineChartVO = LineChartDataTransfer.transfer(dateList, lossWeightList, "name", "value", "报损重量");

        //3.入库重量
        List<Map<String, Object>> inStorageWeightList = inStorageWeight(startQueryDate, endQueryDate);
        LineChartVO inStorageLineChartVO = LineChartDataTransfer.transfer(dateList, inStorageWeightList, defaultNameKey, defaultValueKey, "入库重量");

        //4.出库重量
        List<Map<String, Object>> outStorageWeightList = outStorageWeight(startQueryDate, endQueryDate);
        LineChartVO outStorageLineChartVO = LineChartDataTransfer.transfer(dateList, outStorageWeightList, defaultNameKey, defaultValueKey, "出库重量");
        //5.销售额
        List<Map<String, Object>> orderList = orderMapper.amountAndNum(dateIntervalDTO);
        LineChartVO saleMoneyLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderMoney", "销售额");
        //6.实收额
        LineChartVO receivedSaleMoneyLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "receivedOrderMoney", "实收额");

        //7.种植批次
        String plantBatchUrl = "/trace/hainanrunda/massifinfostatistic/selectBatchCountByHarvestDate";
        List<LinkedHashMap<String, Object>> plantBatchNumList = tracePlantBatchNumOrRarvestStaticis(startQueryDate, endQueryDate, plantBatchUrl);
        LineChartVO plantBatchNumLineChartVO = LineChartDataTransfer.transfer(dateList, plantBatchNumList, "name", "value", "种植批次数");
        //8.实际采收重量
        String ealRarvestUrl = "/trace/hainanrunda/massifinfostatistic/selectHarvestQuantityByHarvestDate";
        List<LinkedHashMap<String, Object>> realRarvestPlanList = tracePlantBatchNumOrRarvestStaticis(startQueryDate, endQueryDate, ealRarvestUrl);
        LineChartVO realRarvestPlanLineChartVO = LineChartDataTransfer.transfer(dateList, realRarvestPlanList, "name", "value", "实际采收重量");

        lineChartVOS.add(plantBatchNumLineChartVO);
        lineChartVOS.add(harvestPlanLineChartVO);
        lineChartVOS.add(realRarvestPlanLineChartVO);
        lineChartVOS.add(lossWeightLineChartVO);
        lineChartVOS.add(inStorageLineChartVO);
        lineChartVOS.add(outStorageLineChartVO);
        lineChartVOS.add(saleMoneyLineChartVO);
        lineChartVOS.add(receivedSaleMoneyLineChartVO);

        productAndSaleDataResponseVO.setValues(lineChartVOS);
        productAndSaleDataResponseVO.setHarvestPlanWeight(harvestPlanLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setInStorageWeight(inStorageLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setLossesWeight(lossWeightLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setOutStorageWeight(outStorageLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setOrderMoney(saleMoneyLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setReceivedOrderMoney(receivedSaleMoneyLineChartVO.getTotalValue());
        productAndSaleDataResponseVO.setPlantBatchNum(plantBatchNumLineChartVO.getTotalValue().intValue());
        productAndSaleDataResponseVO.setHarvestRealWeight(realRarvestPlanLineChartVO.getTotalValue());
        return productAndSaleDataResponseVO;
    }

    private List<LinkedHashMap<String, Object>> tracePlantBatchNumOrRarvestStaticis(String startQueryDate, String endQueryDate, String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("startQueryDate", startQueryDate);
        params.put("endQueryDate", endQueryDate);
        Map<String, String> header = new HashMap<>();
        header.put("super-token", commonUtil.getSuperToken());
        RestResult restResult = commonUtil.codeRequests.getAndGetResultBySpring(commonUtil.traceDomain + url, params, header, RestResult.class, commonUtil.traceIsLoadBalanced);
        LinkedHashMap linkedHashMap = (LinkedHashMap) restResult.getResults();
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        if (null != linkedHashMap && !linkedHashMap.isEmpty()) {
            dataList = (List<LinkedHashMap<String, Object>>) linkedHashMap.get("list");
        }
        return dataList;
    }

    /**
     * 入库重量
     *
     * @param startQueryDate
     * @param endQueryDate
     * @return
     * @throws SuperCodeException
     */
    private List<Map<String, Object>> inStorageWeight(String startQueryDate, String endQueryDate) throws SuperCodeException {

        //1分拣入库
        QueryWrapper<ProductionManageSortInstorage> sortInstorageQueryWrapper = commonUtil.queryTemplate(ProductionManageSortInstorage.class);
        List<Map<String, Object>> sortingLossList = singleFieldSumSelect("Weight", "CreateDate", startQueryDate, endQueryDate, "production_manage_sort_instorage", true, sortInstorageMapper, sortInstorageQueryWrapper, null, null);
        //2盘点入库
        List<Map<String, Object>> stockCheckList = null;
        return listMapAdd(sortingLossList, stockCheckList);
    }

    /**
     * 出库重量
     *
     * @param startQueryDate
     * @param endQueryDate
     * @throws SuperCodeException
     */
    private List<Map<String, Object>> outStorageWeight(String startQueryDate, String endQueryDate) throws SuperCodeException {
        //1正常出库
        QueryWrapper<ProductionManageOutbound> outboundQueryWrapper = commonUtil.queryTemplate(ProductionManageOutbound.class);
        List<Map<String, Object>> outboundList = singleFieldSumSelect("PackingWeight", "OutboundDate", startQueryDate, endQueryDate, "t_production_manage_outbound", true, outboundMapper, outboundQueryWrapper, null, null);
        List<Map<String, Object>> stockCheckList = null;
        return listMapAdd(outboundList, stockCheckList);
    }

    /**
     * 报损重量
     *
     * @param startQueryDate
     * @param endQueryDate
     * @throws SuperCodeException
     */
    private List<Map<String, Object>> lossWeight(String startQueryDate, String endQueryDate) throws SuperCodeException {
        //2获取库存报损重量
        QueryWrapper<ProductionManageStockLoss> stockLossQueryWrapper = commonUtil.queryTemplate(ProductionManageStockLoss.class);
        List<Map<String, Object>> stockLossList = singleFieldSumSelect("DamageWeight", "DamageDate", startQueryDate, endQueryDate, "production_manage_stock_loss", true, stockLossMapper, stockLossQueryWrapper, "name", "value");
        //3获取溯源采收报损重量
        List<LinkedHashMap<String, Object>> traceLossList = tracePlantBatchNumOrRarvestStaticis(startQueryDate, endQueryDate, "/trace/hainanrunda/massifinfostatistic/selectDamagedQuantityByHarvestDate");
        List<Map<String, Object>> mapList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(traceLossList)) {
            mapList.addAll(traceLossList);
        }
        return listMapAdd(null, stockLossList, mapList);
    }

    /**
     * 采收计划重量
     *
     * @return
     */
    private List<Map<String, Object>> statisticsHarvertPlan(String startQueryDate, String endQueryDate) throws SuperCodeException {
        QueryWrapper<ProductionManageHarvestPlan> harvestPlanQueryWrapper = commonUtil.queryTemplate(ProductionManageHarvestPlan.class);
        harvestPlanQueryWrapper.groupBy("harvestDate");
        List<Map<String, Object>> mapList = singleFieldSumSelect("ProductionForecast", "HarvestDate", startQueryDate, endQueryDate, "production_manage_harvest_plan", true, harvestPlanMapper, harvestPlanQueryWrapper, null, null);
        return mapList;
    }

    /**
     * 查询模板方法
     *
     * @param statisticsField
     * @param dateField
     * @param startQueryDate
     * @param endQueryDate
     * @param tableName
     * @param dateNeddFormart
     * @param baseMapper
     * @param queryWrapper
     * @param <T>
     * @return
     * @throws SuperCodeException
     */
    private <T> List<Map<String, Object>> singleFieldSumSelect(String statisticsField, String dateField, String startQueryDate, String endQueryDate, String tableName,
                                                               boolean dateNeddFormart, BaseMapper baseMapper, QueryWrapper<T> queryWrapper, String customNameKey, String customValueKey) throws SuperCodeException {
        //1分拣入库
        String date = dateField;
        if (dateNeddFormart) {
            date = "DATE_FORMAT(" + dateField + ",'%Y-%m-%d')";
        }
        String lowerStatisticsField = statisticsField.substring(0, 1).toLowerCase() + statisticsField.substring(1, statisticsField.length());
        String lowerDateField = dateField.substring(0, 1).toLowerCase() + dateField.substring(1, dateField.length());

        StringBuilder selectBuilder = new StringBuilder();
        selectBuilder.append("sum(").append(statisticsField).append(")").append(" as ");
        if (StringUtils.isBlank(customValueKey)) {
            selectBuilder.append(defaultValueKey);
        } else {
            selectBuilder.append(customValueKey);
        }
        selectBuilder.append(",").append(date).append(" as ");
        if (StringUtils.isBlank(customNameKey)) {
            selectBuilder.append(defaultNameKey);
        } else {
            selectBuilder.append(customNameKey);
        }
        String selectSql = selectBuilder.toString();
        queryWrapper.select(selectSql);
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate), date, startQueryDate);
        queryWrapper.le(StringUtils.isNotBlank(endQueryDate), date, endQueryDate);
        queryWrapper.groupBy(date);
        List<Map<String, Object>> mapList = baseMapper.selectMaps(queryWrapper);
        return mapList;
    }

    public List<Map<String, Object>> listMapAdd(List<Map<String, Object>>... mapList) {
        List<Map<String, Object>> allListMap = new ArrayList<>();
        if (null != mapList) {
            for (int i = 0; i < mapList.length; i++) {
                allListMap.addAll(mapList[i]);
            }
        }
        return allListMap;
    }

    public void exportProductSaleStatistics(ProductAndSaleDataExcelDTO dateIntervalListDTO, HttpServletResponse response) throws SuperCodeException {
        @Data
        class ProductAndSaleDataExcel {
            /**
             * 种植批次数
             */
            @ApiModelProperty(notes = "种植批次数")
            private Integer plantBatchNum;

            /**
             * 计划采收重量
             */
            @ApiModelProperty(notes = "计划采收重量")
            private BigDecimal harvestPlanWeight;

            /**
             * 计划采收重量
             */
            @ApiModelProperty(notes = "实际采收重量")
            private BigDecimal harvestRealWeight;

            /**
             * 入库重量
             */
            @ApiModelProperty(notes = "入库重量")
            private BigDecimal inStorageWeight;

            /**
             * 报损重量
             */
            @ApiModelProperty(notes = "报损重量")
            private BigDecimal lossesWeight;

            @ApiModelProperty(notes = "出库重量")
            private BigDecimal outStorageWeight;

            @ApiModelProperty(notes = "销售额")
            private BigDecimal orderMoney;

            @ApiModelProperty(notes = "实收额")
            private BigDecimal receivedOrderMoney;
        }
        ProductAndSaleDataExcel productAndSaleDataExcel = new ProductAndSaleDataExcel();
        BeanUtils.copyProperties(dateIntervalListDTO, productAndSaleDataExcel);
        List<ProductAndSaleDataExcel> list = new ArrayList<>();
        list.add(productAndSaleDataExcel);
        ExcelUtils.listToExcel(list, dateIntervalListDTO.exportMetadataToMap(), "生产销售数据", response);
    }
}