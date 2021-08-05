package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.PersonOrderConditionDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.StatisticsExcelBaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleStatisticsService implements StatisticsExcelBaseService {

    @Autowired
    private ProductManageOrderMapper orderdao;

    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private CommonUtil commonUtil;

    public RestResult<SaleAndOrderNumVO> amountAndNum(SaleUserLineChartDTO dateIntervalDTO) throws SuperCodeException, ParseException {
        RestResult restResult=new RestResult();
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());
        //获取统计数据
        List<Map<String,Object>> orderList=orderdao.amountAndNum(dateIntervalDTO);
        //新建返回全局vo
        SaleAndOrderNumVO saleAndOrderNumVO=new SaleAndOrderNumVO();
        //销售金额曲线数据
        LineChartVO saleAmountlineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleAmountvalues=new ArrayList<>();

        //实收金额曲线数据
        LineChartVO saleRealAmountlineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleRealAmountvalues=new ArrayList<>();

        //订单数曲线数据
        LineChartVO saleNumlineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> saleNumvalues=new ArrayList<>();

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
            }
        }else{
            Map<String,Map<String,Object>> orderMap=new HashMap<>();
            for (Map<String,Object> order:orderList) {
                orderMap.put(order.get("orderDate").toString(),order);
            }
            //获取总的订单数
            Integer totalOrderNum=orderdao.countOrderNum(dateIntervalDTO);
            saleAndOrderNumVO.setOrderNum(totalOrderNum);
            for (String date: dateList) {
                if (null!=orderMap.get(date)){
                    LineChartVO.NameAndValueVO nameAndValueVO1=saleAmountlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO numNameAndValueVO2=saleNumlineChartVO.new NameAndValueVO();
                    LineChartVO.NameAndValueVO realAmountNameAndValueVO=saleNumlineChartVO.new NameAndValueVO();
                    Map<String,Object> order=orderMap.get(date);

                    //订单金额
                    Object orderMoney=order.get("orderMoney");
                    nameAndValueVO1.setName(date);
                    nameAndValueVO1.setValue(orderMoney==null?0:orderMoney);
                    saleAmountvalues.add(nameAndValueVO1);

                    //订单数
                    Object receivedOrderMoney=order.get("receivedOrderMoney");
                    realAmountNameAndValueVO.setName(date);
                    realAmountNameAndValueVO.setValue(receivedOrderMoney==null?0:receivedOrderMoney);
                    saleRealAmountvalues.add(realAmountNameAndValueVO);

                    //订单数
                    Object orderNum=order.get("orderNum");
                    numNameAndValueVO2.setName(date);
                    numNameAndValueVO2.setValue(orderNum==null?0:orderNum);
                    saleNumvalues.add(numNameAndValueVO2);

                    if (null!=orderMoney){
                        BigDecimal bigDecimal=new BigDecimal(String.valueOf(orderMoney));
                        allbigDecimal=allbigDecimal.add(bigDecimal);
                    }
                    if (null!=receivedOrderMoney){
                        BigDecimal realbigDecimal=new BigDecimal(String.valueOf(receivedOrderMoney));
                        realAllbigDecimal=realAllbigDecimal.add(realbigDecimal);
                    }
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

                    saleNumvalues.add(nameAndValueVO);
                    saleAmountvalues.add(nameAndValueVO1);
                    saleRealAmountvalues.add(nameAndValueVO2);
                }
            }
        }
        saleAmountlineChartVO.setValues(saleAmountvalues);
        saleNumlineChartVO.setValues(saleNumvalues);
        saleRealAmountlineChartVO.setValues(saleRealAmountvalues);

        LineChartVO.Option optionAmount=saleAmountlineChartVO.new Option();
        optionAmount.setName("销售额");
        saleAmountlineChartVO.setOption(optionAmount);

        LineChartVO.Option optionRealAmount=saleAmountlineChartVO.new Option();
        optionRealAmount.setName("实收金额");
        saleRealAmountlineChartVO.setOption(optionRealAmount);

        LineChartVO.Option optionNum=saleAmountlineChartVO.new Option();
        optionNum.setName("订单数");
        saleNumlineChartVO.setOption(optionNum);
        saleAndOrderNumVO.setOrderMoney(allbigDecimal.toString());
        saleAndOrderNumVO.setRealOrderMoney(realAllbigDecimal.toString());
        List<LineChartVO> values=new ArrayList<>();
        values.add(saleAmountlineChartVO);
        values.add(saleNumlineChartVO);
        values.add(saleRealAmountlineChartVO);
        saleAndOrderNumVO.setValues(values);
        restResult.setState(200);
        restResult.setResults(saleAndOrderNumVO);
        return restResult;
    }

    /**
     * 个人销售排行
     * @param dateIntervalListDTO
     * @return
     * @throws SuperCodeException
     * @throws ParseException
     */
    public AbstractPageService.PageResults<List<SaleOrderPersonRankingVO>> salePersonRanking(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException, ParseException {
        Page<SaleOrderPersonRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        IPage<SaleOrderPersonRankingVO> ipage=orderdao.listSaleRanking(page, dateIntervalListDTO);
        List<SaleOrderPersonRankingVO> list=ipage.getRecords();
        setRank(dateIntervalListDTO.getCurrent(),dateIntervalListDTO.getDefaultPageSize(), list);
        AbstractPageService.PageResults<List<SaleOrderPersonRankingVO>> pageResults= CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    public static void setRank(Integer current,Integer pageSize, List<? extends RankVO> list) {
        if (null!=list && !list.isEmpty()) {
        	int rankstart=(current-1)*pageSize+1;
        	for (RankVO rankVO : list) {
                rankVO.setRank(rankstart++);
			}
		}
    }

    /**
     * 产品销售排行
     * @param dateIntervalListDTO
     * @return
     * @throws SuperCodeException
     */
    public AbstractPageService.PageResults<List<SaleOrderProductRankingVO>> orderProductRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException {
        Page<SaleOrderProductRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        IPage<SaleOrderProductRankingVO> ipage=orderdao.listOrderProductRank(page, dateIntervalListDTO);

        List<SaleOrderProductRankingVO> list=ipage.getRecords();
        int current=(int) ipage.getCurrent();
        if (null!=list && !list.isEmpty()) {
            int rankstart=(current-1)*dateIntervalListDTO.getPageSize()+1;
            for (SaleOrderProductRankingVO saleOrderPersonRankingVO : list) {
                saleOrderPersonRankingVO.setRank(rankstart++);
            }
        }
        AbstractPageService.PageResults<List<SaleOrderProductRankingVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    /**
     * 潜在客户排行
     * @param dateIntervalListDTO
     * @return
     */
    public AbstractPageService.PageResults<List<PotentialClientRankingVO>> potentialclientRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException {
        Page<PotentialClientRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        Integer total=orderdao.orderClientSum(dateIntervalListDTO);
        IPage<PotentialClientRankingVO> ipage=clientMapper.listPotentialclientRank(page, dateIntervalListDTO);

        List<PotentialClientRankingVO> list=ipage.getRecords();
        int current=(int) ipage.getCurrent();
        if (null!=list && !list.isEmpty()) {
            int rankstart=(current-1)*dateIntervalListDTO.getPageSize()+1;
            for (PotentialClientRankingVO saleOrderPersonRankingVO : list) {
                saleOrderPersonRankingVO.setRank(rankstart++);
            }
        }
        AbstractPageService.PageResults<List<PotentialClientRankingVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    /**
     * 订单状态排行
     * @param dateIntervalListDTO
     * @return
     */
    public AbstractPageService.PageResults<List<OrderStatusRankingVO>> orderStatusRankPage(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException {
        Page<OrderStatusRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        Integer total=orderdao.orderClientSum(dateIntervalListDTO);
        IPage<OrderStatusRankingVO> ipage=orderdao.orderStatusRank(page, dateIntervalListDTO);

        List<OrderStatusRankingVO> list=ipage.getRecords();
        int current=(int) ipage.getCurrent();
        if (null!=list && !list.isEmpty()) {
            int rankstart=(current-1)*dateIntervalListDTO.getPageSize()+1;
            for (OrderStatusRankingVO saleOrderPersonRankingVO : list) {
                saleOrderPersonRankingVO.setOrderStatus(OrderStatusEnum.getDesc(saleOrderPersonRankingVO.getOrderStatus()));
                saleOrderPersonRankingVO.setRank(rankstart++);
            }
        }
        AbstractPageService.PageResults<List<OrderStatusRankingVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    /**
     * 订单客户
     * @param dateIntervalListDTO
     * @return
     * @throws SuperCodeException
     */
    public AbstractPageService.PageResults<List<SaleClientRankingVO>> clientOrderRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException {
        Page<SaleClientRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        Integer total=orderdao.orderClientSum(dateIntervalListDTO);
        IPage<SaleClientRankingVO> ipage=orderdao.listClientOrderRank(page, dateIntervalListDTO);
        
        List<SaleClientRankingVO> list=ipage.getRecords();
        int current=(int) ipage.getCurrent();
        if (null!=list && !list.isEmpty()) {
        	int rankstart=(current-1)*dateIntervalListDTO.getPageSize()+1;
        	for (SaleClientRankingVO saleOrderPersonRankingVO : list) {
        		saleOrderPersonRankingVO.setRank(rankstart++);
			}
		}
        Map<String,Integer> other=new HashMap<>();
        other.put("totalClient",total);
        AbstractPageService.PageResults<List<SaleClientRankingVO>> pageResults=CommonUtil.iPageToPageResults(ipage,other);
        return pageResults;
    }


    public void exportClientOrderRank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
    	 dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
         dateIntervalListDTO.setSysId(commonUtil.getSysId());
    	exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"订单客户","clientOrderRank","listClientOrderRankWithLimt",orderdao,response);
    }

    public void exportProductRank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
    	 dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
         dateIntervalListDTO.setSysId(commonUtil.getSysId());
    	exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"产品销售排行","orderProductRank","listlistExcelProductRankWithLimt",orderdao,response);
    }


    public void exportPersonSalerank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
    	exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"个人销售排行","salePersonRanking","listSaleRankingWithLimt",orderdao,response);
    }

    /**
     * 导出潜在客户
     * @param dateIntervalListDTO
     * @param response
     * @throws Exception
     */
    public void exportPotentialclientRankExcel(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"潜在客户排行","potentialclientRank","listPotentialclientRankWithLimt",clientMapper,response);
    }

    /**
     * 导出订单汇总状态
     * @param dateIntervalListDTO
     * @param response
     * @throws Exception
     */
    public void exportOrderStatusRank(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"订单状态汇总","orderStatusRankPage","listOrderStatusRankPageWithLimt",orderdao,response);
    }

    /**
     * 个人订单情况
     * @param dateIntervalListDTO
     * @return
     */
    public AbstractPageService.PageResults<List<PersonOrderConditionVO>> personOrderCondition(PersonOrderConditionDTO dateIntervalListDTO) throws SuperCodeException {
        dateIntervalListDTO.transferSaleUserIds();
        Page<PersonOrderConditionVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        IPage<PersonOrderConditionVO> ipage=orderdao.listPersonOrderCondition(page, dateIntervalListDTO);
        List<PersonOrderConditionVO> personOrderConditionVOList=ipage.getRecords();
        if (CollectionUtils.isNotEmpty(personOrderConditionVOList)){
            for (PersonOrderConditionVO poc:personOrderConditionVOList ) {
                poc.setDoneDifferenceOrderMoney(CommonUtil.bigDecimalSub(poc.getDoneReceivedOrderMoney(),poc.getDoneOrderMoney()).setScale(2,BigDecimal.ROUND_HALF_UP));
            }
        }
        AbstractPageService.PageResults<List<PersonOrderConditionVO>> pageResults=CommonUtil.iPageToPageResults(ipage,null);
        return pageResults;
    }

    public Map<String, String> personOrderConditionBestSaleUser(DateIntervalListDTO dateIntervalListDTO) {
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        PersonOrderConditionVO personOrderConditionVO=orderdao.personOrderConditionBestSaleUser(dateIntervalListDTO);
        if (null!=personOrderConditionVO){
            Map<String, String> map=new HashMap<>();
            map.put("saleUserId",personOrderConditionVO.getSaleUserId());
            map.put("saleUserName",personOrderConditionVO.getSaleUserName());
            return map;
        }
        return null;
    }

    /**
     * 导出个人订单情况
     * @param dateIntervalListDTO
     * @param response
     */
    public void exportPersonOrderconditionExcel(PersonOrderConditionDTO dateIntervalListDTO, HttpServletResponse response) throws Exception {
        dateIntervalListDTO.setIds(dateIntervalListDTO.getSaleUserIds());
        dateIntervalListDTO.transferSaleUserIds();
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        exportExcelWithMethod(dateIntervalListDTO,commonUtil.getExportNumber(),"个人订单情况","personOrderCondition","listPersonOrderConditionLimit",orderdao,response);
    }
}
