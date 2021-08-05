package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.StatisticsExcelBaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderDeliveryTypeRankingVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.PersonOrderConditionVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
public class ProductionManageWholeOrderAnalysisService implements StatisticsExcelBaseService {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    public PersonOrderConditionVO wholeOrderCondition(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        dateIntervalDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalDTO.setSysId(commonUtil.getSysId());
        PersonOrderConditionVO donePersonOrderConditionVO=orderMapper.wholeOrderDoneCondition(dateIntervalDTO);
        donePersonOrderConditionVO.nullToZero();
        PersonOrderConditionVO unDonePersonOrderConditionVO=orderMapper.wholeOrderUnDoneCondition(dateIntervalDTO);
        unDonePersonOrderConditionVO.nullToZero();
        donePersonOrderConditionVO.setUnDeliveryNum(unDonePersonOrderConditionVO.getUnDeliveryNum());
        donePersonOrderConditionVO.setUnReceiptNum(unDonePersonOrderConditionVO.getUnReceiptNum());

        donePersonOrderConditionVO.setDoneDifferenceOrderMoney(CommonUtil.bigDecimalSub(donePersonOrderConditionVO.getDoneReceivedOrderMoney(),donePersonOrderConditionVO.getDoneOrderMoney()));
        return donePersonOrderConditionVO;
    }

