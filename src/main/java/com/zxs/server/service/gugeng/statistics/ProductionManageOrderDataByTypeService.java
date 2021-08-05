package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.StatisticsOrderDataByTypeConstants;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.OrderConditionLineChartDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderVerifyStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageOrderDataByType;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageOrderDataByTypeMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.diagramtransfer.LineChartDataTransfer;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.OrderConditionLineVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductionManageOrderDataByTypeListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-17
 */
@Slf4j
@Service
public class ProductionManageOrderDataByTypeService extends ServiceImpl<ProductionManageOrderDataByTypeMapper, ProductionManageOrderDataByType> implements BaseService<ProductionManageOrderDataByTypeListVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    public OrderConditionLineVO orderConditionCurve(OrderConditionLineChartDTO dateIntervalDTO) throws ParseException {

        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderDataByType.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageOrderDataByType.COL_ORDER_DATE, dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageOrderDataByType.COL_ORDER_DATE, dateIntervalDTO.getEndQueryDate());
        Integer type = dateIntervalDTO.getType();
        if (null == type) {
            CommonUtil.throwSuperCodeExtException(500, "type不能为空");
        }
        queryWrapper.eq(ProductionManageOrderDataByType.COL_TYPE_VALUE, type);
        queryWrapper.groupBy(ProductionManageOrderDataByType.COL_ORDER_DATE);
        List<String> dateList = DateUtils.dateZone(dateIntervalDTO.getStartQueryDate(), dateIntervalDTO.getEndQueryDate());
        List<Map<String, Object>> orderList = baseMapper.orderConditionCurve(queryWrapper);
        //销售额
        LineChartVO saleMoneyLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderMoney", "销售额");

        //6.订单个数
        LineChartVO orderProductNumLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderProductNum", "订单个数");

        //6.订单重量
        LineChartVO orderWeightLineChartVO = LineChartDataTransfer.transfer(dateList, orderList, "orderDate", "orderWeight", "订单重量");

        //6.订单数
        LineChartVO orderNumChartVO=LineChartDataTransfer.transfer(dateList,orderList,"orderDate","orderNum","订单数");

        //6.订单箱数
        LineChartVO orderQuantityChartVO=LineChartDataTransfer.transfer(dateList,orderList,"orderDate","orderQuantity","订单箱数");

        //6.订单份数
        LineChartVO ggPartionNumChartVO=LineChartDataTransfer.transfer(dateList,orderList,"orderDate","ggPartionNum","订单份数");

        List<LineChartVO> lineChartVOList=new ArrayList<>(3);
        lineChartVOList.add(orderNumChartVO);
        lineChartVOList.add(orderQuantityChartVO);
        lineChartVOList.add(orderProductNumLineChartVO);
        lineChartVOList.add(orderWeightLineChartVO);
        lineChartVOList.add(saleMoneyLineChartVO);
        lineChartVOList.add(ggPartionNumChartVO);

        OrderConditionLineVO saleAndOrderNumVO=new OrderConditionLineVO();
        saleAndOrderNumVO.setValues(lineChartVOList);
        saleAndOrderNumVO.setOrderMoney(saleMoneyLineChartVO.getTotalValue().toString());
        saleAndOrderNumVO.setOrderNum(orderNumChartVO.getTotalValue().intValue());
        saleAndOrderNumVO.setOrderProductNum(orderProductNumLineChartVO.getTotalValue().intValue());
        saleAndOrderNumVO.setOrderWeight(orderWeightLineChartVO.getTotalValue().doubleValue());
        saleAndOrderNumVO.setOrderQuantity(orderQuantityChartVO.getTotalValue().intValue());
        saleAndOrderNumVO.setGgPartionNum(ggPartionNumChartVO.getTotalValue().intValue());
      return  saleAndOrderNumVO;
    }

    @Override
    public IPage<ProductionManageOrderDataByTypeListVO> pageList(DaoSearch daoSearch) {
        DateIntervalListDTO dateIntervalDTO = (DateIntervalListDTO) daoSearch;
        Page<ProductionManageOrderDataByTypeListVO> page = CommonUtil.genPage(dateIntervalDTO);
        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = commonUtil.queryTemplate(ProductionManageOrderDataByType.class);
        queryWrapper.ge(StringUtils.isNotBlank(dateIntervalDTO.getStartQueryDate()), ProductionManageOrderDataByType.COL_ORDER_DATE, dateIntervalDTO.getStartQueryDate());
        queryWrapper.le(StringUtils.isNotBlank(dateIntervalDTO.getEndQueryDate()), ProductionManageOrderDataByType.COL_ORDER_DATE, dateIntervalDTO.getEndQueryDate());
        List<Integer> typeList = StatisticsOrderDataByTypeConstants.getTypeList(StatisticsOrderDataByTypeConstants.ORDER_STATUS);
        //古耕不需要审核数据
//        typeList.add(StatisticsOrderDataByTypeConstants.ORDER_VERIFY_WAIT_VERIFYD);
        queryWrapper.in(ProductionManageOrderDataByType.COL_TYPE_VALUE, typeList);
        queryWrapper.groupBy(ProductionManageOrderDataByType.COL_TYPE_VALUE);
        queryWrapper.orderByAsc(ProductionManageOrderDataByType.COL_TYPE_VALUE);
        IPage<ProductionManageOrderDataByTypeListVO> productionManageOrderDataByTypeListVOIPage = baseMapper.orderConditionPageList(page, queryWrapper);
        List<ProductionManageOrderDataByTypeListVO> records = productionManageOrderDataByTypeListVOIPage.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            StringBuilder builder = new StringBuilder();
            builder.append(dateIntervalDTO.getStartQueryDate()).append("~").append(dateIntervalDTO.getEndQueryDate());
            String dateTime = builder.toString();
            for (ProductionManageOrderDataByTypeListVO record : records) {
                record.setOrderDate(dateTime);
                String desc = OrderStatusEnum.getDesc(record.getTypeValue());
                if (StringUtils.isBlank(desc)) {
                    desc = OrderVerifyStatusEnum.getDesc(record.getTypeValue());
                }
                record.setTypeValue(desc);
            }
        }
        return productionManageOrderDataByTypeListVOIPage;
    }

    /**
     * 订单情况，订单数据数据同步
     *
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void timedTaskDataSync() {
        log.info("开始同步订单情况");
        QueryWrapper<ProductionManageOrderDataByType> queryWrapper = new QueryWrapper<>();
        baseMapper.delete(queryWrapper);

        queryWrapper.groupBy(ProductionManageOrder.COL_ORGANIZATION_ID, ProductionManageOrder.COL_SYS_ID, ProductionManageOrder.COL_ORDER_STATUS, "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')");
        List<ProductionManageOrderDataByType> saleClientRankingVOS = baseMapper.syncOrderDataByTypeDataListByType(queryWrapper);

        queryWrapper.eq(ProductionManageOrder.COL_VERIFY_STATUS, OrderVerifyStatusEnum.WAIT_VERIFYD.getStatus());
        queryWrapper.groupBy(ProductionManageOrder.COL_ORGANIZATION_ID, ProductionManageOrder.COL_SYS_ID, "date_format(" + ProductionManageOrder.COL_ORDER_DATE + ",'%Y-%m-%d')");
        List<ProductionManageOrderDataByType> saleClientRankingVOS2 = baseMapper.syncOrderDataByTypeDataListByVerifyStatus(queryWrapper);

        if (CollectionUtils.isNotEmpty(saleClientRankingVOS2)) {
            try {
                StringBuilder builder = new StringBuilder();
                for (ProductionManageOrderDataByType productionManageOrderDataByType : saleClientRankingVOS2) {
                    Integer ggPartionNum = productionManageOrderDataByType.getGgPartionNum();
                    builder.append(ggPartionNum).append(",");
                }
                log.info("执行订单情况查询的订单数据为：" + builder.toString());
            } catch (Exception e) {
            }
            saveBatch(saleClientRankingVOS2);
        }
        if (CollectionUtils.isNotEmpty(saleClientRankingVOS)){
            try {
                StringBuilder builder=new StringBuilder();
                for (ProductionManageOrderDataByType productionManageOrderDataByType : saleClientRankingVOS2) {
                    Integer ggPartionNum = productionManageOrderDataByType.getGgPartionNum();
                    builder.append(ggPartionNum).append(",");
                }
                log.info("执行订单情况查询的订单数据为："+builder.toString());
            }catch (Exception e){
            }
            saveBatch(saleClientRankingVOS);
        }

        log.info("同步订单情况同步完成");
    }

}
