package com.zxs.server.service.gugeng.statistics;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.ProductionManageOrderProductDataStatisticsDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleProductChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderDataByType;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleOrderStatusDayStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductionManageSaleTaskService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ClientDataCurveLineVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.CustomerNumResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


@Service
public class SaleOverviewStatisticsService  extends BaseSummaryStatisticsService  {

    @Autowired
    private ProductionManageSalesGoalsMonthStatisticsMapper salesGoalsMonthStatisticsMapper;

    @Autowired
    private ProductionManageSaleProductDayStatisticsMapper saleProductDayStatisticsMapper;

    @Autowired
    private ProductionManageSaleOrderStatusDayStatisticsMapper saleOrderStatusDayStatisticsMapper;

    @Autowired
    private ProductionManageCustomerNumDayStatisticsMapper customerNumDayStatisticsMapper;

    @Autowired
    private ProductionManageSaleTaskService saleTaskService;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private ProductionManageOrderDataByTypeMapper orderDataByTypeMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageClientDataStatisticsService service;

    @Autowired
    private ProductionManageOrderProductDataStatisticsService orderProductDataStatisticsService;

    public Map<String,List<NameValueVO>> selectSaleProduct(SaleProductChartDTO dto) throws Exception {
        String[] dateInterval =dto.getQueryDate().split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        String startTime = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endTime = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        ProductionManageOrderProductDataStatisticsDTO statisticsDTO=new ProductionManageOrderProductDataStatisticsDTO();
        statisticsDTO.setStartQueryDate(startTime);
        statisticsDTO.setEndQueryDate(endTime);
        statisticsDTO.setProductSortId(dto.getProductSortId());
        statisticsDTO.setType(dto.getType()+1);
        Map<String,List<NameValueVO>> dataStatistics = orderProductDataStatisticsService.pie(statisticsDTO);

        // 过滤掉百分比为0的数据集
        dataStatistics.forEach((k, v) -> {
            List<NameValueVO> nameValueVOS = dataStatistics.get(k);
            Iterator<NameValueVO> iterator = nameValueVOS.iterator();
            while (iterator.hasNext()) {
                NameValueVO vo = iterator.next();
                BigDecimal value = (BigDecimal) vo.getValue();
                if (value.signum() != 1) {
                    iterator.remove();
                }
            }
        });

        return dataStatistics;
    }

    /*public List<LineChartVO> selectSaleProduct2(SaleProductChartDTO dto) throws Exception {
        String[] dateInterval = dto.getQueryDate().split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> dateList= DateUtils.dateZone(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        QueryWrapper<ProductionManageSaleProductDayStatistics> wrapper= queryTemplate();
        wrapper.ge("OrderDate", startDate)
                .le("OrderDate", endDate);
        wrapper.groupBy("productId");
        wrapper.select("sum(orderMoney) orderMoney, sum(receivedOrderMoney) receivedOrderMoney, productName");
        List<ProductionManageSaleProductDayStatistics> goalsMonthStatistics= saleProductDayStatisticsMapper.selectList(wrapper);

        LineChartVO chart0=createLineChartVO("销售额");
        LineChartVO chart1=createLineChartVO("实收额");
        List<LineChartVO> values= new ArrayList<>();
        if(dto.getType()==1){
            values.add(chart1);
        } else {
            values.add(chart0);
        }

        if(CollectionUtils.isEmpty(goalsMonthStatistics)){
            chart0.getValues().add(createNameAndValueVO(chart0, "全部", 0));
            chart1.getValues().add(createNameAndValueVO(chart0, "全部", 0));
        } else {
            for(ProductionManageSaleProductDayStatistics statistics: goalsMonthStatistics){
                chart0.getValues().add(createNameAndValueVO(chart0, statistics.getProductName(),  statistics.getOrderMoney()));
                chart1.getValues().add(createNameAndValueVO(chart0, statistics.getProductName(),  statistics.getReceivedOrderMoney()));
            }
        }

        return values;
    }*/

