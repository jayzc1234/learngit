package com.zxs.server.service.gugeng.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.enums.DatePatternEnum;
import net.app315.hydra.intelligent.planting.mybatis.CustomFunctionBuilder;
import net.app315.hydra.intelligent.planting.mybatis.YsLambdaQueryWrapper;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageSortInstorageService;
import net.app315.nail.common.result.RichResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存统计
 * @author zc
 */
@AllArgsConstructor
@Service
public class ProductionManageStockStatisticsService {

    private ProductionManageSortInstorageService inStorageService;

    private ProductionManageOutboundPackageMessageMapper outboundPackageMessageMapper;

    private CommonUtil commonUtil;
    /**
     * 计算近六个月和前年近六个月的库存信息
     * @param productId
     * @return
     */
    public RichResult<QueryTimeVO> nearlySixMonthsStock(String productId) {
        QueryTime queryTime = queryTime();
        List<String> list = LocalDateTimeUtil.intervalTimes(queryTime.getCStartTime(), queryTime.getCEndTime(), DatePatternEnum.MONTH_PATTERN);
        List<BigDecimal> current_bigDecimals = stockStatistics(productId, queryTime.getCStartTime(), queryTime.getCEndTime());
        List<BigDecimal> history_bigDecimals = stockStatistics(productId, queryTime.getLStartTime(), queryTime.getLEndTime());
        QueryTimeVO queryTimeVO = this.new QueryTimeVO();
        queryTimeVO.setXData(list);
        queryTimeVO.setY1(current_bigDecimals);
        queryTimeVO.setY2(history_bigDecimals);
        return RestResult.ok(queryTimeVO);
    }

    private List<BigDecimal> stockStatistics(String productId, String startTime, String endTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");

        List<BigDecimal> stockList = new ArrayList<>();
        BigDecimal inHistory = sumIn(productId, startTime);
        BigDecimal outHistory = sumOut(productId, startTime);
        Map<String, BigDecimal> inMap = inList(productId, startTime, endTime).stream().collect(Collectors.toMap(c->format.format(c.getCreateDate()), c->c.getWeight().add(inHistory)));
        Map<String, BigDecimal> outMap = outList(productId, startTime, endTime).stream().collect(Collectors.toMap(c->format.format(c.getPackingDate()), c->c.getPackingWeight().add(outHistory)));
        List<String> list = LocalDateTimeUtil.intervalTimes(startTime, endTime, DatePatternEnum.MONTH_PATTERN);
        for (String date : list) {
            BigDecimal in_bigDecimal = inMap.get(date);
            BigDecimal out_bigDecimal = outMap.get(date);
            stockList.add(CommonUtil.bigDecimalSub(in_bigDecimal,out_bigDecimal));
        }
        return stockList;
    }


    private List<ProductionManageOutboundPackageMessage> outList(String productId, String startTime, String endTime) {
        YsLambdaQueryWrapper<ProductionManageOutboundPackageMessage> queryWrapper = new YsLambdaQueryWrapper<>();
        queryWrapper.eq(CustomFunctionBuilder.build(ProductionManageOutbound.COL_ORGANIZATION_ID),commonUtil.getOrganizationId())
                .eq(CustomFunctionBuilder.build(ProductionManageOutbound.COL_SYS_ID),commonUtil.getSysId())
                .eq(StringUtils.isNotBlank(productId),ProductionManageOutboundPackageMessage::getProductId,productId)
                .lt(ProductionManageOutboundPackageMessage::getPackingDate,startTime)
                .groupBy(CustomFunctionBuilder.build("date_format("+ProductionManageOutboundPackageMessage.COL_PACKING_DATE+",'%Y-%m')"));
        List<ProductionManageOutboundPackageMessage> outboundWeightGroupDate = outboundPackageMessageMapper.getOutboundWeightGroupDate(queryWrapper);
        return Objects.isNull(outboundWeightGroupDate)?new ArrayList<>():outboundWeightGroupDate;
    }