    public void exportWholeOrderCondition(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws SuperCodeException {
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        PersonOrderConditionVO personOrderConditionVO=wholeOrderCondition(dateIntervalListDTO);
        List<PersonOrderConditionVO> list=new ArrayList<>();
        if (null!=personOrderConditionVO){
            list.add(personOrderConditionVO);
        }
        ExcelUtils.listToExcel(list, dateIntervalListDTO.exportMetadataToMap(), "订单整天情况", response);
    }

    public AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>> orderdeliveryTypeRank(DateIntervalListDTO dateIntervalListDTO) throws SuperCodeException {
        Page<OrderDeliveryTypeRankingVO> page = new Page<>(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize());
        dateIntervalListDTO.setOrganizationId(commonUtil.getOrganizationId());
        dateIntervalListDTO.setSysId(commonUtil.getSysId());
        IPage<OrderDeliveryTypeRankingVO> ipage = orderMapper.orderdeliveryTypeRank(page, dateIntervalListDTO);

        List<OrderDeliveryTypeRankingVO> list = ipage.getRecords();
        QueryWrapper<ProductionManageOrder> orderQueryWrapper = commonUtil.queryTemplate(ProductionManageOrder.class);
        orderQueryWrapper.ge(StringUtils.isNotBlank(dateIntervalListDTO.getStartQueryDate()), ProductionManageOrder.COL_DONE_DATE, dateIntervalListDTO.getStartQueryDate())
                .le(StringUtils.isNotBlank(dateIntervalListDTO.getEndQueryDate()), ProductionManageOrder.COL_DONE_DATE, dateIntervalListDTO.getEndQueryDate());
        if (CollectionUtils.isNotEmpty(list)) {
            for (OrderDeliveryTypeRankingVO deliveryTypeRankVo : list) {
                orderQueryWrapper.eq(ProductionManageOrder.COL_DELIVERY_TYPE, deliveryTypeRankVo.getDeliveryType());
                BigDecimal receivedOrderMoney = orderMapper.sumReceivedOrderMoneyByDeliveryType(dateIntervalListDTO, deliveryTypeRankVo.getDeliveryType());
                deliveryTypeRankVo.setReceivedOrderMoney(receivedOrderMoney);
            }
            SaleStatisticsService.setRank(dateIntervalListDTO.getDefaultCurrent(), dateIntervalListDTO.getDefaultPageSize(), list);
        }
        return CommonUtil.iPageToPageResults(ipage, null);
    }

    /**
     * 发货类型比例
     * @param dateIntervalDTO
     * @param type:1订单数，2订单客户数，3销售额，4实收额
     * @return
     * @throws SuperCodeException
     */
    public RestResult<LineChartVO> orderdeliveryTypeProportion(DateIntervalListDTO dateIntervalDTO, Integer type) throws SuperCodeException {
        RestResult<LineChartVO> restResult=new RestResult<>();

        AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>> pageResults=orderdeliveryTypeRank(dateIntervalDTO);
        LineChartVO lineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> values=new ArrayList<>();
        LineChartVO.NameAndValueVO outNameValue=lineChartVO.new NameAndValueVO();
        LineChartVO.NameAndValueVO localNameValue=lineChartVO.new NameAndValueVO();
        localNameValue.setName("本部发货");
        outNameValue.setName("外部发货");

        Double localValue=0d;
        Double outValue=0d;
        Double totalValue=0d;
        if (null==pageResults || null==pageResults.getList() || pageResults.getList().isEmpty()){
            localNameValue.setValue(0);
            outNameValue.setValue(0);
        }else{
            List<OrderDeliveryTypeRankingVO> list= pageResults.getList();
            for (OrderDeliveryTypeRankingVO eliveryTypeRankingVO:list ) {
                int deliverType=Integer.valueOf(eliveryTypeRankingVO.getDeliveryType());
                if (DeliveryTypeEnum.LOCAL_DELIVERY.getKey()==deliverType){
                    localValue= caculete(type, eliveryTypeRankingVO);
                }else {
                    outValue= caculete(type, eliveryTypeRankingVO);
                }
                totalValue=localValue+outValue;
            }
            if (totalValue.compareTo(0D)==0){
                localNameValue.setValue(0);
                outNameValue.setValue(0);
            }else {
                if (localValue.compareTo(0D)==0){
                    localNameValue.setValue(0);
                    outNameValue.setValue(100);
                }else if (outValue.compareTo(0D)==0){
                    localNameValue.setValue(100);
                    outNameValue.setValue(0);
                }else {
                    Double local=localValue/totalValue*100;
                    localNameValue.setValue(new BigDecimal(local).setScale(2,BigDecimal.ROUND_HALF_UP));
                    outNameValue.setValue(new BigDecimal(100-local).setScale(2,BigDecimal.ROUND_HALF_UP));
                }
            }
        }
        values.add(localNameValue);
        values.add(outNameValue);
        lineChartVO.setValues(values);
        restResult.setState(200);
        restResult.setResults(lineChartVO);
        return restResult;
    }

    private Double caculete(Integer type, OrderDeliveryTypeRankingVO eliveryTypeRankingVO) {
        Double localValue=0D;
        switch (type){
            case 1:
                localValue= Optional.ofNullable(eliveryTypeRankingVO.getOrderNum()).orElse(0).doubleValue();
                break;
            case 2:
                localValue= Optional.ofNullable(eliveryTypeRankingVO.getClientNum()).orElse(0).doubleValue();
                break;
            case 3:
                localValue= Double.parseDouble(Optional.ofNullable(eliveryTypeRankingVO.getOrderMoney()).orElse("0"));
                break;
            case 4:
                localValue= Optional.ofNullable(eliveryTypeRankingVO.getReceivedOrderMoney()).orElse(new BigDecimal(0)).doubleValue();
                break;
            default:break;
        }
        return localValue;
    }

    /**
     * 发货类型
     * @param dateIntervalListDTO
     * @param response
     * @throws SuperCodeException
     */
    public void exportOrderdeliveryType(DateIntervalListDTO dateIntervalListDTO, HttpServletResponse response) throws SuperCodeException {
        AbstractPageService.PageResults<List<OrderDeliveryTypeRankingVO>> pageResults= orderdeliveryTypeRank(dateIntervalListDTO);
        List<OrderDeliveryTypeRankingVO> list=pageResults.getList();
        if (CollectionUtils.isNotEmpty(list)){
            for (OrderDeliveryTypeRankingVO deliveryTypeRankingVO:list ) {
                deliveryTypeRankingVO.setDeliveryType(DeliveryTypeEnum.getDesc(deliveryTypeRankingVO.getDeliveryType()));
            }
        }
        ExcelUtils.listToExcel(list, dateIntervalListDTO.exportMetadataToMap(), "发货类型", response);
    }

}