    public CustomerNumResponseVO selectCustomerNumList(String queryDate) throws Exception {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        String startTime = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endTime = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        DateIntervalDTO dateIntervalDTO=new DateIntervalDTO();
        dateIntervalDTO.setStartQueryDate(startTime);
        dateIntervalDTO.setEndQueryDate(endTime);

        ClientDataCurveLineVO orderClientNum= service.orderClientNum(dateIntervalDTO);
        ClientDataCurveLineVO potentialClientNum= service.potentialClientNum(dateIntervalDTO);

        List<LineChartVO> chartVOS= new ArrayList<>();
        chartVOS.addAll(orderClientNum.getValues());
        chartVOS.addAll(potentialClientNum.getValues());

        CustomerNumResponseVO responseVO=new CustomerNumResponseVO();
        responseVO.setOrderCustomerNum(orderClientNum.getClientTotalNum());
        responseVO.setPotentialCustomerNum(potentialClientNum.getClientTotalNum());
        responseVO.setValues(chartVOS);

        return responseVO;
    }

    /*public List<LineChartVO> selectCustomerNumList(String queryDate) throws Exception {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> dateList= DateUtils.dateZone(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        QueryWrapper<ProductionManageCustomerNumDayStatistics> wrapper= queryTemplate();
        wrapper.ge("createDate", startDate)
                .le("createDate", endDate);
        List<ProductionManageCustomerNumDayStatistics> goalsMonthStatistics= customerNumDayStatisticsMapper.selectList(wrapper);

        LineChartVO chart0=createLineChartVO("潜在客户");
        LineChartVO chart1=createLineChartVO("订单客户");

        List<LineChartVO> values= Arrays.asList(chart0, chart1);

        if(CollectionUtils.isEmpty(goalsMonthStatistics)){
            for (String date: dateList){
                chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
            }
        } else {
            for (String date: dateList){
                ProductionManageCustomerNumDayStatistics status0=  goalsMonthStatistics.stream().filter(e->e.getCreateDate().equals(date) && e.getClientType().equals(0)).findFirst().orElse(null);
                if(status0!=null){
                    chart0.getValues().add(createNameAndValueVO(chart0, date, status0.getCustomerNum()));
                } else {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageCustomerNumDayStatistics status1=  goalsMonthStatistics.stream().filter(e->e.getCreateDate().equals(date) && e.getClientType().equals(1)).findFirst().orElse(null);
                if(status1!=null){
                    chart1.getValues().add(createNameAndValueVO(chart0, date, status1.getCustomerNum()));
                } else {
                    chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                }
            }
        }

        return values;
    }*/

    public List<LineChartVO> selectSaleOrderList(String queryDate) throws Exception {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
        String startTime = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endTime = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<String> dateList = DateUtils.dateZone(startTime, endTime);

        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderDataByType.class);
        queryWrapper.ge(StringUtils.isNotBlank(startTime), ProductionManageOrderDataByType.COL_ORDER_DATE, startTime);
        queryWrapper.le(StringUtils.isNotBlank(endTime), ProductionManageOrderDataByType.COL_ORDER_DATE, endTime);
        queryWrapper.groupBy(ProductionManageOrderDataByType.COL_ORDER_DATE + ", " + ProductionManageOrderDataByType.COL_TYPE_VALUE);
        queryWrapper.select(" date_format(" + ProductionManageOrderDataByType.COL_ORDER_DATE + ",'%Y-%m-%d') orderDate, " + ProductionManageOrderDataByType.COL_ORDER_NUM + " , " + ProductionManageOrderDataByType.COL_TYPE_VALUE + " orderStatus ");
        List<Map<String, Object>> orderMapList = orderDataByTypeMapper.selectMaps(queryWrapper);

        List<ProductionManageSaleOrderStatusDayStatistics> goalsMonthStatistics = JSON.parseArray(JSON.toJSONString(orderMapList), ProductionManageSaleOrderStatusDayStatistics.class);

        LineChartVO chart0 = createLineChartVO("待审核");
        LineChartVO chart1 = createLineChartVO("待发货");
        LineChartVO chart2 = createLineChartVO("待收货");
        LineChartVO chart3 = createLineChartVO("已完成");

        List<LineChartVO> values = Arrays.asList(//chart0,
                chart1, chart2, chart3);

