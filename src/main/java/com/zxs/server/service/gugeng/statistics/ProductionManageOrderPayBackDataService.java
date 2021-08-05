package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderDataByType;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageOrderDataByTypeMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.diagramtransfer.LineChartDataTransfer;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductManageOrderUnPayBackListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderPayBackListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageSaleProductDataListVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SaleAndOrderNumVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-15
 */
@Service
public class ProductionManageOrderPayBackDataService extends ServiceImpl<ProductionManageOrderDataByTypeMapper, ProductionManageOrderDataByType> implements BaseService {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    public IPage<ProductionManageOrderPayBackListVO> pageList(DateIntervalListDTO dateIntervalDTO) throws SuperCodeException {
        Page<ProductionManageSaleProductDataListVO> page = CommonUtil.genPage(dateIntervalDTO);
        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderDataByType.class);
        String startQueryDate = dateIntervalDTO.getStartQueryDate();
        String endQueryDate = dateIntervalDTO.getEndQueryDate();
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')", startQueryDate.substring(0, startQueryDate.length() - 3));
        queryWrapper.le(StringUtils.isNotBlank(endQueryDate), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')", endQueryDate.substring(0, endQueryDate.length() - 3));
        queryWrapper.groupBy("DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')");
        queryWrapper.orderByAsc("DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m')");
        return baseMapper.orderPaybackPageList(page, queryWrapper);
    }

    public SaleAndOrderNumVO paybackLine(DateIntervalListDTO dateIntervalDTO) throws ParseException {
        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderDataByType.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getEndQueryDate());
        queryWrapper.groupBy("DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')");
        List<String> dateList = DateUtils.dateZone(dateIntervalDTO.getStartQueryDate(), dateIntervalDTO.getEndQueryDate());
        List<Map<String, Object>> orderList = baseMapper.paybackLine(queryWrapper);
        LineChartVO saleMoneyLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderMoney", "销售额");
        //6.实收额
        LineChartVO receivedSaleMoneyLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "receivedOrderMoney", "实收额");

        //6.实收额
        LineChartVO payBackLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "paybackRate", "回款率");

        List<LineChartVO> lineChartVOList = new ArrayList<>(3);
        lineChartVOList.add(payBackLineChartVO);
        lineChartVOList.add(saleMoneyLineChartVO);
        lineChartVOList.add(receivedSaleMoneyLineChartVO);

        SaleAndOrderNumVO saleAndOrderNumVO=new SaleAndOrderNumVO();
        saleAndOrderNumVO.setValues(lineChartVOList);
        saleAndOrderNumVO.setOrderMoney(saleMoneyLineChartVO.getTotalValue().toString());
        saleAndOrderNumVO.setRealOrderMoney(receivedSaleMoneyLineChartVO.getTotalValue().toString());

        if (receivedSaleMoneyLineChartVO.getTotalValue().compareTo(new BigDecimal(0))==0){
            saleAndOrderNumVO.setPaybackRate("0");
        }else {
            BigDecimal divide = receivedSaleMoneyLineChartVO.getTotalValue().divide(saleMoneyLineChartVO.getTotalValue(), 4, BigDecimal.ROUND_DOWN);
            BigDecimal multiply = divide.multiply(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_DOWN);
            saleAndOrderNumVO.setPaybackRate(multiply.toString());
        }
        return saleAndOrderNumVO;
    }


    public IPage<ProductManageOrderUnPayBackListVO> unPaybackPageList(DateIntervalListDTO dateIntervalDTO) {
        Page<ProductionManageSaleProductDataListVO> page = CommonUtil.genPage(dateIntervalDTO);
        QueryWrapper<ProductionManageOrder> queryWrapper = commonUtil.queryTemplate(ProductionManageOrder.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), "DATE_FORMAT(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getEndQueryDate());
        queryWrapper.ne(ProductionManageOrder.COL_ORDER_STATUS, OrderStatusEnum.DONE.getStatus());
        queryWrapper.orderByDesc("unPayBackMoney");
        return orderMapper.orderPaybackPageList(page, queryWrapper);
    }
}
