package com.zxs.server.service.gugeng.statistics;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchStockLossStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsReportLoss;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStockLossDayStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsReportLossMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.StockLossStatisticsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchHSLLineChartResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseOverviewStatisticsService  extends BaseSummaryStatisticsService  {

    @Autowired
    private StockLossStatisticsMapper baseMapper;

    @Autowired
    private ProductionManageStatisticsReportLossMapper reportLossMapper;

    @Autowired
    private CommonUtil commonUtil;

    public LineChartVO listStockLoss(String queryDate, SearchHSLLineChartResponseVO responseVO) throws Exception {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        SearchStockLossStatisticsRequestDTO requestDTO=new SearchStockLossStatisticsRequestDTO();
        requestDTO.setStartQueryDate(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        requestDTO.setEndQueryDate(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        List<String> dateList= DateUtils.dateZone(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        // 获取时间区间内的库存报损数据列表
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

/*        QueryWrapper<ProductionManageStockLossDayStatistics> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), "SysId", sysId)
                .eq(StringUtils.isNotBlank(organizationId), "OrganizationId", organizationId)
                .eq("OperateType", StockLossOperateTypeEnum.STOCK_LOSS.getKey())
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), "DamageDate", requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), "DamageDate", LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                //.in(Objects.nonNull(types), "Type", types)
                //.in(CollectionUtils.isNotEmpty(requestDTO.getPlantBatchIds()), "PlantBatchId", requestDTO.getPlantBatchIds())

        // 按照时间升序，加快图表响应体的生成速度
        ;*/

        QueryWrapper<ProductionManageStatisticsReportLoss> wrapper1 = new QueryWrapper<>();
        wrapper1.eq(StringUtils.isNotBlank(sysId), ProductionManageStatisticsReportLoss.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStatisticsReportLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_TYPE, 2)
                .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE, requestDTO.getStartQueryDate())
                .lt(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1));
        //wrapper1.ne("department_id", "").isNotNull("department_id");
        wrapper1.select("date_format(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE + ",'%Y-%m-%d') damageDate, sum(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_WEIGHT + ") damageWeight");
        wrapper1.groupBy("date_format(" + ProductionManageStatisticsReportLoss.COL_REPORT_LOSS_DATE + ",'%Y-%m-%d') ");
        List<Map<String, Object>> list1 = reportLossMapper.selectMaps(wrapper1);
        List<ProductionManageStockLossDayStatistics> list = JSONObject.parseArray(JSONObject.toJSONString(list1), ProductionManageStockLossDayStatistics.class);

        LineChartVO chart0 = createLineChartVO("库存报损");
        List<LineChartVO> values = new ArrayList<>();
        values.add(chart0);

        BigDecimal totalStockLossWeight = new BigDecimal(0);

        if (CollectionUtils.isEmpty(list)) {
            for (String date : dateList) {
                chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
            }
        } else {
            for (String date : dateList) {
                ProductionManageStockLossDayStatistics status0 = list.stream().filter(e -> e.getDamageDate().equals(date)).findFirst().orElse(null);
                if (status0 != null) {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, status0.getDamageWeight()));
                    totalStockLossWeight = totalStockLossWeight.add(BigDecimal.valueOf(status0.getDamageWeight()));
                } else {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                }
            }
        }

        responseVO.getValues().add(chart0);
        responseVO.setTotalStockLossWeight(totalStockLossWeight);
        responseVO.setTotalReportLossWeight(totalStockLossWeight);

        return chart0;
    }

}