        if (CollectionUtils.isEmpty(goalsMonthStatistics)) {
            for (String date : dateList) {
                chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart3.getValues().add(createNameAndValueVO(chart0, date, 0));
            }
        } else {
            for (String date : dateList) {
                ProductionManageSaleOrderStatusDayStatistics status0 = goalsMonthStatistics.stream().filter(e -> e.getOrderDate().equals(date) && e.getOrderStatus().equals(0)).findFirst().orElse(null);
                if (status0 != null) {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, status0.getOrderNum()));
                } else {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status1=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(2)).findFirst().orElse(null);
                if(status1!=null){
                    chart1.getValues().add(createNameAndValueVO(chart0, date, status1.getOrderNum()));
                } else {
                    chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status2=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(3)).findFirst().orElse(null);
                if(status2!=null){
                    chart2.getValues().add(createNameAndValueVO(chart0, date, status2.getOrderNum()));
                } else {
                    chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status3=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(4)).findFirst().orElse(null);
                if(status3!=null){
                    chart3.getValues().add(createNameAndValueVO(chart0, date, status3.getOrderNum()));
                } else {
                    chart3.getValues().add(createNameAndValueVO(chart0, date, 0));
                }
            }
        }

        return values;
    }

    /*public List<LineChartVO> selectSaleOrderList2(String queryDate) throws Exception {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> dateList= DateUtils.dateZone(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        QueryWrapper<ProductionManageSaleOrderStatusDayStatistics> wrapper= queryTemplate();
        wrapper.ge("orderDate", startDate)
                .le("orderDate", endDate);
        List<ProductionManageSaleOrderStatusDayStatistics> goalsMonthStatistics= saleOrderStatusDayStatisticsMapper.selectList(wrapper);

        LineChartVO chart0=createLineChartVO("待审核");
        LineChartVO chart1=createLineChartVO("待发货");
        LineChartVO chart2=createLineChartVO("待收货");
        LineChartVO chart3=createLineChartVO("已完成");

        List<LineChartVO> values= Arrays.asList(chart0, chart1, chart2, chart3);

        if(CollectionUtils.isEmpty(goalsMonthStatistics)){
            for (String date: dateList){
                chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart3.getValues().add(createNameAndValueVO(chart0, date, 0));
            }
        } else {
            for (String date: dateList){
                ProductionManageSaleOrderStatusDayStatistics status0=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(0)).findFirst().orElse(null);
                if(status0!=null){
                    chart0.getValues().add(createNameAndValueVO(chart0, date, status0.getOrderNum()));
                } else {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status1=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(2)).findFirst().orElse(null);
                if(status1!=null){
                    chart1.getValues().add(createNameAndValueVO(chart0, date, status1.getOrderNum()));
                } else {
                    chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status2=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(3)).findFirst().orElse(null);
                if(status2!=null){
                    chart2.getValues().add(createNameAndValueVO(chart0, date, status2.getOrderNum()));
                } else {
                    chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
                }

                ProductionManageSaleOrderStatusDayStatistics status3=  goalsMonthStatistics.stream().filter(e->e.getOrderDate().equals(date) && e.getOrderStatus().equals(4)).findFirst().orElse(null);
                if(status3!=null){
                    chart3.getValues().add(createNameAndValueVO(chart0, date, status3.getOrderNum()));
                } else {
                    chart3.getValues().add(createNameAndValueVO(chart0, date, 0));
                }
            }
        }

        return values;
    }*/

    /*public List<LineChartVO> selectSalesGoals(String queryDate){
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        List<String> dateList = new ArrayList<>();
        dateList.add(startDate.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        dateList.add(startDate.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        dateList.add(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));

        QueryWrapper<ProductionManageSalesGoalsMonthStatistics> wrapper= queryTemplate();
        wrapper.ge("saleDate", dateList.get(0))
            .le("saleDate", dateList.get(2));
        List<ProductionManageSalesGoalsMonthStatistics> goalsMonthStatistics= salesGoalsMonthStatisticsMapper.selectList(wrapper);

        LineChartVO chart0=createLineChartVO("目标销售额");
        LineChartVO chart1=createLineChartVO("实际销售额");
        LineChartVO chart2=createLineChartVO("目标达成率");

        List<LineChartVO> values= Arrays.asList(chart0, chart1, chart2);

        if(CollectionUtils.isEmpty(goalsMonthStatistics)){
            for (String date: dateList){
                chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
            }
        } else {
            Map<String, ProductionManageSalesGoalsMonthStatistics> listMap = goalsMonthStatistics.stream().collect(Collectors.toMap(ProductionManageSalesGoalsMonthStatistics::getSaleDate, a -> a, (k1,k2)->k1));
            for (String date: dateList){
                if(listMap.get(date)!=null){
                    ProductionManageSalesGoalsMonthStatistics statistics= listMap.get(date);

                    chart0.getValues().add(createNameAndValueVO(chart0, date, statistics.getTargetSaleAmount()));
                    chart1.getValues().add(createNameAndValueVO(chart0, date, statistics.getCompletedSaleAmount()));
                    chart2.getValues().add(createNameAndValueVO(chart0, date, statistics.getCompletionPercentage()));
                } else {
                    chart0.getValues().add(createNameAndValueVO(chart0, date, 0));
                    chart1.getValues().add(createNameAndValueVO(chart0, date, 0));
                    chart2.getValues().add(createNameAndValueVO(chart0, date, 0));
                }
            }
        }

        return values;
    }

    void statisticsSalesGoals() throws Exception {
        ProductManageSaleTaskRequestDTO taskRequestDTO=new ProductManageSaleTaskRequestDTO();
        List<DepartmentSaleTaskResponseVO> saleTaskResponseVOList= saleTaskService.listTasksForDepartment(taskRequestDTO);

        QueryWrapper<ProductionManageSalesGoalsMonthStatistics> wrapper= queryTemplate();
        salesGoalsMonthStatisticsMapper.delete(wrapper);

        for(DepartmentSaleTaskResponseVO saleTaskResponseVO: saleTaskResponseVOList){
            ProductionManageSalesGoalsMonthStatistics salesGoalsMonthStatistics=new ProductionManageSalesGoalsMonthStatistics();
            salesGoalsMonthStatistics.setOrganizationId(superToken.getOrganizationId());
            salesGoalsMonthStatistics.setSysId(superToken.getSysId());

            BeanUtils.copyProperties(saleTaskResponseVO, salesGoalsMonthStatistics);
            salesGoalsMonthStatisticsMapper.insert(salesGoalsMonthStatistics);
        }
    }*/

