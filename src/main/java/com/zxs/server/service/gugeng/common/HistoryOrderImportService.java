package com.zxs.server.service.gugeng.common;

import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.ProductionManageClientMO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.HistoryOrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderOutBoundStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 导入订单excel
*   1  客户地址可以为空
*   2、所以导入的客户判断唯一性只根据客户名称，联系人电话，联系人和客户类目
*   3.   所有订单产品信息默认为：等级为：一级， 产品规格为：A级 ，包装方式为：气柱袋溯源码封口标签，包装规格为：6个/箱
*   4.新增的产品信息除了产品类目必填和产品名称必填外其它取页面创建时的默认值
*   5.产品类目默认取“素菜瓜果类”
 *
 */
@Component
public class HistoryOrderImportService {
    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private ClientCategoryMapper clientCategoryMapper;

    @Autowired
    private ProductManageOrderMapper orderMapper;

    @Autowired
    private ProductionManageOrderProductMapper orderProductMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private OrderImportBaseInfoCommonService orderImportCommonService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SerialNumberGenerator serialNumberGenerator;

    private QueryWrapper<ProductionManageOrder> orderQueryWrapper = new QueryWrapper<>();


    @Transactional(rollbackFor = Exception.class)
    public void invoke(HistoryOrderImportExcelHead historyOrderImportExcelHead, Date orderDate, AnalysisContext analysisContext) {
        int rowIndex = analysisContext.readRowHolder().getRowIndex()+1;
        List<ProductionManageOrder> orderList = new ArrayList<>();
        Specification packSpecification = null;
        packSpecification = orderImportCommonService.getOrAddSpecification(rowIndex, "6个/箱", 2);
        Specification packWaySpecification = orderImportCommonService.getOrAddSpecification(rowIndex, "气柱袋溯源码封口标签", 3);
        try {
            ProductionManageOrder productionManageOrder = new ProductionManageOrder();
            ProductionManageOrderProduct orderProduct = new ProductionManageOrderProduct();
//            HashMap<Integer, String> map = (HashMap<Integer, String>) o;
//            String orderDateStr = map.get(0);
//            String clientCategoryName = map.get(1);
//            String clientName = map.get(2);
//            String contactMan = map.get(3);
//            String contactPhone = map.get(4);
//            String address = map.get(5);
//            String saleMan = map.get(6);
//            String productName = map.get(7);
//            String unit = map.get(8);
//            String weightOrNum = map.get(9);
//            String price = map.get(10);
//            String mark = map.get(11);



            String orderDateD = historyOrderImportExcelHead.getOrderDate();
            String clientCategoryName = historyOrderImportExcelHead.getCategoryName();
            String clientName = historyOrderImportExcelHead.getClientName();
            String contactMan =historyOrderImportExcelHead.getContactMan();
            String contactPhone = historyOrderImportExcelHead.getContactPhone();
            String address = historyOrderImportExcelHead.getAddress();
            String saleMan = historyOrderImportExcelHead.getSaleUserName();
            String productName = historyOrderImportExcelHead.getProductName();
            String unit = historyOrderImportExcelHead.getUnit();
            String weightOrNum =historyOrderImportExcelHead.getNum();
            String price = historyOrderImportExcelHead.getPrice();
            String mark = historyOrderImportExcelHead.getMark();
            //默认前两列为空则当作空行
            if (null==orderDateD && StringUtils.isBlank(clientCategoryName)) {
                return;
            }
            String organizationId = commonUtil.getOrganizationId();
            String sysId = commonUtil.getSysId();
            checkParams(rowIndex, orderDateD, clientCategoryName, clientName, contactMan, contactPhone, saleMan, productName, weightOrNum, price);
            String productId = orderImportCommonService.getProductId(rowIndex, productName,true);
            Long categoryId = orderImportCommonService.addClientCategory(clientCategoryName);
            Employee saleUser = orderImportCommonService.getEmployName(rowIndex,saleMan);
            ProductionManageClientMO productionManageClient = orderImportCommonService.addClient(clientName, contactMan, contactPhone, clientCategoryName, categoryId, address,null, saleMan,true,saleUser, rowIndex, null, null);

            ProductInfo productInfo = orderImportCommonService.getOrAddProductInfo(productId, "一等级", "A级", "6个/箱");
            Date deliveryDate = orderDate;
            int productNum = Double.valueOf(weightOrNum).intValue() * 6;
            productionManageOrder.setOrderStatus((byte) OrderStatusEnum.DONE.getStatus());
            if ("斤".equals(unit)) {//按重量
                productionManageOrder.setOrderType((byte) OrderTypeEnum.WEIGHT.getStatus());
                productionManageOrder.setOrderWeight(Double.parseDouble(weightOrNum));
                BigDecimal orderMoney =
                        new BigDecimal(price).multiply(new BigDecimal(weightOrNum)).setScale(2, BigDecimal.ROUND_HALF_UP);
                productionManageOrder.setOrderMoney(orderMoney.toString());

                //设置订单产品属性
                orderProduct.setOrderWeight(Double.parseDouble(weightOrNum));
                orderProduct.setTotalPrice(orderMoney);
            } else if ("箱".equals(unit)) {
                productionManageOrder.setOrderProductNum(productNum);
                productionManageOrder.setOrderType((byte) OrderTypeEnum.NUM.getStatus());
                productionManageOrder.setOrderQuantity(Double.valueOf(weightOrNum).intValue());
                productionManageOrder.setReceivedOrderQuantity(Double.valueOf(weightOrNum).intValue());
                BigDecimal orderMoney = new BigDecimal(price).multiply(new BigDecimal(Double.valueOf(weightOrNum) * 6)).setScale(2, BigDecimal.ROUND_HALF_UP);
                productionManageOrder.setOrderMoney(orderMoney.toString());

                //设置订单产品属性
                orderProduct.setTotalPrice(orderMoney);
                orderProduct.setOrderQuantity(6);
                orderProduct.setProductNum(productNum);
            }
            //订单状态设置
            productionManageOrder.setDeliveryType((byte) DeliveryTypeEnum.LOCAL_DELIVERY.getKey());
            productionManageOrder.setOutboundStatus(OrderOutBoundStatusEnum.ALL_DELIVEY.getStatus());
            productionManageOrder.setSourceFrom(2);
            productionManageOrder.setRejectStatus(-1);

            productionManageOrder.setDoneDate(deliveryDate);
            productionManageOrder.setDeliveryDate(deliveryDate);
            productionManageOrder.setOutboundNum(1);
            productionManageOrder.setTotalBenefitPrice("0");
            productionManageOrder.setOrderRemark(mark);
            //客户及客户类目
            productionManageOrder.setClientId(productionManageClient.getId());
            productionManageOrder.setClientCategoryId(categoryId);
            productionManageOrder.setClientName(productionManageClient.getClientName());
            productionManageOrder.setClientDetailAddress(productionManageClient.getDetailAddress());
            productionManageOrder.setClientAreaCode(productionManageClient.getAreaCode());
            productionManageOrder.setClientContactMan(contactMan);
            productionManageOrder.setClientCategoryName(clientCategoryName);
            productionManageOrder.setClientContactPhone(contactPhone);
            try {
                String clientMiddleAddress = commonUtil.getAddressWithAreaCode(productionManageClient.getAreaCode());
                productionManageOrder.setClientMiddleAddress(clientMiddleAddress);
                String clientAddress =clientMiddleAddress;
                if (StringUtils.isNotBlank(productionManageClient.getDetailAddress())) {
                    clientAddress=clientMiddleAddress+productionManageClient.getDetailAddress();
                }
                productionManageOrder.setClientAddress(clientAddress);
            }catch (Exception e){

            }
            //封装创建人组织等信息
            Employee employee = commonUtil.getEmployee();
            productionManageOrder.setCreateUserId(employee.getEmployeeId());
            productionManageOrder.setCreateUserName(employee.getName());
            productionManageOrder.setOrganizationId(commonUtil.getOrganizationId());
            productionManageOrder.setSysId(commonUtil.getSysId());

            //dingdanh
            String orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
            orderQueryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
            Integer count = orderMapper.selectCount(orderQueryWrapper);
            while (null != count && count > 0) {
                orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
                orderQueryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
                count = orderMapper.selectCount(orderQueryWrapper);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //设置销售人员
            productionManageOrder.setSaleRealUserId(saleUser.getUserId());
            productionManageOrder.setSaleUserId(saleUser.getEmployeeId());
            productionManageOrder.setSaleUserName(saleUser.getName());
            productionManageOrder.setOrderNo(orderNo);

            productionManageOrder.setProductName(productName);
            productionManageOrder.setClientId(productionManageClient.getId());
            productionManageOrder.setOrderDate(orderDate);
            productionManageOrder.setOrderRemark(mark);

            orderMapper.insert(productionManageOrder);
            //订单产品构建
            orderProduct.setOrderId(productionManageOrder.getId());
            if (price.length() > 10) {
                orderProduct.setUnitPrice(price.substring(0, 9));
            }
            orderProduct.setPackingSpecCode(packSpecification.getSpecificationId());
            orderProduct.setPackingSpecName(packSpecification.getSpecificationName());
            orderProduct.setPackingWayCode(packWaySpecification.getSpecificationId());
            orderProduct.setPackingWayName(packWaySpecification.getSpecificationName());
            orderProduct.setProductSpecCode(productInfo.getSpecificationId());
            orderProduct.setProductSpecName(productInfo.getSpecificationName());
            orderProduct.setProductLevelName(productInfo.getSpecificationName());
            orderProduct.setProductId(productId);
            orderProduct.setProductName(productName);
            orderProduct.setProductLevelCode(productInfo.getLevelSpecificationId());
            orderProduct.setProductLevelName(productInfo.getLevelSpecificationName());
            orderProductMapper.insert(orderProduct);
        } finally {
            orderImportCommonService.close();
        }
    }

    private static void checkParams(int rowIndex, String orderDate, String clientCategoryName, String clientName, String contactMan, String contactPhone, String saleMan,
                                    String productName, String weightOrNum, String price) throws SuperCodeExtException {
        if (StringUtils.isBlank(orderDate)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行日期有空值");
        }

        if (StringUtils.isBlank(clientCategoryName) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行客户类目有空值");
        }
        if (StringUtils.isBlank(clientName) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行客户名称有空值");
        }
        if (StringUtils.isBlank(contactMan) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行联系人有空值");
        }
        if (StringUtils.isBlank(contactPhone) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行联系电话有空值");
        }
        if (StringUtils.isBlank(saleMan) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行销售人员有空值");
        }
        if (StringUtils.isBlank(productName) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行产品名称有空值");
        }
        if (StringUtils.isBlank(weightOrNum) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行数量有空值");
        }
        if (StringUtils.isBlank(price) ) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行单价有空值");
        }

    }
}
