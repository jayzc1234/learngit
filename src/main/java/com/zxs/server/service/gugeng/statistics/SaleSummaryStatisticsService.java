package com.zxs.server.service.gugeng.statistics;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSaleOrderDayStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSaleOrderDayStatisticsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleOrderDayStatisticsVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SaleAndOrderNumVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleSummaryStatisticsService  extends BaseSummaryStatisticsService  {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductManageOrderMapper orderdao;

    @Autowired
    private ProductionManageSaleOrderDayStatisticsMapper saleOrderDayStatisticsMapper;

    @Autowired
    private ObjectConverter<ProductionManageSaleOrderDayStatistics, ProductionManageSaleOrderDayStatisticsVO> objectConverter;


    public RestResult<SaleAndOrderNumVO> amountAndNum(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        RestResult restResult = new RestResult();

        QueryWrapper<ProductionManageSaleOrderDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleOrderDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageSaleOrderDayStatistics.COL_ORDER_DATE, dateIntervalDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageSaleOrderDayStatistics.COL_ORDER_DATE, dateIntervalDTO.getEndQueryDate());

        List<ProductionManageSaleOrderDayStatistics> orderList = saleOrderDayStatisticsMapper.selectList(queryWrapper);
        List<ProductionManageSaleOrderDayStatistics> todayList = selectTodayList();

        if (dateIntervalDTO.getEndQueryDate().compareTo(today()) >= 0) {
            orderList.addAll(todayList);
        }


        //新建返回全局vo
        SaleAndOrderNumVO saleAndOrderNumVO = new SaleAndOrderNumVO();
        //销售金额曲线数据
        LineChartVO saleAmountlineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleAmountvalues = new ArrayList<>();

        //实收金额曲线数据
        LineChartVO saleRealAmountlineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleRealAmountvalues=new ArrayList<>();

        //订单数曲线数据
        LineChartVO saleNumlineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleNumvalues=new ArrayList<>();

        LineChartVO paybackRateChatVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> paybackRatevalues=new ArrayList<>();

        //获取查询时间段
        String startQueryDate=dateIntervalDTO.getStartQueryDate();
        String endQueryDate=dateIntervalDTO.getEndQueryDate();
        List<String> dateList= DateUtils.dateZone(startQueryDate,endQueryDate);

        //总的销售金额
        BigDecimal allbigDecimal=new BigDecimal(0);
        //总的销售实收金额
        BigDecimal realAllbigDecimal=new BigDecimal(0);

        if (null==orderList || orderList.isEmpty()){
            saleAndOrderNumVO.setOrderNum(0);
            for (String date: dateList) {
                //订单金额
                LineChartVO.NameAndValueVO nameAndValueVO1=saleAmountlineChartVO.new NameAndValueVO();
                nameAndValueVO1.setName(date);
                nameAndValueVO1.setValue(0);
                saleAmountvalues.add(nameAndValueVO1);

                //实收金额
                LineChartVO.NameAndValueVO realnameAndValueVO=saleRealAmountlineChartVO.new NameAndValueVO();
                realnameAndValueVO.setName(date);
                realnameAndValueVO.setValue(0);
                saleRealAmountvalues.add(realnameAndValueVO);

                //订单数
                LineChartVO.NameAndValueVO numnameAndValueVO=saleNumlineChartVO.new NameAndValueVO();
                numnameAndValueVO.setName(date);
                numnameAndValueVO.setValue(0);
                saleNumvalues.add(numnameAndValueVO);

                LineChartVO.NameAndValueVO paybackRateVO=paybackRateChatVO.new NameAndValueVO();
                paybackRateVO.setName(date);
                paybackRateVO.setValue(0);
                paybackRatevalues.add(paybackRateVO);
            }
        }else{
            Map<String,ProductionManageSaleOrderDayStatistics> orderMap=new HashMap<>();
            for (ProductionManageSaleOrderDayStatistics order:orderList) {
                orderMap.put(order.getOrderDate(),order);
            }

            for (String date: dateList) {
                if (null!=orderMap.get(date)){
                    LineChartVO.NameAndValueVO nameAndValueVO1=saleAmountlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO numNameAndValueVO2=saleNumlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO realAmountNameAndValueVO=saleNumlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO paybackRateVO=paybackRateChatVO.new NameAndValueVO();
                    ProductionManageSaleOrderDayStatistics order=orderMap.get(date);

                    //订单金额
                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(order.getOrderMoney());
                    saleAmountvalues.add(nameAndValueVO1);

                    //订单数
                    realAmountNameAndValueVO.setName(date);
                    realAmountNameAndValueVO.setValue(order.getReceivedOrderMoney());
                    saleRealAmountvalues.add(realAmountNameAndValueVO);

                    //订单数
                    numNameAndValueVO2.setName(date);
                    numNameAndValueVO2.setValue(order.getOrderNum());
                    saleNumvalues.add(numNameAndValueVO2);

                    paybackRateVO.setName(date);
                    paybackRateVO.setValue(order.getPaybackRate());
                    paybackRatevalues.add(paybackRateVO);
                }else{
                    LineChartVO.NameAndValueVO nameAndValueVO=saleAmountlineChartVO.new NameAndValueVO();
                    nameAndValueVO.setName(date);
                    nameAndValueVO.setValue(0);

                    LineChartVO.NameAndValueVO nameAndValueVO1=saleAmountlineChartVO.new NameAndValueVO();
                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(0);

                    LineChartVO.NameAndValueVO nameAndValueVO2=saleAmountlineChartVO.new NameAndValueVO();
                    nameAndValueVO2.setName(date);
                    nameAndValueVO2.setValue(0);

                    LineChartVO.NameAndValueVO paybackRateVO=paybackRateChatVO.new NameAndValueVO();
                    paybackRateVO.setName(date);
                    paybackRateVO.setValue(0);
                    paybackRatevalues.add(paybackRateVO);

                    saleNumvalues.add(nameAndValueVO);
                    saleAmountvalues.add(nameAndValueVO1);
                    saleRealAmountvalues.add(nameAndValueVO2);
                }
            }
            //获取总的订单数
            Double totalOrderNum=orderList.stream().mapToDouble(ProductionManageSaleOrderDayStatistics::getOrderNum).sum();
            saleAndOrderNumVO.setOrderNum(totalOrderNum.intValue());

            Double orderMoney=orderList.stream().mapToDouble(ProductionManageSaleOrderDayStatistics::getOrderMoney).sum();
            saleAndOrderNumVO.setOrderMoney(NumberUtil.retainTwoDecimal(orderMoney));

            Double receivedOrderMoney=orderList.stream().mapToDouble(ProductionManageSaleOrderDayStatistics::getReceivedOrderMoney).sum();
            saleAndOrderNumVO.setRealOrderMoney(receivedOrderMoney.toString());

            String paybackRate=null;
            if(orderMoney.intValue()!=0){
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMaximumFractionDigits(2);
                paybackRate =nf.format( ( receivedOrderMoney * 100 ) / orderMoney );
            }
            saleAndOrderNumVO.setPaybackRate(paybackRate);

        }
        saleAmountlineChartVO.setValues(saleAmountvalues);
        saleNumlineChartVO.setValues(saleNumvalues);
        saleRealAmountlineChartVO.setValues(saleRealAmountvalues);
        paybackRateChatVO.setValues(paybackRatevalues);

        LineChartVO.Option optionAmount=saleAmountlineChartVO.new Option();
        optionAmount.setName("销售额");
        saleAmountlineChartVO.setOption(optionAmount);

        LineChartVO.Option optionRealAmount=saleAmountlineChartVO.new Option();
        optionRealAmount.setName("实收金额");
        saleRealAmountlineChartVO.setOption(optionRealAmount);

        LineChartVO.Option optionNum=saleAmountlineChartVO.new Option();
        optionNum.setName("订单数");
        saleNumlineChartVO.setOption(optionNum);

        LineChartVO.Option optionPaybackRate = paybackRateChatVO.new Option();
        optionPaybackRate.setName("回款率");
        paybackRateChatVO.setOption(optionPaybackRate);

        List<LineChartVO> values=new ArrayList<>();
        values.add(saleAmountlineChartVO);
        values.add(saleNumlineChartVO);
        //values.add(saleRealAmountlineChartVO);
        //values.add(paybackRateChatVO);
        saleAndOrderNumVO.setValues(values);
        restResult.setState(200);
        restResult.setResults(saleAndOrderNumVO);
        return restResult;
    }

    public AbstractPageService.PageResults<List<ProductionManageSaleOrderDayStatisticsVO>> listAmountAndNum(SaleUserLineChartDTO requestDTO) {
        QueryWrapper<ProductionManageSaleOrderDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageSaleOrderDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageSaleOrderDayStatistics.COL_ORDER_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageSaleOrderDayStatistics.COL_ORDER_DATE, requestDTO.getEndQueryDate())
                .in(StringUtils.isNotBlank(requestDTO.getIds()), ProductionManageSaleOrderDayStatistics.COL_ID, requestDTO.getIdList());
        queryWrapper.orderByAsc(ProductionManageSaleOrderDayStatistics.COL_ORDER_DATE);

        Page<ProductionManageSaleOrderDayStatistics> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        IPage<ProductionManageSaleOrderDayStatistics> iPage = saleOrderDayStatisticsMapper.selectPage(page, queryWrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageSaleOrderDayStatistics> list = iPage.getRecords();

        List<ProductionManageSaleOrderDayStatistics> todayList = selectTodayList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String today = LocalDate.now().format(fmt);
        if (CollectionUtils.isNotEmpty(todayList) && requestDTO.getEndQueryDate().compareTo(today) >= 0) {
            int realTotal = pagination.getTotal() + todayList.size();
            if ((iPage.getCurrent() == iPage.getPages() && iPage.getTotal() % iPage.getSize() != 0)
                    || (list.size() == 0 && requestDTO.getEndQueryDate().compareTo(today()) >= 0)) {
                list.addAll(todayList);
            }

            pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(pagination.getPageSize(), (int) iPage.getCurrent(), realTotal);
        }

/*            if(iPage.getCurrent()== iPage.getPages()){
                if(iPage.getTotal()%iPage.getPages()!=0){
                    long mergeSize= iPage.getSize()- iPage.getTotal()%iPage.getSize();
                    List<ProductionManageSaleOrderDayStatistics> mergeList= todayList.stream().limit(mergeSize).collect(Collectors.toList());
                    list.addAll(mergeList);
                }
            } else if(iPage.getCurrent()>= iPage.getPages()){
                long mergeSize= iPage.getSize()- iPage.getTotal()%iPage.getSize();
                long skipSize= (iPage.getCurrent() -1)*iPage.getSize() - iPage.getTotal();
                List<ProductionManageSaleOrderDayStatistics> mergeList= todayList.stream().skip(skipSize).collect(Collectors.toList());
                list.addAll(mergeList);
            }*/

        AbstractPageService.PageResults<List<ProductionManageSaleOrderDayStatisticsVO>> pageResults = new AbstractPageService.PageResults<>();
        pageResults.setPagination(pagination);

        List<ProductionManageSaleOrderDayStatisticsVO> orderDayStatisticsVOList=  objectConverter.convert(list, ProductionManageSaleOrderDayStatisticsVO.class);
        pageResults.setList(orderDayStatisticsVOList);

        return pageResults;
    }

    public void exportOrderAmountAndNum(SaleUserLineChartDTO requestDTO, HttpServletResponse response) throws Exception {
        List<ProductionManageSaleOrderDayStatisticsVO> list=null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(requestDTO.getDataList())){
            list = JSONObject.parseArray(requestDTO.getDataList(),ProductionManageSaleOrderDayStatisticsVO.class);
        } else {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            AbstractPageService.PageResults<List<ProductionManageSaleOrderDayStatisticsVO>> pageResults= listAmountAndNum(requestDTO);
            list= pageResults.getList();
        }
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "销售总数据", response);
    }

    public List<ProductionManageSaleOrderDayStatistics> selectTodayList(){
        SaleUserLineChartDTO dateIntervalDTO=new SaleUserLineChartDTO();
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = LocalDate.now().format(fmt);
        String end= LocalDate.now().plusDays(1).format(fmt);
        dateIntervalDTO.setStartQueryDate(start);
        dateIntervalDTO.setEndQueryDate(end);

        List<Map<String,Object>> orderList=orderdao.amountAndNum(dateIntervalDTO);

        List<ProductionManageSaleOrderDayStatistics> saleOrderDayStatisticsList=new ArrayList<>();
        if(CollectionUtils.isNotEmpty(orderList)){
            for(Map<String,Object> order: orderList){
                ProductionManageSaleOrderDayStatistics saleOrderDayStatistics=new ProductionManageSaleOrderDayStatistics();

                if(order.get("orderDate")==null) {
                    continue;
                }

                String orderDate= order.get("orderDate").toString();
                saleOrderDayStatistics.setOrderDate(orderDate);

                Object receivedOrderMoney=order.get("receivedOrderMoney");
                saleOrderDayStatistics.setReceivedOrderMoney(receivedOrderMoney==null?0d: Double.parseDouble(receivedOrderMoney.toString()));

                Object orderMoney=order.get("orderMoney");
                saleOrderDayStatistics.setOrderMoney(orderMoney==null?0d:Double.parseDouble(orderMoney.toString()));

                Object orderNum=order.get("orderNum");
                saleOrderDayStatistics.setOrderNum(orderNum==null?0:Integer.parseInt(orderNum.toString()));

                String paybackRate=null;
                if(saleOrderDayStatistics.getOrderMoney()!=0){
                    paybackRate = NumberUtil.retainTwoDecimal( ( saleOrderDayStatistics.getReceivedOrderMoney() * 100 ) / saleOrderDayStatistics.getOrderMoney() );
                }
                saleOrderDayStatistics.setPaybackRate(paybackRate);

                saleOrderDayStatisticsList.add(saleOrderDayStatistics);
            }
        }

        return saleOrderDayStatisticsList;
    }

    @Transactional(rollbackFor = Exception.class)
    public void statisticsSaleOrder(){
        SaleUserLineChartDTO dateIntervalDTO=new SaleUserLineChartDTO();
        setAccountInfo(dateIntervalDTO);

        List<Map<String,Object>> orderList=orderdao.amountAndNum(dateIntervalDTO);
        if(CollectionUtils.isNotEmpty(orderList)){
            QueryWrapper<ProductionManageSaleOrderDayStatistics> wrapper= queryTemplate();
            saleOrderDayStatisticsMapper.delete(wrapper);
            List<ProductionManageSaleOrderDayStatistics> saleOrderDayStatisticsList=new ArrayList<>();
            for(Map<String,Object> order: orderList){
                ProductionManageSaleOrderDayStatistics saleOrderDayStatistics=new ProductionManageSaleOrderDayStatistics();
                saleOrderDayStatistics.setOrganizationId(superToken.getOrganizationId());
                saleOrderDayStatistics.setSysId(superToken.getSysId());

                if(order.get("orderDate")==null) {
                    continue;
                }

                String orderDate= order.get("orderDate").toString();
                saleOrderDayStatistics.setOrderDate(orderDate);

                Object receivedOrderMoney=order.get("receivedOrderMoney");
                saleOrderDayStatistics.setReceivedOrderMoney(receivedOrderMoney==null?0d: Double.parseDouble(receivedOrderMoney.toString()));

                Object orderMoney=order.get("orderMoney");
                saleOrderDayStatistics.setOrderMoney(orderMoney==null?0d:Double.parseDouble(orderMoney.toString()));

                Object orderNum=order.get("orderNum");
                saleOrderDayStatistics.setOrderNum(orderNum==null?0:Integer.parseInt(orderNum.toString()));

                String paybackRate=null;
                if(saleOrderDayStatistics.getOrderMoney()!=0){
                    paybackRate = NumberUtil.retainTwoDecimal( ( saleOrderDayStatistics.getReceivedOrderMoney() * 100 ) / saleOrderDayStatistics.getOrderMoney() );
                }
                saleOrderDayStatistics.setPaybackRate(paybackRate);

                saleOrderDayStatisticsMapper.insert(saleOrderDayStatistics);
                //saleOrderDayStatisticsList.add(saleOrderDayStatistics);
            }
        }
    }

}