/*    void statisticsSaleProduct() {
        QueryWrapper<ProductionManageSaleProductDayStatistics> wrapper=queryTemplate();
        List<ProductionManageSaleProductDayStatistics> saleProductDayStatisticsList= orderMapper.selectSaleProduct(wrapper);
        saleProductDayStatisticsMapper.delete(wrapper);
        for(ProductionManageSaleProductDayStatistics saleProductDayStatistics : saleProductDayStatisticsList){
            saleProductDayStatistics.setOrganizationId(superToken.getOrganizationId());
            saleProductDayStatistics.setSysId(superToken.getSysId());

            saleProductDayStatisticsMapper.insert(saleProductDayStatistics);
        }
    }*/

/*    void statisticsSaleOrderList(){
        QueryWrapper<ProductionManageSaleOrderStatusDayStatistics> wrapper=queryTemplate();
        List<ProductionManageSaleOrderStatusDayStatistics> list= orderMapper.selectSaleOrderList(wrapper);
        saleOrderStatusDayStatisticsMapper.delete(wrapper);
        for(ProductionManageSaleOrderStatusDayStatistics orderStatusDayStatistics: list){
            orderStatusDayStatistics.setOrganizationId(superToken.getOrganizationId());
            orderStatusDayStatistics.setSysId(superToken.getSysId());

            saleOrderStatusDayStatisticsMapper.insert(orderStatusDayStatistics);
        }
    }*/

/*    void statisticsCustomerNum(){
        QueryWrapper<ProductionManageCustomerNumDayStatistics> wrapper=queryTemplate();
        List<ProductionManageCustomerNumDayStatistics> list= clientMapper.selectClientList(wrapper);
        customerNumDayStatisticsMapper.delete(wrapper);
        for(ProductionManageCustomerNumDayStatistics customerNumDayStatistics: list){
            customerNumDayStatistics.setOrganizationId(superToken.getOrganizationId());
            customerNumDayStatistics.setSysId(superToken.getSysId());

            customerNumDayStatisticsMapper.insert(customerNumDayStatistics);
        }
    }*/

    @Transactional(rollbackFor = Exception.class)
    public void statisticsSaleData() throws Exception{
        //statisticsSaleProduct();
    }


}