    private List<ProductionManageSortInstorage> inList(String productId, String startTime, String endTime) {
        YsLambdaQueryWrapper<ProductionManageSortInstorage> queryWrapper = new YsLambdaQueryWrapper<>();
        queryWrapper.select(CustomFunctionBuilder.build("sum(weight) as weight,date_format("+ProductionManageSortInstorage.COL_CREATE_DATE+",'%Y-%m')"));
        queryWrapper.eq(ProductionManageSortInstorage::getOrganizationId,commonUtil.getOrganizationId())
                .eq(ProductionManageSortInstorage::getSysId,commonUtil.getSysId())
                .eq(StringUtils.isNotBlank(productId),ProductionManageSortInstorage::getProductId,productId)
                .le(ProductionManageSortInstorage::getCreateDate,endTime)
                .ge(ProductionManageSortInstorage::getCreateDate,startTime)
                .groupBy(CustomFunctionBuilder.build("date_format("+ProductionManageSortInstorage.COL_CREATE_DATE+",'%Y-%m')"));
        List<ProductionManageSortInstorage> list = inStorageService.list(queryWrapper);
        return Objects.isNull(list)?new ArrayList<>():list;
    }

    private BigDecimal sumOut(String productId, String startTime) {
        YsLambdaQueryWrapper<ProductionManageOutboundPackageMessage> queryWrapper = new YsLambdaQueryWrapper<>();
        queryWrapper.eq(CustomFunctionBuilder.build(ProductionManageOutbound.COL_ORGANIZATION_ID),commonUtil.getOrganizationId())
                .eq(CustomFunctionBuilder.build(ProductionManageOutbound.COL_SYS_ID),commonUtil.getSysId())
                .eq(StringUtils.isNotBlank(productId),ProductionManageOutboundPackageMessage::getProductId,productId)
                .lt(ProductionManageOutboundPackageMessage::getPackingDate,startTime);
        BigDecimal outboundWeight = outboundPackageMessageMapper.getOutboundWeight(queryWrapper);
        return Optional.ofNullable(outboundWeight).orElse(BigDecimal.ZERO);
    }

    private BigDecimal sumIn(String productId, String startTime) {
        YsLambdaQueryWrapper<ProductionManageSortInstorage> queryWrapper = new YsLambdaQueryWrapper<>();
        queryWrapper.select(CustomFunctionBuilder.build("ifnull(sum(weight),0) as weight"));
        queryWrapper.addSelectSupplier(ProductionManageSortInstorage.COL_WEIGHT,(a)->{return "sum("+a+") as weight";});
        queryWrapper.eq(ProductionManageSortInstorage::getOrganizationId,commonUtil.getOrganizationId())
                .eq(ProductionManageSortInstorage::getSysId,commonUtil.getSysId())
                .eq(StringUtils.isNotBlank(productId),ProductionManageSortInstorage::getProductId,productId)
                .lt(ProductionManageSortInstorage::getCreateDate,startTime);
        /**
         * 历史数据
         */
        List<ProductionManageSortInstorage> list = inStorageService.list(queryWrapper);
        BigDecimal historyTotal = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(list)){
            historyTotal = list.get(0).getWeight();
        }
        return historyTotal;
    }


    private QueryTime queryTime() {
        QueryTime queryTime = new QueryTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-1);
        String start = format.format(calendar.getTime());
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)-5);
        String end = format.format(calendar.getTime());
        queryTime.setCEndTime(start);
        queryTime.setCStartTime(end);

        calendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR)-1);
        String lEnd = format.format(calendar.getTime());
        calendar.set(Calendar.MONTH,calendar.get(Calendar.MONTH)+5);
        String lStart = format.format(calendar.getTime());
        queryTime.setLStartTime(lEnd);
        queryTime.setLEndTime(lStart);
        return queryTime;
    }

    /**
     * 前缀c表示current,l表示last
     */
    @Data
    private class QueryTime{
        private String cStartTime;
        private String cEndTime;
        private String lStartTime;
        private String lEndTime;
    }

    /**
     * 前缀c表示current,l表示last
     */
    @Data
    public class QueryTimeVO{
        private List<String> xData;
        private List<BigDecimal> y1;
        private List<BigDecimal> y2;
    }
}
