package com.zxs.server.service.gugeng.salemanage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.ProductionManageOrderProductReturnDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProductReturn;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductReturnMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  退货服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-03
 */
@Service
public class ProductionManageOrderProductReturnService extends ServiceImpl<ProductionManageOrderProductReturnMapper, ProductionManageOrderProductReturn> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    @Transactional(rollbackFor = Exception.class)
    public void returnGoods(ProductionManageOrderProductReturnDTO productReturnDTO) throws SuperCodeException, ParseException {
        Long orderId=productReturnDTO.getOrderId();
        ProductionManageOrder order=orderMapper.selectById(orderId);
        if (null==order){
            CommonUtil.throwSupercodeException(500,"订单不存在");
        }
//        Integer verifyStatus=order.getVerifyStatus();
//        if (null==verifyStatus || verifyStatus.intValue()!=1){
//            CommonUtil.throwSupercodeException(500,"订单未通过审核");
//        }
        Byte orderStatus=order.getOrderStatus();
        if (OrderStatusEnum.UN_RECEIPT.getStatus()!=orderStatus.intValue()){
            CommonUtil.throwSupercodeException(500,"订单需为待收货状态");
        }
        List<ProductionManageOrderProductReturn> productReturns=productReturnDTO.getReturnProductList();
        if (null==productReturns || productReturns.isEmpty()){
            CommonUtil.throwSupercodeException(500,"退货商品不能为空");
        }

        Integer returnBatchNum= baseMapper.selectMaxReturnBatchNum(orderId);
        if (null==returnBatchNum){
            returnBatchNum=1;
        }else {
            returnBatchNum++;
        }
        List<ProductionManageOrderProductReturn> needSaveProductReturns=new ArrayList<>();
        for (ProductionManageOrderProductReturn orderProductReturn:productReturns) {
            Integer returnBoxQuantity=orderProductReturn.getReturnBoxQuantity();
            Integer returnQuantity=orderProductReturn.getReturnQuantity();
            Double returnWeight=orderProductReturn.getReturnWeight();
            orderProductReturn.setOrderDate(order.getOrderDate());
            orderProductReturn.setOrganizationId(order.getOrganizationId());
            orderProductReturn.setSysId(order.getSysId());
            if ((null!=returnBoxQuantity && returnBoxQuantity!=0) || (null!=returnQuantity && returnQuantity!=0) || (null!=returnWeight && returnWeight!=0)){
                orderProductReturn.setOrderId(orderId);
                orderProductReturn.setReturnBatchNum(returnBatchNum);
                orderProductReturn.setReturnDate(CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss"));
                needSaveProductReturns.add(orderProductReturn);
            }
        }
        if (null!=needSaveProductReturns && !needSaveProductReturns.isEmpty()){
            this.saveBatch(needSaveProductReturns);
            order.setReturnReason(productReturnDTO.getReturnReason());
            orderMapper.updateById(order);
        }
    }
}
