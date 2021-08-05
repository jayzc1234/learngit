package com.zxs.server.service.gugeng.statistics;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.HarvestStatisticsResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionYieldDataRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.producemanage.ProductionManageHarvestPlan;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageProduceWeightDayStatistics;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductionYieldData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageWeight;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.producemanage.ProductionManageHarvestPlanMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageProduceWeightDayStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsProductionYieldDataMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageWeightMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageProduceWeightDayStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.RealWeightAndLossesWeightVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProduceSummaryStatisticsService extends BaseSummaryStatisticsService {

    @Autowired
    private ProductionManageProduceWeightDayStatisticsMapper produceWeightDayStatisticsMapper;

    @Autowired
    private ProductionManageHarvestPlanMapper harvestPlanMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageWeightMapper productionManageWeightMapper;

    @Autowired
    private ProductionManageStatisticsProductionYieldDataMapper productionYieldDataMapper;


    @Autowired
    private ObjectConverter<ProductionManageProduceWeightDayStatistics, ProductionManageProduceWeightDayStatisticsVO> objectConverter;

    @Transactional(rollbackFor = Exception.class)
    public void statisticsProduceData() throws Exception {

        String endQueryDate= DateUtils.dateFormat(new Date(), LocalDateTimeUtil.DATE_PATTERN);
        List<Map<String, Object>> harvestPlanList = statisticsHarvertPlan(null, endQueryDate);

        if(superToken==null){
            superToken=new ProductionManageSuperToken();
            superToken.setToken(commonUtil.getSuperToken());
            superToken.setOrganizationId(commonUtil.getOrganizationId());
            superToken.setSysId(commonUtil.getSysId());
        }

        List<HarvestStatisticsResponseDTO> harvestStatistics = listHarvestStatisticsByDateInterval(null, null, superToken.getToken());

        List<Map<String, Object>> weightingList = statisticsWeighting(null, null);

        List<String> inStorageDates = harvestPlanList.stream().map(e -> String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        List<String> outStorageDates = harvestStatistics.stream().map(e -> e.getHarvestDate()).collect(Collectors.toList());
        List<String> stockWeightDates = weightingList.stream().filter(e -> e != null && e.get("dateTime") != null).map(e -> String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        inStorageDates.addAll(outStorageDates);
        inStorageDates.addAll(stockWeightDates);

        inStorageDates = inStorageDates.stream().distinct().sorted().collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(inStorageDates)) {
            QueryWrapper<ProductionManageProduceWeightDayStatistics> wrapper= queryTemplate();
            produceWeightDayStatisticsMapper.delete(wrapper);
            for (String date : inStorageDates) {
                ProductionManageProduceWeightDayStatistics weightDayStatistics = new ProductionManageProduceWeightDayStatistics();
                weightDayStatistics.setOrganizationId(superToken.getOrganizationId());
                weightDayStatistics.setSysId(superToken.getSysId());
                weightDayStatistics.setOperateDate(date);

                Map<String, Object> harvestPlan = harvestPlanList.stream().filter(e -> e.get("dateTime").toString().equals(date)).findFirst().orElse(null);
                if (harvestPlan != null) {
                    weightDayStatistics.setPlanWeight(Double.parseDouble(harvestPlan.get("totalValue").toString()));
                } else {
                    weightDayStatistics.setPlanWeight(0d);
                }
                HarvestStatisticsResponseDTO harvestStatisticsResponseDTO = harvestStatistics.stream().filter(e -> e.getHarvestDate().equals(date)).findFirst().orElse(null);
                if (harvestStatisticsResponseDTO != null) {
                    BigDecimal harvestDamagedQuantity= harvestStatisticsResponseDTO.getHarvestDamagedQuantity();
                    weightDayStatistics.setLossesWeight(Double.parseDouble(harvestDamagedQuantity==null?"0":harvestDamagedQuantity.toString()));
                } else {
                    weightDayStatistics.setLossesWeight(0d);
                }
                Map<String, Object> weightingMap = weightingList.stream().filter(e -> e.get("dateTime").toString().equals(date)).findFirst().orElse(null);
                if (weightingMap != null) {
                    weightDayStatistics.setRealWeight(Double.parseDouble(weightingMap.get("totalValue").toString()));
                } else {
                    weightDayStatistics.setRealWeight(0d);
                }

                produceWeightDayStatisticsMapper.insert(weightDayStatistics);
            }
        }
    }

    public List<ProductionManageProduceWeightDayStatistics> selectTodayList() throws Exception {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = LocalDate.now().format(fmt);
        String end= LocalDate.now().plusDays(1).format(fmt);

        List<Map<String, Object>> harvestPlanList = statisticsHarvertPlan(start, end);

        List<HarvestStatisticsResponseDTO> harvestStatistics = listHarvestStatisticsByDateInterval(start, end, commonUtil.getSuperToken());
        harvestStatistics=harvestStatistics.stream().filter(e->e.getHarvestDate().equals(start)).collect(Collectors.toList());

        List<Map<String, Object>> weightingList = statisticsWeighting(start, end);

        List<ProductionManageProduceWeightDayStatistics> dayStatistics=new ArrayList<>();

        List<String> inStorageDates = harvestPlanList.stream().map(e -> String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        List<String> outStorageDates = harvestStatistics.stream().map(e -> e.getHarvestDate()).collect(Collectors.toList());
        List<String> stockWeightDates = weightingList.stream().filter(e -> e != null && e.get("dateTime") != null).map(e -> String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        inStorageDates.addAll(outStorageDates);
        inStorageDates.addAll(stockWeightDates);

        inStorageDates = inStorageDates.stream().distinct().sorted().collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(inStorageDates)) {
            for (String date : inStorageDates) {
                ProductionManageProduceWeightDayStatistics weightDayStatistics = new ProductionManageProduceWeightDayStatistics();

                weightDayStatistics.setOperateDate(date);

                Map<String, Object> harvestPlan = harvestPlanList.stream().filter(e -> e.get("dateTime").toString().equals(date)).findFirst().orElse(null);
                if (harvestPlan != null) {
                    weightDayStatistics.setPlanWeight(Double.parseDouble(harvestPlan.get("totalValue").toString()));
                } else {
                    weightDayStatistics.setPlanWeight(0d);
                }
                HarvestStatisticsResponseDTO harvestStatisticsResponseDTO = harvestStatistics.stream().filter(e -> e.getHarvestDate().equals(date)).findFirst().orElse(null);
                if (harvestStatisticsResponseDTO != null) {
                    BigDecimal harvestDamagedQuantity= harvestStatisticsResponseDTO.getHarvestDamagedQuantity();
                    if(harvestDamagedQuantity!=null){
                        String damagedQuantity= new DecimalFormat("#.00").format(harvestDamagedQuantity);
                        weightDayStatistics.setLossesWeight(Double.parseDouble(harvestDamagedQuantity==null?"0":damagedQuantity));
                    } else {
                        weightDayStatistics.setLossesWeight(0d);
                    }
                } else {
                    weightDayStatistics.setLossesWeight(0d);
                }
                Map<String, Object> weightingMap = weightingList.stream().filter(e -> e.get("dateTime").toString().equals(date)).findFirst().orElse(null);
                if (weightingMap != null) {
                    weightDayStatistics.setRealWeight(Double.parseDouble(weightingMap.get("totalValue").toString()));
                } else {
                    weightDayStatistics.setRealWeight(0d);
                }

                dayStatistics.add(weightDayStatistics);
            }
        }
        return dayStatistics;
    }

    private List<Map<String, Object>> statisticsHarvertPlan(String startQueryDate, String endQueryDate) throws SuperCodeException {
        QueryWrapper<ProductionManageHarvestPlan> harvestPlanQueryWrapper = queryTemplate();

        harvestPlanQueryWrapper.eq(ProductionManageHarvestPlan.COL_DELETE_OR_NOT, 0);
        harvestPlanQueryWrapper.groupBy(ProductionManageHarvestPlan.COL_HARVEST_DATE);

        List<Map<String, Object>> mapList = singleFieldSumSelect(ProductionManageHarvestPlan.COL_PRODUCTION_FORECAST, ProductionManageHarvestPlan.COL_HARVEST_DATE, startQueryDate, endQueryDate, "t_production_manage_harvest_plan", true, harvestPlanMapper, harvestPlanQueryWrapper, null, null);
        return mapList;
    }

    private List<Map<String, Object>> statisticsWeighting(String startQueryDate, String endQueryDate) throws SuperCodeException {
        QueryWrapper<ProductionManageHarvestPlan> harvestPlanQueryWrapper =  queryTemplate();
        harvestPlanQueryWrapper.eq(ProductionManageWeight.COL_TYPE, 1);

        List<Map<String, Object>> mapList = singleFieldSumSelect(ProductionManageWeight.COL_WEIGHT, ProductionManageWeight.COL_WEIGHING_DATE, startQueryDate, endQueryDate, "t_production_manage_weighting", true, productionManageWeightMapper, harvestPlanQueryWrapper, null, null);
        return mapList;
    }

    public RealWeightAndLossesWeightVO realWeightAndLossesWeight(SaleUserLineChartDTO dateIntervalDTO) throws Exception {

        QueryWrapper<ProductionManageProduceWeightDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageProduceWeightDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageProduceWeightDayStatistics.COL_OPERATE_DATE, dateIntervalDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageProduceWeightDayStatistics.COL_OPERATE_DATE, dateIntervalDTO.getEndQueryDate());

        List<ProductionManageProduceWeightDayStatistics> orderList = produceWeightDayStatisticsMapper.selectList(queryWrapper);

        List<ProductionManageProduceWeightDayStatistics> todayList = selectTodayList();
        if (dateIntervalDTO.getEndQueryDate().compareTo(today()) >= 0) {
            orderList.addAll(todayList);
        }


        //新建返回全局vo
        RealWeightAndLossesWeightVO realWeightAndLossesWeightVO = new RealWeightAndLossesWeightVO();

        LineChartVO planWeightlineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> planWeightvalues = new ArrayList<>();

        LineChartVO realWeightlineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> realWeightvalues = new ArrayList<>();

        LineChartVO lossesWeightlineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> lossesWeightvalues = new ArrayList<>();

        //获取查询时间段
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        List<String> dateList= DateUtils.dateZone(startQueryDate,endQueryDate);

        if (null==orderList || orderList.isEmpty()){
            realWeightAndLossesWeightVO.setPlanWeight(0d);
            for (String date: dateList) {
                LineChartVO.NameAndValueVO nameAndValueVO1=planWeightlineChartVO.new NameAndValueVO();
                nameAndValueVO1.setName(date);
                nameAndValueVO1.setValue(0);
                planWeightvalues.add(nameAndValueVO1);

                LineChartVO.NameAndValueVO realnameAndValueVO=realWeightlineChartVO.new NameAndValueVO();
                realnameAndValueVO.setName(date);
                realnameAndValueVO.setValue(0);
                realWeightvalues.add(realnameAndValueVO);

                LineChartVO.NameAndValueVO numnameAndValueVO=lossesWeightlineChartVO.new NameAndValueVO();
                numnameAndValueVO.setName(date);
                numnameAndValueVO.setValue(0);
                lossesWeightvalues.add(numnameAndValueVO);
            }
        }else{
            Map<String,ProductionManageProduceWeightDayStatistics> orderMap=new HashMap<>();
            for (ProductionManageProduceWeightDayStatistics order:orderList) {
                orderMap.put(order.getOperateDate(),order);
            }

            for (String date: dateList) {
                if (null!=orderMap.get(date)){
                    LineChartVO.NameAndValueVO nameAndValueVO1=planWeightlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO numNameAndValueVO2=realWeightlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO lossesWeightNameAndValueVO=lossesWeightlineChartVO.new NameAndValueVO();
                    ProductionManageProduceWeightDayStatistics order=orderMap.get(date);

                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(order.getPlanWeight());
                    planWeightvalues.add(nameAndValueVO1);

                    numNameAndValueVO2.setName(date);
                    numNameAndValueVO2.setValue(order.getRealWeight());
                    realWeightvalues.add(numNameAndValueVO2);

                    lossesWeightNameAndValueVO.setName(date);
                    lossesWeightNameAndValueVO.setValue(order.getLossesWeight());
                    lossesWeightvalues.add(lossesWeightNameAndValueVO);
                }else{
                    LineChartVO.NameAndValueVO nameAndValueVO1=planWeightlineChartVO.new NameAndValueVO();
                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(0);
                    planWeightvalues.add(nameAndValueVO1);

                    LineChartVO.NameAndValueVO realnameAndValueVO=realWeightlineChartVO.new NameAndValueVO();
                    realnameAndValueVO.setName(date);
                    realnameAndValueVO.setValue(0);
                    realWeightvalues.add(realnameAndValueVO);

                    LineChartVO.NameAndValueVO numnameAndValueVO=lossesWeightlineChartVO.new NameAndValueVO();
                    numnameAndValueVO.setName(date);
                    numnameAndValueVO.setValue(0);
                    lossesWeightvalues.add(numnameAndValueVO);
                }
            }
            //获取总的订单数
            Double totalPlanWeight=orderList.stream().filter(e->e.getPlanWeight()!=null).mapToDouble(ProductionManageProduceWeightDayStatistics::getPlanWeight).sum();
            realWeightAndLossesWeightVO.setPlanWeight(NumberUtil.retainTwoDecimal(totalPlanWeight.toString()));

            Double realWeight=orderList.stream().filter(e->e.getRealWeight()!=null).mapToDouble(ProductionManageProduceWeightDayStatistics::getRealWeight).sum();
            realWeightAndLossesWeightVO.setRealWeight(NumberUtil.retainTwoDecimal(realWeight.toString()));

            Double lossesWeight=orderList.stream().filter(e->e.getLossesWeight()!=null).mapToDouble(ProductionManageProduceWeightDayStatistics::getLossesWeight).sum();
            realWeightAndLossesWeightVO.setLossesWeight(NumberUtil.retainTwoDecimal(lossesWeight.toString()));

        }

        planWeightlineChartVO.setValues(planWeightvalues);
        realWeightlineChartVO.setValues(realWeightvalues);
        lossesWeightlineChartVO.setValues(lossesWeightvalues);

        LineChartVO.Option optionAmount=planWeightlineChartVO.new Option();
        optionAmount.setName("计划产量");
        planWeightlineChartVO.setOption(optionAmount);

        LineChartVO.Option optionRealAmount=realWeightlineChartVO.new Option();
        optionRealAmount.setName("产量");
        realWeightlineChartVO.setOption(optionRealAmount);

        LineChartVO.Option optionNum=lossesWeightlineChartVO.new Option();
        optionNum.setName("采收报损");
        lossesWeightlineChartVO.setOption(optionNum);

        List<LineChartVO> values=new ArrayList<>();
        values.add(planWeightlineChartVO);
        values.add(realWeightlineChartVO);
        //values.add(lossesWeightlineChartVO);

        realWeightAndLossesWeightVO.setValues(values);

        return realWeightAndLossesWeightVO;
    }

    public AbstractPageService.PageResults<List<ProductionManageProduceWeightDayStatisticsVO>> listRealWeightAndLossesWeight(SaleUserLineChartDTO requestDTO) throws Exception {
        QueryWrapper<ProductionManageProduceWeightDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageProduceWeightDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageProduceWeightDayStatistics.COL_OPERATE_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageProduceWeightDayStatistics.COL_OPERATE_DATE, requestDTO.getEndQueryDate())
                .in(StringUtils.isNotBlank(requestDTO.getIds()), ProductionManageProduceWeightDayStatistics.COL_ID, requestDTO.getIdList());
        queryWrapper.and(wrapper -> wrapper.gt(ProductionManageProduceWeightDayStatistics.COL_PLAN_WEIGHT, 0).or()
                .gt(ProductionManageProduceWeightDayStatistics.COL_REAL_WEIGHT, 0));

        queryWrapper.orderByAsc(ProductionManageProduceWeightDayStatistics.COL_OPERATE_DATE);

        Page<ProductionManageProduceWeightDayStatistics> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        IPage<ProductionManageProduceWeightDayStatistics> iPage = produceWeightDayStatisticsMapper.selectPage(page, queryWrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageProduceWeightDayStatistics> list = iPage.getRecords();

        List<ProductionManageProduceWeightDayStatistics> todayList = selectTodayList();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDate.now().format(fmt);
        if(CollectionUtils.isNotEmpty(todayList) && requestDTO.getEndQueryDate().compareTo(today)>=0){
            int realTotal=pagination.getTotal()+todayList.size();
            if((iPage.getCurrent()==iPage.getPages() && iPage.getTotal()%iPage.getSize()!=0)
                    || (list.size()==0 && requestDTO.getEndQueryDate().compareTo(today())>=0)){
                list.addAll(todayList);
            }

            pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(pagination.getPageSize(), (int) iPage.getCurrent(), realTotal);
        }

        AbstractPageService.PageResults<List<ProductionManageProduceWeightDayStatisticsVO>> pageResults = new AbstractPageService.PageResults<>();
        pageResults.setPagination(pagination);

        List<ProductionManageProduceWeightDayStatisticsVO> orderDayStatisticsVOList=  objectConverter.convert(list, ProductionManageProduceWeightDayStatisticsVO.class);
        pageResults.setList(orderDayStatisticsVOList);

        return pageResults;
    }

    public void exportRealWeightAndLossesWeight(SaleUserLineChartDTO requestDTO, HttpServletResponse response) throws Exception {
        List<ProductionManageProduceWeightDayStatisticsVO> list=null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(requestDTO.getDataList())){
            list = JSONObject.parseArray(requestDTO.getDataList(),ProductionManageProduceWeightDayStatisticsVO.class);
        }else {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            AbstractPageService.PageResults<List<ProductionManageProduceWeightDayStatisticsVO>> pageResults= listRealWeightAndLossesWeight(requestDTO);
            list= pageResults.getList();
        }
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "生产总数据", response);
    }

    public void select(ProductionYieldDataRequestDTO requestDTO){
        QueryWrapper<ProductionManageStatisticsProductionYieldData> wrapper=productionYieldDataMapper.getSqlWrapper(requestDTO, commonUtil.getSysId(), commonUtil.getOrganizationId());

        productionYieldDataMapper.selectList(wrapper);
    }
}
