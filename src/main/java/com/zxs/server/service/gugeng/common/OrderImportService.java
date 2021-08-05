package com.zxs.server.service.gugeng.common;

import com.alibaba.excel.context.AnalysisContext;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.ProductionManageClientMO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.BaseOrderImportMO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.OrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.OuterOrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.NoNullArrayList;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderOutBoundStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryWayEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrderProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductionManageOrderProductMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundDeliveryWayMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageClientService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageSaleClientNumStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundService;
import net.app315.hydra.user.data.auth.sdk.utils.AreaUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderImportService {
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
    private SerialNumberGenerator serialNumberGenerator;

    @Autowired
    private ProductionManageOutboundService outboundService;

    @Autowired
    private ProductionManageOutboundDeliveryWayMapper outboundDeliveryWayMapper;

    @Autowired
    private ProductionManageSaleClientNumStatisticsService saleClientNumStatisticsService;

    @Autowired
    private ProductManageClientService clientService;

    @Autowired
    private CommonService commonService;

    private QueryWrapper<ProductionManageOrder> orderQueryWrapper = new QueryWrapper<>();

    public Map<String, Object> invoke(BaseOrderImportMO orderImportMO, Date orderDate, AnalysisContext analysisContext, Integer importType) {
        OrderImportExcelHead orderImportExcelHead=null;
        OuterOrderImportExcelHead outerOrderImportExcelHead=null;
        if (importType==1){
            orderImportExcelHead=(OrderImportExcelHead) orderImportMO;
        }else {
            outerOrderImportExcelHead=(OuterOrderImportExcelHead) orderImportMO;
        }
        int rowIndex = analysisContext.readRowHolder().getRowIndex() ;
        rowIndex++;
        if (rowIndex<2){
            return null;
        }
        ProductionManageOrder productionManageOrder = new ProductionManageOrder();
        ProductionManageOrderProduct orderProduct = new ProductionManageOrderProduct();
        boolean isInner=importType==1;
        String orderNo =isInner? orderImportExcelHead.getOrderNo():outerOrderImportExcelHead.getOrderNo();
        String clientCategoryName = isInner?orderImportExcelHead.getCategoryName():outerOrderImportExcelHead.getCategoryName();
        String clientName = isInner?orderImportExcelHead.getClientName():outerOrderImportExcelHead.getClientName();
        String contactMan = isInner?orderImportExcelHead.getContactMan():outerOrderImportExcelHead.getContactMan();
        String contactPhone = isInner?orderImportExcelHead.getContactPhone():outerOrderImportExcelHead.getContactPhone();
        String address = isInner?orderImportExcelHead.getAddress():outerOrderImportExcelHead.getAddress();
        String detailAddress =isInner? orderImportExcelHead.getDetailAddress():outerOrderImportExcelHead.getDetailAddress();
        String ggCustomersFond = isInner?orderImportExcelHead.getGgCustomersFond():outerOrderImportExcelHead.getGgCustomersFond();
        String ggDietTaboos  =isInner? orderImportExcelHead.getGgDietTaboos():outerOrderImportExcelHead.getGgDietTaboos();

        String deliveryDateStr = isInner?orderImportExcelHead.getDeliveryDate():outerOrderImportExcelHead.getDeliveryDate();
        Date deliveryDate=null;
        deliveryDate=CommonUtil.formatStringToDate(deliveryDateStr,"yyyy-MM-dd");
        //本部发货/外部发货
        String deliveryType = isInner?orderImportExcelHead.getDeliveryType():outerOrderImportExcelHead.getDeliveryType();
        String saleMan = isInner?orderImportExcelHead.getSaleUserName():outerOrderImportExcelHead.getSaleUserName();
        String productName =isInner? orderImportExcelHead.getProductName():outerOrderImportExcelHead.getProductName();
        String productLevelName = isInner?orderImportExcelHead.getProductLevelName():outerOrderImportExcelHead.getProductLevelName();
        String productSpecName =isInner? orderImportExcelHead.getProductSpecName():outerOrderImportExcelHead.getProductSpecName();
        String packSpecName =isInner? orderImportExcelHead.getPackingSpecName():outerOrderImportExcelHead.getPackingSpecName();
        String packWayName = isInner?orderImportExcelHead.getPackingWayName():outerOrderImportExcelHead.getPackingWayName();

        Double orderWeight = isInner?orderImportExcelHead.getOrderWeight():outerOrderImportExcelHead.getOrderWeight();
        Double weightPrice = isInner?orderImportExcelHead.getWeightUnitPrice():outerOrderImportExcelHead.getWeightUnitPrice();

        Integer ggPartionNum = isInner?orderImportExcelHead.getGgPartionNum():outerOrderImportExcelHead.getGgPartionNum();
        Double ggPartionPrice = isInner?orderImportExcelHead.getGgPartionPrice():outerOrderImportExcelHead.getGgPartionPrice();

        Integer productBoxNum = isInner?orderImportExcelHead.getOrderQuantity():outerOrderImportExcelHead.getOrderQuantity();
        Double productBoxNumPrice =isInner? orderImportExcelHead.getBoxNumUnitPrice():outerOrderImportExcelHead.getBoxNumUnitPrice();
        Integer productNum = isInner?orderImportExcelHead.getProductNum():outerOrderImportExcelHead.getProductNum();
        Double numPrice = isInner?orderImportExcelHead.getNumUnitPrice():outerOrderImportExcelHead.getNumUnitPrice();
        String benefitPrice = isInner?orderImportExcelHead.getBenefitPrice():outerOrderImportExcelHead.getBenefitPrice();
        String mark = isInner?orderImportExcelHead.getMark():outerOrderImportExcelHead.getMark();

        if (StringUtils.isNotBlank(orderNo) && orderNo.contains("该行请勿删除")){
            return null;
        }
        if (StringUtils.isBlank(orderNo) && StringUtils.isBlank(clientCategoryName) && StringUtils.isBlank(clientName)) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        Map<Integer, ProductionManageOrder> orderMap = new HashMap<>();
        Map<Integer, ProductionManageOrderProduct> orderProductHashMap = new HashMap<>();

        String copyAddress = address;
        checkParams(rowIndex, clientCategoryName, clientName, contactMan, contactPhone, saleMan, address, deliveryDate, deliveryType, productName);
        int orderType = checkWeigHTOrNum(rowIndex, orderWeight, weightPrice, productBoxNum,productBoxNumPrice, productNum, numPrice,ggPartionNum,ggPartionPrice);
        if (StringUtils.isNotBlank(packSpecName)){
            Specification packSpecification = orderImportCommonService.getOrAddSpecification(rowIndex, packSpecName, 2);
            orderProduct.setPackingSpecCode(packSpecification.getSpecificationId());
            orderProduct.setPackingSpecName(packSpecification.getSpecificationName());
        }
        if (StringUtils.isNotBlank(packWayName)){
            Specification packWaySpecification = orderImportCommonService.getOrAddSpecification(rowIndex, packWayName, 3);
            orderProduct.setPackingWayCode(packWaySpecification.getSpecificationId());
            orderProduct.setPackingWayName(packWaySpecification.getSpecificationName());
        }

        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        //创建基础数据
        String productId = orderImportCommonService.getProductId(rowIndex, productName, false);
        Long categoryId = orderImportCommonService.addClientCategory(clientCategoryName);
        Employee saleUser = orderImportCommonService.getEmployName(rowIndex, saleMan);
        ProductionManageClientMO productionManageClient = orderImportCommonService.addClient(clientName, contactMan, contactPhone, clientCategoryName, categoryId, address, detailAddress, saleMan, false, saleUser,rowIndex,ggCustomersFond,ggDietTaboos);
        productionManageClient.setDetailAddress(detailAddress);

        if (StringUtils.isNotBlank(productLevelName) && StringUtils.isNotBlank(productSpecName)){
            ProductInfo productInfo = orderImportCommonService.getOrAddProductInfo(productId, productLevelName, productSpecName, "6个/箱");
            orderProduct.setProductSpecCode(productInfo.getSpecificationId());
            orderProduct.setProductSpecName(productInfo.getSpecificationName());
            orderProduct.setProductLevelName(productInfo.getSpecificationName());
            orderProduct.setProductLevelCode(productInfo.getLevelSpecificationId());
            orderProduct.setProductLevelName(productInfo.getLevelSpecificationName());
        }
        BigDecimal orderMoney =new BigDecimal(0);
        if (orderType == OrderTypeEnum.WEIGHT.getStatus()) {
            productionManageOrder.setOrderWeight(orderWeight);
            orderMoney = new BigDecimal(weightPrice).multiply(new BigDecimal(orderWeight)).setScale(2, BigDecimal.ROUND_HALF_UP);
            //设置订单产品属性
            orderProduct.setOrderWeight(orderWeight);
            orderProduct.setUnitPrice(weightPrice.toString());
        } else {
            int productBoxNumint = 0;
            if (orderType==OrderTypeEnum.NUM.getStatus()){
                int productNumInt = Double.valueOf(productNum).intValue();
                productionManageOrder.setOrderProductNum(productNumInt);
                orderProduct.setProductNum(productNumInt);
                orderMoney=new BigDecimal(numPrice).multiply(new BigDecimal(Double.valueOf(productNumInt))).setScale(2, BigDecimal.ROUND_HALF_UP);
                orderProduct.setUnitPrice(numPrice.toString());
                productionManageOrder.setOrderQuantity(productBoxNumint);
            }else if (orderType==OrderTypeEnum.BOX_NUN.getStatus()){
                productBoxNumint=productBoxNum;
                orderMoney=new BigDecimal(productBoxNumPrice).multiply(new BigDecimal(Double.valueOf(productBoxNumint))).setScale(2, BigDecimal.ROUND_HALF_UP);
                orderProduct.setUnitPrice(productBoxNumPrice.toString());
                orderProduct.setOrderQuantity(productBoxNumint);
                productionManageOrder.setOrderQuantity(productBoxNumint);
            }else if (orderType==OrderTypeEnum.PORTION.getStatus()){
                orderMoney=new BigDecimal(ggPartionPrice).multiply(new BigDecimal(Double.valueOf(ggPartionNum))).setScale(2, BigDecimal.ROUND_HALF_UP);
                orderProduct.setUnitPrice(ggPartionPrice.toString());
                orderProduct.setGgPartionNum(ggPartionNum);
                orderProduct.setOrderWeight(orderWeight);
                productionManageOrder.setGgTotalPartionNum(ggPartionNum);
                productionManageOrder.setOrderWeight(orderWeight);
            }
        }
        if (StringUtils.isNotBlank(benefitPrice) ){
            if (!CommonUtil.isNumber(benefitPrice)){
                CommonUtil.throwSuperCodeExtException(500,"第"+rowIndex+"行优惠金额非法，请填写数字");
            }
            BigDecimal benefitPriceBigDecimal = new BigDecimal(benefitPrice).setScale(2,BigDecimal.ROUND_DOWN);
            orderProduct.setBenefitPrice(benefitPriceBigDecimal.toString());
            productionManageOrder.setTotalBenefitPrice(benefitPriceBigDecimal.toString());
            orderMoney=CommonUtil.bigDecimalSub(orderMoney,benefitPriceBigDecimal);
        }
        orderProduct.setTotalPrice(orderMoney);
        productionManageOrder.setOrderMoney(orderMoney.toString());

        if ("本部发货".equals(deliveryType)) {
            productionManageOrder.setDeliveryType((byte) 0);
        } else {
            productionManageOrder.setDeliveryType((byte) 1);
        }
        productionManageOrder.setOrderType((byte) orderType);
        //订单状态设置
        productionManageOrder.setOutboundStatus(OrderOutBoundStatusEnum.UN_DELIVEY.getStatus());
        productionManageOrder.setSourceFrom(1);
        productionManageOrder.setOrderStatus((byte) OrderStatusEnum.UN_DELIVEY.getStatus());
        productionManageOrder.setRejectStatus(-1);
        productionManageOrder.setDoneDate(deliveryDate);
        productionManageOrder.setDeliveryDate(deliveryDate);
        productionManageOrder.setOutboundNum(1);
        productionManageOrder.setOrderRemark(mark);
        //客户及客户类目
        productionManageOrder.setClientId(productionManageClient.getId());
        productionManageOrder.setClientCategoryId(categoryId);
        productionManageOrder.setClientName(clientName);
        productionManageOrder.setClientContactMan(contactMan);
        productionManageOrder.setClientCategoryName(clientCategoryName);
        productionManageOrder.setClientContactPhone(contactPhone);

        //地址设置
        addressSet(productionManageOrder, address, detailAddress,productionManageClient,rowIndex);
        productionManageOrder.setClientDetailAddress(detailAddress);
        //封装创建人组织等信息
        Employee employee = commonUtil.getEmployee();
        productionManageOrder.setCreateUserId(employee.getEmployeeId());
        productionManageOrder.setCreateUserName(employee.getName());
        productionManageOrder.setOrganizationId(organizationId);
        productionManageOrder.setSysId(sysId);
        productionManageOrder.setProductName(productName);
        productionManageOrder.setClientId(productionManageClient.getId());
        productionManageOrder.setOrderDate(orderDate);
        productionManageOrder.setOrderRemark(mark);

        productionManageOrder.setGgDietTaboos(productionManageClient.getGgDietTaboos());
        productionManageOrder.setGgCustomersFond(productionManageClient.getGgCustomersFond());
        productionManageOrder.setGgContactManId(productionManageClient.getGgContactManId());

        productionManageOrder.setDepartmentId(null==saleUser.getDepartmentId()?"":saleUser.getDepartmentId());
        //订单编号
        if (StringUtils.isBlank(orderNo)){
            orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
            orderQueryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
            Integer count = orderMapper.selectCount(orderQueryWrapper);
            while (null != count && count > 0) {
                orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
                orderQueryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
                count = orderMapper.selectCount(orderQueryWrapper);
            }
        }else {
            QueryWrapper<ProductionManageOrder> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
            queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
            if (StringUtils.isNotBlank(orderNo)) {
                queryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
                Integer count=orderMapper.selectCount(queryWrapper);
                if (null!=count && count>0) {
                    CommonUtil.throwSuperCodeExtException(500,"第"+rowIndex+"行订单号"+orderNo+"已存在");
                }
            }
        }
        productionManageOrder.setOrderNo(orderNo);
        //设置销售人员
        productionManageOrder.setSaleRealUserId(saleUser.getUserId());
        productionManageOrder.setSaleUserId(saleUser.getEmployeeId());
        productionManageOrder.setSaleUserName(saleUser.getName());

        //订单产品构建
        orderProduct.setOrderId(productionManageOrder.getId());
        orderProduct.setProductId(productId);
        orderProduct.setProductName(productName);

        if (importType==2){
            productionManageOrder.setOutboundStatus(OrderOutBoundStatusEnum.ALL_DELIVEY.getStatus());
            productionManageOrder.setOrderStatus((byte)OrderStatusEnum.UN_RECEIPT.getStatus());
            
            String expressCo = outerOrderImportExcelHead.getExpressCo();
            String expressNo = outerOrderImportExcelHead.getExpressNo();
            if (StringUtils.isBlank(expressCo)||StringUtils.isBlank(expressNo)){
                CommonUtil.throwSuperCodeExtException(500, "请检查第"+rowIndex+"行快递单号或快递公司列，不可为空");
            }

            ProductionManageOutboundDeliveryWay deliveryWay = new ProductionManageOutboundDeliveryWay();
            deliveryWay.setCreateDate(new Date());
            deliveryWay.setCreateUserId(employee.getEmployeeId());
            deliveryWay.setCreateUserName(employee.getName());
            deliveryWay.setDeliveryWay(DeliveryWayEnum.EXPRESS.getKey());
            StringBuilder expressNoBuilder=new StringBuilder();
            StringBuilder expressCoBuilder=new StringBuilder();
            String[] expressNo_arr = expressNo.split("/");
            for (String exp : expressNo_arr) {
                expressNoBuilder.append(exp).append("&");
                expressCoBuilder.append(expressCo).append("&");
            }
            deliveryWay.setExpressCo(expressCoBuilder.substring(0,expressCoBuilder.length()-1));
            deliveryWay.setExpressNo(expressNoBuilder.substring(0,expressNoBuilder.length()-1));
            Map<Integer, ProductionManageOutboundDeliveryWay> deliveryWayHashMap = new HashMap<>();
            deliveryWayHashMap.put(rowIndex,deliveryWay);
            data.put("deliveryWay", deliveryWayHashMap);
        }

        orderMap.put(rowIndex, productionManageOrder);
        orderProductHashMap.put(rowIndex, orderProduct);
        data.put("order", orderMap);
        data.put("product", orderProductHashMap);
        data.put("client",productionManageClient);
        return data;
    }

    private void addressSet(ProductionManageOrder productionManageOrder, String address, String detailAddress, ProductionManageClientMO productionManageClient, int rowIndex) {
        if (productionManageClient.isJustCreateRightNow()){
            productionManageOrder.setClientAreaCode(productionManageClient.getAreaCode());
            String clientMiddleAddress =address;
            productionManageOrder.setClientMiddleAddress(clientMiddleAddress);
            String clientAddress = clientMiddleAddress;
            if (StringUtils.isNotBlank(detailAddress)) {
                clientAddress = clientMiddleAddress + detailAddress;
            }
            productionManageOrder.setClientAddress(clientAddress);
        }else {
            try {
                AreaUtil.AreaBean areaCodeFromBase = commonService.getAreaCodeFromBase(address);
                if (null==areaCodeFromBase){
                    areaCodeFromBase=commonService.getAreaCodeFromGaoDe(address);
                    commonService.syncBaseAdministrativeCode(areaCodeFromBase);
                }
                String areaCode = orderImportCommonService.getAreaCode(areaCodeFromBase.getCounty(), areaCodeFromBase.getCity(), areaCodeFromBase.getProvince());
                productionManageOrder.setClientAreaCode(areaCode);
                String clientMiddleAddress = address;
                productionManageOrder.setClientMiddleAddress(clientMiddleAddress);
                String clientAddress = clientMiddleAddress;
                if (StringUtils.isNotBlank(detailAddress)) {
                    clientAddress = clientMiddleAddress + detailAddress;
                }
                productionManageOrder.setClientAddress(clientAddress);
            } catch (Exception e) {
                CommonUtil.throwSuperCodeExtException(500, "第"+rowIndex+"行所在地解析失败，请填写正确的省市区,"+e.getLocalizedMessage());
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchImportOrder(Map<Integer, ProductionManageOrder> orderMap, Map<Integer, ProductionManageOrderProduct> orderProductHashMap, Map<Integer, ProductionManageOutboundDeliveryWay> deliveryWayHashMap, List<ProductionManageClientMO> clientMOList, Integer importType) throws SuperCodeException {
        Map<String, Integer> orderNoMap=new HashMap<>();
        if (!orderMap.isEmpty()) {
            for (Integer key : orderMap.keySet()) {
                ProductionManageOrder order = orderMap.get(key);
                Integer noCount = orderNoMap.get(order.getOrderNo());
                if (null==noCount){
                    orderNoMap.put(order.getOrderNo(),1);
                }else {
                    CommonUtil.throwSuperCodeExtException(500,"重复订单号--"+order.getOrderNo());
                }

                ProductionManageOrderProduct orderProduct = orderProductHashMap.get(key);
                orderMapper.insert(order);
                Long orderId = order.getId();
                orderProduct.setOrderId(orderId);
                orderProductMapper.insert(orderProduct);
                if (importType==2){
                    Long outboundId = outboundService.add(orderId);
                    ProductionManageOutboundDeliveryWay productionManageOutboundDeliveryWay = deliveryWayHashMap.get(key);
                    if (null!=productionManageOutboundDeliveryWay){
                        productionManageOutboundDeliveryWay.setOrderId(orderId);
                        productionManageOutboundDeliveryWay.setOutboundId(outboundId);
                        outboundDeliveryWayMapper.insert(productionManageOutboundDeliveryWay);
                    }
                }
            }
            //更新销售人员潜在客户订单客户统计表
            Map<Long, Long> handledMap=new HashMap<>();
            for (ProductionManageClientMO productionManageClientMO : clientMOList) {
                if (productionManageClientMO.isJustCreateRightNow() || (null!=productionManageClientMO.getClientType() && productionManageClientMO.getClientType()==0)){
                    Long clientId = productionManageClientMO.getId();
                    Long existClientId = handledMap.get(clientId);
                    if (null==existClientId){
                        saleClientNumStatisticsService.updateOrderClientNum(productionManageClientMO.getSaleUserId(),1);
                        clientService.updateClientToRealClient(clientId);
                        handledMap.put(clientId,clientId);
                    }
                }
            }
        }
    }

    /**
     * 关闭资源
     */
    public void close() {
        orderImportCommonService.close();
    }

    /**
     * 返回订单类型
     *
     * @param rowIndex
     * @param orderWeight
     * @param weightPrice
     * @param productBoxNum
     * @param productBoxNumPrice
     * @param productNum
     * @param numPrice
     * @param ggPartionNum
     * @param ggPartionPrice
     * @return
     */
    private int checkWeigHTOrNum(int rowIndex, Double orderWeight, Double weightPrice, Integer productBoxNum, Double productBoxNumPrice, Integer productNum, Double numPrice, Integer ggPartionNum, Double ggPartionPrice) {
        NoNullArrayList<Double> weightArrayList = new NoNullArrayList();
        weightArrayList.add(orderWeight);
        weightArrayList.add(weightPrice);

        NoNullArrayList<Object> numArrayList = new NoNullArrayList();
        numArrayList.add(productNum);
        numArrayList.add(numPrice);

        NoNullArrayList<Object> boxNumArrayList = new NoNullArrayList();
        boxNumArrayList.add(productBoxNum);
        boxNumArrayList.add(productBoxNumPrice);

        NoNullArrayList<Object> ggPartionNumArrayList = new NoNullArrayList();
        ggPartionNumArrayList.add(ggPartionNum);
        ggPartionNumArrayList.add(ggPartionPrice);
        ggPartionNumArrayList.add(orderWeight);

        int orderType = 0;
        int count=0;
        if (numArrayList.size()==2){
            count++;
            orderType= OrderTypeEnum.NUM.getStatus();
        }
        if (boxNumArrayList.size()==2){
            count++;
            orderType=  OrderTypeEnum.BOX_NUN.getStatus();
        }
        if (weightArrayList.size()==2){
            count++;
            orderType=  OrderTypeEnum.WEIGHT.getStatus();
        }
        if (ggPartionNumArrayList.size()==3){
            count++;
            orderType=  OrderTypeEnum.PORTION.getStatus();
        }
        if (count>1 || count<1){
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行重量和数量及箱数及其单价只能选一组填写");
        }
        return orderType;
    }

    /**
     * 校验非空行
     *
     * @param rowIndex
     * @param clientCategoryName
     * @param clientName
     * @param contactMan
     * @param contactPhone
     * @param saleMan
     * @param address
     * @param deliveryDate
     * @param deliveryType
     * @param productName
     * @throws SuperCodeExtException
     */
    private static void checkParams(int rowIndex, String clientCategoryName, String clientName, String contactMan, String contactPhone, String saleMan, String address,
                                    Date deliveryDate, String deliveryType,
                                    String productName) throws SuperCodeExtException {
        if (StringUtils.isBlank(clientCategoryName)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行客户类目有空值");
        }

        if (StringUtils.isBlank(clientName)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行客户名称有空值");
        }

        if (StringUtils.isBlank(contactMan)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行联系人有空值");
        }
        if (StringUtils.isBlank(contactPhone)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行联系电话有空值");
        }

        if (StringUtils.isBlank(address)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行所在地有空值");
        }

        if (null == deliveryDate) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行发货日期有空值");
        }

        if (StringUtils.isBlank(deliveryType)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行发货类型有空值");
        } else if (!("本部发货".equals(deliveryType) || "外部发货".equals(deliveryType))) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行发货类型只能为 ‘本部发货’ 或 ‘外部发货’");
        }

        if (StringUtils.isBlank(saleMan)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行销售人员有空值");
        }

        if (StringUtils.isBlank(productName)) {
            CommonUtil.throwSuperCodeExtException(500, "第" + rowIndex + "行产品名称有空值");
        }
    }
}
