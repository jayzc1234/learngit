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
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStorageStockDayStatistics;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStorageStockDayStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.InstorageAndOutboundWeightVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageStorageStockDayStatisticsVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WarehouseSummaryStatisticsService extends BaseSummaryStatisticsService {

    @Autowired
    private ProductionManageSortInstorageMapper sortInstorageMapper;

    @Autowired
    private ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private ProductionManageStockMapper stockMapper;

    @Autowired
    private ProductionManageStorageStockDayStatisticsMapper stockDayStatisticsMapper;

    @Autowired
    private ObjectConverter<ProductionManageStorageStockDayStatistics, ProductionManageStorageStockDayStatisticsVO> objectConverter;

    @Autowired
    private CommonUtil commonUtil;


    public InstorageAndOutboundWeightVO instorageAndOutboundWeight(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        RestResult restResult = new RestResult();

        QueryWrapper<ProductionManageStorageStockDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageStorageStockDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE, dateIntervalDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE, dateIntervalDTO.getEndQueryDate())
                .eq(StringUtils.isNotBlank(dateIntervalDTO.getProductId()), ProductionManageStorageStockDayStatistics.COL_PRODUCT_ID, dateIntervalDTO.getProductId());
        if (StringUtils.isEmpty(dateIntervalDTO.getProductId())) {
            queryWrapper.groupBy(ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE);
            queryWrapper.select(ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE + "," +
                    " SUM(" + ProductionManageStorageStockDayStatistics.COL_OUTBOUND_WEIGHT + ") outboundWeight, " +
                    "SUM(" + ProductionManageStorageStockDayStatistics.COL_INSTORAGE_WEIGHT + ") instorageWeight," +
                    "SUM(" + ProductionManageStorageStockDayStatistics.COL_STOCK_WEIGHT + ") stockWeight");
        }

        List<ProductionManageStorageStockDayStatistics> orderList = stockDayStatisticsMapper.selectList(queryWrapper);
        List<ProductionManageStorageStockDayStatistics> todayList = selectTodayList(dateIntervalDTO.getProductId());

        if (dateIntervalDTO.getEndQueryDate().compareTo(today()) >= 0) {
            orderList.addAll(todayList);
        }

        //新建返回全局vo
        InstorageAndOutboundWeightVO instorageAndOutboundVO = new InstorageAndOutboundWeightVO();

        LineChartVO instoragelineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> instoragevalues = new ArrayList<>();

        LineChartVO outboundlineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> outboundvalues = new ArrayList<>();

        LineChartVO stocklineChartVO = new LineChartVO();
        List<LineChartVO.NameAndValueVO> stockvalues = new ArrayList<>();

        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        List<String> dateList = DateUtils.dateZone(startQueryDate, endQueryDate);


        if (null==orderList || orderList.isEmpty()){
            instorageAndOutboundVO.setInstorageWeight(0d);
            for (String date: dateList) {
                LineChartVO.NameAndValueVO nameAndValueVO1=instoragelineChartVO.new NameAndValueVO();
                nameAndValueVO1.setName(date);
                nameAndValueVO1.setValue(0);
                instoragevalues.add(nameAndValueVO1);

                LineChartVO.NameAndValueVO realnameAndValueVO=instoragelineChartVO.new NameAndValueVO();
                realnameAndValueVO.setName(date);
                realnameAndValueVO.setValue(0);
                outboundvalues.add(realnameAndValueVO);

                LineChartVO.NameAndValueVO numnameAndValueVO=instoragelineChartVO.new NameAndValueVO();
                numnameAndValueVO.setName(date);
                numnameAndValueVO.setValue(0);
                stockvalues.add(numnameAndValueVO);
            }
        }else{
            Map<String,ProductionManageStorageStockDayStatistics> orderMap=new HashMap<>();
            for (ProductionManageStorageStockDayStatistics order:orderList) {
                orderMap.put(order.getOperateDate(),order);
            }

            for (String date: dateList) {
                if (null!=orderMap.get(date)){
                    LineChartVO.NameAndValueVO nameAndValueVO1=instoragelineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO numNameAndValueVO2=instoragelineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO realAmountNameAndValueVO=instoragelineChartVO.new NameAndValueVO();
                    ProductionManageStorageStockDayStatistics order=orderMap.get(date);

                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(order.getInstorageWeight());
                    instoragevalues.add(nameAndValueVO1);

                    realAmountNameAndValueVO.setName(date);
                    realAmountNameAndValueVO.setValue(order.getOutboundWeight());
                    outboundvalues.add(realAmountNameAndValueVO);

                    //订单数
                    numNameAndValueVO2.setName(date);
                    numNameAndValueVO2.setValue(order.getStockWeight() <0? 0: order.getStockWeight());
                    stockvalues.add(numNameAndValueVO2);
                }else{
                    LineChartVO.NameAndValueVO nameAndValueVO1=instoragelineChartVO.new NameAndValueVO();
                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(0);
                    instoragevalues.add(nameAndValueVO1);

                    LineChartVO.NameAndValueVO realnameAndValueVO=instoragelineChartVO.new NameAndValueVO();
                    realnameAndValueVO.setName(date);
                    realnameAndValueVO.setValue(0);
                    outboundvalues.add(realnameAndValueVO);

                    LineChartVO.NameAndValueVO numnameAndValueVO=instoragelineChartVO.new NameAndValueVO();
                    numnameAndValueVO.setName(date);
                    numnameAndValueVO.setValue(0);
                    stockvalues.add(numnameAndValueVO);
                }
            }
            Double instorageWeight=orderList.stream().filter(e->e.getInstorageWeight()!=null).mapToDouble(ProductionManageStorageStockDayStatistics::getInstorageWeight).sum();
            // 入库重量保留2位小数
            instorageWeight = BigDecimal.valueOf(instorageWeight).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
            instorageAndOutboundVO.setInstorageWeight(instorageWeight);

            Double outboundWeight=orderList.stream().filter(e->e.getOutboundWeight()!=null).mapToDouble(ProductionManageStorageStockDayStatistics::getOutboundWeight).sum();
            // 出库重量保留2位小数
            outboundWeight = BigDecimal.valueOf(outboundWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            instorageAndOutboundVO.setOutboundWeight(outboundWeight);

            Double stockWeight = orderList.stream().filter(e -> e.getStockWeight()!=null).mapToDouble(ProductionManageStorageStockDayStatistics::getStockWeight).sum();
            // 库存重量保留2位小数
            stockWeight = BigDecimal.valueOf(stockWeight).setScale(2,  BigDecimal.ROUND_HALF_UP).doubleValue();
            instorageAndOutboundVO.setStockWeight(NumberUtil.retainTwoDecimal(stockWeight.toString()));
        }
        instoragelineChartVO.setValues(instoragevalues);
        outboundlineChartVO.setValues(outboundvalues);
        stocklineChartVO.setValues(stockvalues);

        LineChartVO.Option optionAmount=instoragelineChartVO.new Option();
        optionAmount.setName("入库重量");
        instoragelineChartVO.setOption(optionAmount);

        LineChartVO.Option optionRealAmount=instoragelineChartVO.new Option();
        optionRealAmount.setName("出库重量");
        outboundlineChartVO.setOption(optionRealAmount);

        LineChartVO.Option optionNum=instoragelineChartVO.new Option();
        optionNum.setName("库存重量");
        stocklineChartVO.setOption(optionNum);

        List<LineChartVO> values=new ArrayList<>();
        values.add(instoragelineChartVO);
        values.add(outboundlineChartVO);
        values.add(stocklineChartVO);
        instorageAndOutboundVO.setValues(values);

        return instorageAndOutboundVO;
    }

    public AbstractPageService.PageResults<List<ProductionManageStorageStockDayStatisticsVO>> listInstorageAndOutboundWeight(SaleUserLineChartDTO requestDTO) {
        QueryWrapper<ProductionManageStorageStockDayStatistics> queryWrapper = commonUtil.queryTemplate(ProductionManageStorageStockDayStatistics.class);
        queryWrapper.ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE, requestDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE, requestDTO.getEndQueryDate())
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStorageStockDayStatistics.COL_PRODUCT_ID, requestDTO.getProductId())
                .in(StringUtils.isNotBlank(requestDTO.getIds()), ProductionManageStorageStockDayStatistics.COL_ID, requestDTO.getIdList());
        if (StringUtils.isEmpty(requestDTO.getProductId())) {
            queryWrapper.groupBy(ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE);
            queryWrapper.select(ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE + ", " +
                    "SUM(" + ProductionManageStorageStockDayStatistics.COL_OUTBOUND_WEIGHT + ") outboundWeight, " +
                    "SUM(" + ProductionManageStorageStockDayStatistics.COL_INSTORAGE_WEIGHT + ") instorageWeight," +
                    "SUM(" + ProductionManageStorageStockDayStatistics.COL_STOCK_WEIGHT + ") stockWeight, GROUP_CONCAT(" + ProductionManageStorageStockDayStatistics.COL_ID + ") Id");
        }
        queryWrapper.orderByAsc(ProductionManageStorageStockDayStatistics.COL_OPERATE_DATE);

        Page<ProductionManageStorageStockDayStatistics> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        IPage<ProductionManageStorageStockDayStatistics> iPage = stockDayStatisticsMapper.selectPage(page, queryWrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<ProductionManageStorageStockDayStatistics> list = iPage.getRecords();

        List<ProductionManageStorageStockDayStatistics> todayList = selectTodayList(requestDTO.getProductId());
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

        AbstractPageService.PageResults<List<ProductionManageStorageStockDayStatisticsVO>> pageResults = new AbstractPageService.PageResults<>();
        pageResults.setPagination(pagination);

        List<ProductionManageStorageStockDayStatisticsVO> orderDayStatisticsVOList=  objectConverter.convert(list, ProductionManageStorageStockDayStatisticsVO.class);
        pageResults.setList(orderDayStatisticsVOList);
        if(StringUtils.isEmpty(requestDTO.getProductId())){
            if (CollectionUtils.isNotEmpty(orderDayStatisticsVOList)) {
                for (ProductionManageStorageStockDayStatisticsVO stockDayStatisticsVO : orderDayStatisticsVOList) {
                    stockDayStatisticsVO.setProductName("全部");
                }
            }
        }
        if (CollectionUtils.isNotEmpty(orderDayStatisticsVOList)) {
            for (ProductionManageStorageStockDayStatisticsVO stockDayStatisticsVO : orderDayStatisticsVOList) {
                if (stockDayStatisticsVO.getStockWeight() < 0) {
                    stockDayStatisticsVO.setStockWeight(0d);
                }
            }
        }

        return pageResults;
    }

    public void exportInstorageAndOutboundWeight(SaleUserLineChartDTO requestDTO, HttpServletResponse response)  throws Exception {
        List<ProductionManageStorageStockDayStatisticsVO> list=null;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(requestDTO.getDataList())){
            list = JSONObject.parseArray(requestDTO.getDataList(),ProductionManageStorageStockDayStatisticsVO.class);
        } else {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            AbstractPageService.PageResults<List<ProductionManageStorageStockDayStatisticsVO>> pageResults= listInstorageAndOutboundWeight(requestDTO);
            list= pageResults.getList();
        }
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "仓储总数据", response);
    }

    public List<ProductionManageStorageStockDayStatistics> selectTodayList(String productId){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = LocalDate.now().format(fmt);
        String end= LocalDate.now().plusDays(1).format(fmt);
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        if(StringUtils.isEmpty(productId)){
            return stockDayStatisticsMapper.selectTodayStockWeight2(organizationId, sysId, start, end);
        } else {
            return stockDayStatisticsMapper.selectTodayStockWeight2ByProductId(organizationId, sysId, start, end, productId);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void statisticsWarehouseData() throws SuperCodeException {
        List<ProductionManageStorageStockDayStatistics> stockDayStatistics= stockDayStatisticsMapper.selectStockWeight2(superToken.getOrganizationId(), superToken.getSysId());
        QueryWrapper<ProductionManageStorageStockDayStatistics> wrapper= queryTemplate();
        stockDayStatisticsMapper.delete(wrapper);
        for(ProductionManageStorageStockDayStatistics stockDayStatistics1: stockDayStatistics){
            stockDayStatistics1.setOrganizationId(superToken.getOrganizationId());
            stockDayStatistics1.setSysId(superToken.getSysId());
            if(stockDayStatistics1.getOutboundWeight()==null){
                stockDayStatistics1.setOutboundWeight(0d);
            }
            try{
                stockDayStatisticsMapper.insert(stockDayStatistics1);
            }catch (Exception e){
                e.printStackTrace();
            }

        }


/*        List<Map<String, Object>> inStorageWeightList= inStorageWeight(null, null);

        List<Map<String, Object>> outStorageWeightList=outStorageWeight(null, null);

        List<Map<String, Object>> stockWeightList=  stockWeight(null, null);

        List<String> inStorageDates= inStorageWeightList.stream().map(e->String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        List<String> outStorageDates= outStorageWeightList.stream().map(e->String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        List<String> stockWeightDates= stockWeightList.stream().filter(e->e!=null && e.get("dateTime")!=null).map(e->String.valueOf(e.get("dateTime"))).collect(Collectors.toList());
        inStorageDates.addAll(outStorageDates);
        inStorageDates.addAll(stockWeightDates);

        inStorageDates= inStorageDates.stream().distinct().collect(Collectors.toList());*/


    }

    /*private List<Map<String, Object>> stockWeight(String startQueryDate, String endQueryDate)  throws SuperCodeException  {
        QueryWrapper<ProductionManageStock> sortInstorageQueryWrapper=new QueryWrapper();
        List<Map<String, Object>> sortingLossList=singleFieldSumSelect("Weight","CreateDate",startQueryDate,endQueryDate,"production_manage_stock",true,stockMapper,sortInstorageQueryWrapper,null,null);
        return sortingLossList;
    }

    private  List<Map<String, Object>> outStorageWeight(String startQueryDate, String endQueryDate) throws SuperCodeException {
        //1正常出库
        QueryWrapper<ProductionManageOutbound> outboundQueryWrapper=new QueryWrapper();
        List<Map<String, Object>> outboundList=singleFieldSumSelect("PackingWeight","OutboundDate",startQueryDate,endQueryDate,"t_production_manage_outbound",true,outboundMapper,outboundQueryWrapper,null,null);
        //2盘点出库
        QueryWrapper<ProductionManageStockCheck> stockCheckQueryWrapper=new QueryWrapper();
        stockCheckQueryWrapper.lt("StockSubOrAddWeight",0);
        List<Map<String, Object>> stockCheckList=singleFieldSumSelect("-StockSubOrAddWeight","InventoryDate",startQueryDate,endQueryDate,"production_manage_stock_check",true,stockCheckMapper,stockCheckQueryWrapper,null,null);
        return listMapAdd(outboundList,stockCheckList);
    }

    private List<Map<String, Object>> inStorageWeight(String startQueryDate, String endQueryDate) throws SuperCodeException {
        //1分拣入库
        QueryWrapper<ProductionManageSortInstorage> sortInstorageQueryWrapper=new QueryWrapper();
        List<Map<String, Object>> sortingLossList=singleFieldSumSelect("Weight","CreateDate",startQueryDate,endQueryDate,"production_manage_sort_instorage",true,sortInstorageMapper,sortInstorageQueryWrapper,null,null);
        //2盘点入库
        QueryWrapper<ProductionManageStockCheck> stockCheckQueryWrapper=new QueryWrapper();
        stockCheckQueryWrapper.gt("StockSubOrAddWeight",0);
        List<Map<String, Object>> stockCheckList=singleFieldSumSelect("StockWeight","InventoryDate",startQueryDate,endQueryDate,"production_manage_stock_check",true,stockCheckMapper,stockCheckQueryWrapper,null,null);
        return listMapAdd(sortingLossList,stockCheckList);
    }*/



}
