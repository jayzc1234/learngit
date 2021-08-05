package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOrderProductReceivedDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReceived;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductReceivedMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageOrderProductDataStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageProductRecordService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.ProductionManageOrderReceivedVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ORDER_ID;


/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-03
 */
@Service
public class ProductionManageOrderProductReceivedService extends ServiceImpl<ProductionManageOrderProductReceivedMapper, ProductionManageOrderProductReceived> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper packageMessageMapper;

    @Autowired
    private ProductionManageProductRecordService recordService;

    @Autowired
    private ProductionManageOrderProductDataStatisticsService orderProductDataStatisticsService;

    @Transactional(rollbackFor = Exception.class)
    public void reject(ProductionManageOrderProductReceivedDTO productReceivedDTO) throws SuperCodeException, ParseException {
        Long orderId=productReceivedDTO.getOrderId();
        ProductionManageOrder order=orderMapper.selectById(orderId);
        if (null==order){
            CommonUtil.throwSupercodeException(500,"订单不存在");
        }
//        Integer verifyStatus=order.getVerifyStatus();
//        if (null==verifyStatus || verifyStatus.intValue()!=1){
//            CommonUtil.throwSupercodeException(500,"订单未通过审核");
//        }
        Byte orderStatus=order.getOrderStatus();
        if (OrderStatusEnum.UN_RECEIPT.getStatus()!=orderStatus.intValue() && OrderStatusEnum.CONFIRMED.getStatus()!=orderStatus.intValue()){
            CommonUtil.throwSupercodeException(500,"订单需为待收货或者已确认状态");
        }
        List<ProductionManageOrderProductReceived> productReceiveds=productReceivedDTO.getOrderProductReceivedList();
        if (null==productReceiveds || productReceiveds.isEmpty()){
            CommonUtil.throwSupercodeException(500,"拒收时实收商品不能为空");
        }
        BigDecimal receivedOrderMoney=new BigDecimal(0);
        Integer receivedOrderQuantity=null;
        BigDecimal receivedTotalBenefitPrice=new BigDecimal(0);
        BigDecimal receivedOrderWeight=new BigDecimal(0);

        for (ProductionManageOrderProductReceived productReceived:productReceiveds) {
            productReceived.setOrderId(orderId);
            if (StringUtils.isNotBlank(productReceived.getReceivedBenefitPrice())){
                receivedTotalBenefitPrice=CommonUtil.bigDecimalAdd(receivedTotalBenefitPrice,new BigDecimal(productReceived.getReceivedBenefitPrice()));
            }
            if (StringUtils.isNotBlank(productReceived.getReceivedProMoney())){
                receivedOrderMoney=CommonUtil.bigDecimalAdd(receivedOrderMoney,new BigDecimal(productReceived.getReceivedProMoney()));
            }
            if (null!=productReceived.getReceivedProWeight()){
                receivedOrderWeight=CommonUtil.bigDecimalAdd(receivedOrderWeight,new BigDecimal(productReceived.getReceivedProWeight()));
            }
            if (null!=productReceived.getReceivedProQuantity()){
                receivedOrderQuantity=CommonUtil.integerAdd(receivedOrderQuantity,productReceived.getReceivedProQuantity());
            }
        }
        order.setReceivedOrderWeight(receivedOrderWeight.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
        order.setReceivedOrderQuantity(receivedOrderQuantity);
        order.setReceivedOrderMoney(receivedOrderMoney.setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        order.setReceivedTotalBenefitPrice(receivedTotalBenefitPrice.setScale(2,BigDecimal.ROUND_HALF_UP).toString());
        order.setDoneDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        this.saveOrUpdateBatch(productReceiveds);
        //更新销售产品统计实收额
        orderProductDataStatisticsService.updateReceivedOrderMoney(productReceiveds,order.getOrderDate());

        order.setRejectReason(productReceivedDTO.getRejectReason());
        order.setOrderStatus((byte) OrderStatusEnum.DONE.getStatus());
        order.setRejectStatus(1);
        order.setDoneDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
        order.setActualReceivedMark(productReceivedDTO.getActualReceivedMark());
        orderMapper.updateById(order);

        recordService.updateTotalAmountReceived(order, productReceiveds);
    }

    public Integer getMaxReceivedBatchNum(Long orderId) {
        return baseMapper.getMaxReceivedBatchNum(orderId);
    }

    public ProductionManageOrderReceivedVO getByOrderId(Long orderId) {
        ProductionManageOrderReceivedVO orderReceivedVO=new ProductionManageOrderReceivedVO();
        List<ProductionManageOrderProductReceived> productReceiveds=baseMapper.getByOrderId(orderId);
        if (CollectionUtils.isNotEmpty(productReceiveds)){
            Double totalReceivedProMoney = 0d;
            Double totalReceivedBenefitPrice = 0d;
            Integer totalReceivedProQuantity = 0;
            Double totalReceivedProWeight = 0d;
            Integer totalReceivedProductNum = 0;
            for (ProductionManageOrderProductReceived productReceived : productReceiveds) {
                String receivedProMoney = productReceived.getReceivedProMoney();
                String receivedBenefitPrice = productReceived.getReceivedBenefitPrice();
                Integer receivedProQuantity = productReceived.getReceivedProQuantity();
                Double receivedProWeight = productReceived.getReceivedProWeight();
                Integer receivedProductNum = productReceived.getReceivedProductNum();

                if (StringUtils.isNotBlank(receivedBenefitPrice)){
                    totalReceivedBenefitPrice=CommonUtil.doubleAdd(Double.parseDouble(receivedBenefitPrice),totalReceivedBenefitPrice);
                }
                if (StringUtils.isNotBlank(receivedProMoney)){
                    totalReceivedProMoney=CommonUtil.doubleAdd(Double.parseDouble(receivedProMoney),totalReceivedProMoney);
                }
                totalReceivedProQuantity=CommonUtil.integerAdd(receivedProQuantity,totalReceivedProQuantity);
                totalReceivedProWeight=CommonUtil.doubleAdd(receivedProWeight,totalReceivedProWeight);
                totalReceivedProductNum=CommonUtil.integerAdd(receivedProductNum,totalReceivedProductNum);
            }

            orderReceivedVO.setTotalReceivedProductNum(totalReceivedProductNum);
            orderReceivedVO.setTotalReceivedProQuantity(totalReceivedProQuantity);
            orderReceivedVO.setTotalReceivedProWeight(totalReceivedProWeight);
            orderReceivedVO.setTotalReceivedProMoney(new BigDecimal(totalReceivedProMoney).setScale(2,BigDecimal.ROUND_DOWN));
            orderReceivedVO.setTotalReceivedBenefitPrice(new BigDecimal(totalReceivedBenefitPrice).setScale(2,BigDecimal.ROUND_DOWN));
        }
        orderReceivedVO.setReceivedList(productReceiveds);
        return orderReceivedVO;
    }

    /**
     * 通过订单id来获取实收信息列表
     *
     * @author shixiongfei
     * @date 2019-12-19
     * @updateDate 2019-12-19
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<ProductionManageOrderProductReceived> listByOrderId(Long orderId) {
        return query().eq(OLD_ORDER_ID, orderId).list();
    }
}