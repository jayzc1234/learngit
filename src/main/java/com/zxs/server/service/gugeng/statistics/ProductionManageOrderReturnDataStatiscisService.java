package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReturn;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderReturnDataStatiscis;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageOrderReturnDataStatiscisMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.diagramtransfer.LineChartDataTransfer;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderReturnDataCurveLineVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-22
 */
@Service
public class ProductionManageOrderReturnDataStatiscisService extends ServiceImpl<ProductionManageOrderReturnDataStatiscisMapper, ProductionManageOrderReturnDataStatiscis> implements BaseService<ProductionManageOrderReturnDataStatiscis> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    public OrderReturnDataCurveLineVO curve(DateIntervalDTO dateIntervalDTO) throws ParseException {
        OrderReturnDataCurveLineVO curveLineVO = new OrderReturnDataCurveLineVO();
        QueryWrapper<ProductionManageOrderProductReturn> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderProductReturn.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), "DATE_FORMAT(" + ProductionManageOrderProductReturn.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), "DATE_FORMAT(" + ProductionManageOrderProductReturn.COL_ORDER_DATE + ",'%Y-%m-%d')", dateIntervalDTO.getEndQueryDate());
        queryWrapper.groupBy("DATE_FORMAT(" + ProductionManageOrderProductReturn.COL_ORDER_DATE + ",'%Y-%m-%d')");
        List<String> dateList = DateUtils.dateZone(dateIntervalDTO.getStartQueryDate(), dateIntervalDTO.getEndQueryDate());

        List<LineChartVO> chartVOList = new ArrayList<>(4);
        List<Map<String, Object>> orderList = baseMapper.returnDataCurveLine(queryWrapper);
        //6.订单个数
        LineChartVO productNumLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "returnQuantity", "退货个数");

        //6.订单重量
        LineChartVO orderWeightLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "returnWeight", "退货重量");

        //6.订单数
        LineChartVO orderNumChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderNum", "退货订单数");

        //6.订单数
        LineChartVO orderQuantityChartVO=LineChartDataTransfer.transfer(dateList,orderList,"orderDate","returnBoxQuantity","退货箱数");

        chartVOList.add(orderNumChartVO);
        chartVOList.add(orderQuantityChartVO);
        chartVOList.add(productNumLineChartVO);
        chartVOList.add(orderWeightLineChartVO);
        curveLineVO.setValues(chartVOList);
        curveLineVO.setTotalOrderNum(orderNumChartVO.getTotalValue().intValue());
        curveLineVO.setTotalReturnBoxQuantity(orderQuantityChartVO.getTotalValue().intValue());
        curveLineVO.setTotalReturnQuantity(productNumLineChartVO.getTotalValue().intValue());
        curveLineVO.setTotalReturnWeight(orderWeightLineChartVO.getTotalValue().doubleValue());
        return  curveLineVO;
    }
}
