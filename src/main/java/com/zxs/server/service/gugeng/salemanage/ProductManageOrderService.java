package com.zxs.server.service.gugeng.salemanage;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.ProductManageConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.HistoryOrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.OrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.model.excel.OuterOrderImportExcelHead;
import net.app315.hydra.intelligent.planting.common.gugeng.model.storagemanage.OutboundDeliveryExpress;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.ExcelImportDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.EmployeeMsgDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryWayEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsSaleTargetData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.server.context.SpringContext;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ClientCategoryMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageOrderMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageOrderProductDataStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageSaleClientNumStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundDeliveryWayService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageOutboundPackageMessageService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageProductRecordService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageStockService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.wechat.ProductionManageWechatMessageService;
import net.app315.hydra.intelligent.planting.vo.gugeng.salemanage.*;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.ProductionManageOutboundDeliveryWayVO;
import net.app315.hydra.user.data.auth.sdk.component.RoleDataAuthComponent;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorEmployee;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.EofSensorInputStream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.ORDER_MANAGE;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ORGANIZATION_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_SYS_ID;


@Slf4j
@Service
public class ProductManageOrderService extends ServiceImpl<BaseMapper<ProductionManageOrder>, ProductionManageOrder> {

    @Autowired
    private ProductManageOrderMapper dao;

    @Autowired
    private ProductManageOrderProductService orderProductService;

    @Autowired
    private ProductManageClientService clientService;

    @Autowired
    private ClientCategoryMapper categoryMapper;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator serialNumberGenerator;

    @Autowired
    private ProductionManageOutboundPackageMessageService outboundPackageMessageService;

    @Autowired
    private ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private ProductionManageOutboundDeliveryWayService outboundDeliveryWayService;

    @Autowired
    private MessageInformService informService;

    @Autowired
    private ProductionManageOrderProductReceivedService productReceivedService;

    @Autowired
    private ProductionManageOrderProductReturnService productReturnService;

    @Autowired
    private HistoryOrderImportService historyOrderImportService;

    @Autowired
    private OrderImportService orderImportService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ProductionManageProductRecordService recordService;

    @Autowired
    private ProductionManageSaleClientNumStatisticsService saleClientNumStatisticsService;

    @Autowired
    private ProductionManageOrderProductDataStatisticsService orderProductDataStatisticsService;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageStockService stockService;

    @Value("${qiniu.upload.domain}")
    private String OSS_DOMAIN;

    @Autowired
    private ProductionManageWechatMessageService messageService;

    @Autowired
    private GuGengContactManService gengContactManService;

    @Autowired
    private RoleDataAuthComponent authComponent;

    /**
     * 1订单号为空则自动生成
     * 2订单金额或者数量后台需要重新计算
     * 3参数校验
     *
     * @param pOrderDTO
     * @throws SuperCodeException
     * @throws ParseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ProductManageOrderDTO pOrderDTO) throws SuperCodeException, ParseException {
        List<ProductManageOrderProductDTO> productDTOs = pOrderDTO.getProOrderProductDTOs();
        ProductManageClientDTO clientDTO = pOrderDTO.getPclientDTO();
        if (null == productDTOs || productDTOs.isEmpty()) {
            throw new SuperCodeException("订单产品不能为空", 500);
        }
        if (null == clientDTO) {
            throw new SuperCodeException("客户信息不能为空", 500);
        }
        Byte OrderType = pOrderDTO.getOrderType();
        if (null == OrderType) {
            throw new SuperCodeException("订单类型不能为空", 500);
        }

        ProductionManageClient pClient = null;
        Long clientId = clientDTO.getId();
        if (null == clientId) {
            clientDTO.setSaleUserId(pOrderDTO.getSaleUserId());
            clientDTO.setSaleUserName(pOrderDTO.getSaleUserName());
            pClient = clientService.orderAddOrReturn(clientDTO);
            clientId = pClient.getId();
            clientDTO.setId(clientId);
        } else {
            pClient = clientService.selectById(clientId);
        }
        if (null == pClient) {
            CommonUtil.throwSuperCodeExtException(500, "客户不存在");
        }

        Long ggContactManId = clientDTO.getGgContactManId();
        if (null == ggContactManId) {
            GuGengContactMan guGengContactMan = gengContactManService.addContactManFormOrder(clientId, clientDTO.getAreaCode(), clientDTO.getDetailAddress(), clientDTO.getContactMan(), clientDTO.getContactPhone());
            ggContactManId=guGengContactMan.getId();
        }
        String orderNo = pOrderDTO.getOrderNo();
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        QueryWrapper<ProductionManageOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
        queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
        if (StringUtils.isNotBlank(orderNo)) {
            queryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
            Integer count = dao.selectCount(queryWrapper);
            if (null != count && count > 0) {
                throw new SuperCodeException("该订单号已存在", 500);
            }

        } else {
            orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
            queryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
            Integer count = dao.selectCount(queryWrapper);
            while (null != count && count > 0) {
                orderNo = serialNumberGenerator.getSerialNumber(6, RedisKey.SALE_ORDER_NO_KEY, organizationId + sysId, CommonUtil.getSecondsNextEarlyMorning());
                queryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
                count = dao.selectCount(queryWrapper);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //如果客户id不存在则新增客户
        ProductionManageOrder pOrder = new ProductionManageOrder();

        pOrder.setClientAreaCode(clientDTO.getAreaCode());
        pOrder.setClientContactPhone(clientDTO.getContactPhone());
        pOrder.setClientContactMan(clientDTO.getContactMan());
        try {
            pOrder.setClientCategoryName(categoryMapper.selectById(clientDTO.getCategoryId()).getCategoryName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        pOrder.setClientCategoryId(clientDTO.getCategoryId());
        String clientMiddleAddress = commonUtil.getAddressWithAreaCode(clientDTO.getAreaCode());
        pOrder.setClientMiddleAddress(clientMiddleAddress);
        String clientAddress = clientMiddleAddress;
        if (StringUtils.isNotBlank(clientDTO.getDetailAddress())) {
            clientAddress = clientMiddleAddress + clientDTO.getDetailAddress();
        }
        InterceptorEmployee employee = commonUtil.getEmployeeInfo();
        Date currentDate = CommonUtil.getCurrentDate("yyyy-MM-dd HH:mm:ss");
        pOrder.setSourceFrom(0);
        pOrder.setGgContactManId(ggContactManId);
        pOrder.setSaleRealUserId(pOrderDTO.getSaleRealUserId());
        pOrder.setClientDetailAddress(clientDTO.getDetailAddress());
        pOrder.setClientAddress(clientAddress);
        pOrder.setClientName(clientDTO.getClientName());
        pOrder.setDeliveryType(pOrderDTO.getDeliveryType());
        pOrder.setDeliveryDate(pOrderDTO.getDeliveryDate());
        pOrder.setReceivedEditStatus((byte) 0);
        pOrder.setClientId(clientId);
        pOrder.setCreateUserId(employee.getEmployeeId());
        pOrder.setCreateUserName(employee.getEmployeeName());
        pOrder.setAuthDepartmentId(employee.getDepartmentId());
        pOrder.setDeliveryDate(pOrderDTO.getDeliveryDate());
        pOrder.setOrderStatus((byte) OrderStatusEnum.UN_DELIVEY.getStatus());
        pOrder.setOrderMoney(pOrderDTO.getOrderMoney());
        pOrder.setOrderNo(orderNo);
        pOrder.setOrderQuantity(pOrderDTO.getOrderQuantity());
        pOrder.setOrderRemark(pOrderDTO.getOrderRemark());
        pOrder.setOrderType(pOrderDTO.getOrderType());
        pOrder.setOrderWeight(pOrderDTO.getOrderWeight());
        pOrder.setSaleUserId(pOrderDTO.getSaleUserId());
        pOrder.setSaleUserName(pOrderDTO.getSaleUserName());
        pOrder.setOrganizationId(commonUtil.getOrganizationId());
        pOrder.setSysId(commonUtil.getSysId());
        pOrder.setOrderDate(currentDate);
        pOrder.setOutboundStatus(1);
        //设置古耕客户饮食禁忌及客户喜好
        pOrder.setGgDietTaboos(clientDTO.getGgDietTaboos());
        pOrder.setGgCustomersFond(clientDTO.getGgCustomersFond());
        String orderMoney = pOrderDTO.getOrderMoney();
        Double orderWeight = pOrderDTO.getOrderWeight();
        if (StringUtils.isNotBlank(orderMoney)) {
            pOrder.setOrderMoney(new BigDecimal(orderMoney).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
        if (null != orderWeight) {
            pOrder.setOrderWeight(new BigDecimal(orderWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        pOrder.setReceivedOrderQuantity(pOrderDTO.getOrderQuantity());
        List<ProductionManageOrderProduct> prOrderProducts = productDTOsToOrderProduct(pOrderDTO, pOrder);

        // v1.8设置部门id
        String saleUserId = pOrderDTO.getSaleUserId();
        EmployeeMsgDTO saleUserMsg = commonUtil.getEmployeeMsg(commonUtil.getSuperToken(), saleUserId);
        String departmentId = Objects.isNull(saleUserMsg) ? StringUtils.EMPTY : saleUserMsg.getDepartmentId();
        pOrder.setDepartmentId(departmentId);

        pOrder.setGgTotalPartionNum(pOrderDTO.getGgTotalPartionNum());
        dao.insert(pOrder);
        for (ProductionManageOrderProduct productManageOrderProduct : prOrderProducts) {
            productManageOrderProduct.setOrderId(pOrder.getId());
        }
        orderProductService.saveBatch(prOrderProducts);

        //新增潜在及订单客户转化率统计
        if (null == pClient.getClientType() || pClient.getClientType() == 0) {
            saleClientNumStatisticsService.updateOrderClientNum(clientDTO.getSaleUserId(), 1);
        }
        //更改客户类型为订单客户
        clientService.updateClientToRealClient(clientId);
        //更新订单统计信息
        orderProductDataStatisticsService.updateOrderMoney(prOrderProducts, currentDate);
        //发送待审核提示
//        messageService.createOrderMessage();
    }


    /**
     * 编辑时不可修改订单编号和订单状态
     *
     * @param pOrderDTO
     * @throws SuperCodeException
     * @throws ParseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductManageOrderDTO pOrderDTO) throws SuperCodeException, ParseException {
        List<ProductManageOrderProductDTO> productDTOs = pOrderDTO.getProOrderProductDTOs();
        ProductManageClientDTO clientDTO = pOrderDTO.getPclientDTO();

        Long orderId = pOrderDTO.getId();

        if (null == orderId) {
            throw new SuperCodeException("订单id不能为空", 500);
        }
        ProductionManageOrder existOrder = dao.selectById(orderId);
        if (null == existOrder) {
            throw new SuperCodeException("订单id不能为空", 500);
        }

        if (existOrder.getOrderStatus().intValue() != OrderStatusEnum.UN_DELIVEY.getStatus()) {
            throw new SuperCodeException("订单状态为待发货才可编辑", 500);
        }

        if (null == productDTOs || productDTOs.isEmpty()) {
            throw new SuperCodeException("订单产品不能为空", 500);
        }

        if (null == clientDTO) {
            throw new SuperCodeException("客户信息不能为空", 500);
        }

        Byte OrderType = pOrderDTO.getOrderType();
        if (null == OrderType) {
            throw new SuperCodeException("订单类型不能为空", 500);
        }
        String orderNo = pOrderDTO.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            throw new SuperCodeException("订单编号不能为空", 500);
        }
        //如果客户id不存在则新增客户
        Long clientId = clientDTO.getId();
        ProductionManageClient pClient = null;
        if (null == clientId) {
            clientDTO.setSaleUserId(pOrderDTO.getSaleUserId());
            clientDTO.setSaleUserName(pOrderDTO.getSaleUserName());
            pClient = clientService.orderAddOrReturn(clientDTO);
            clientId = pClient.getId();
            clientDTO.setId(clientId);
        } else {
            pClient = clientService.selectById(clientId);
        }

        if (null == pClient) {
            CommonUtil.throwSuperCodeExtException(500, "客户不存在");
        }

        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();
        Employee employee = commonUtil.getEmployee();

        QueryWrapper<ProductionManageOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductManageConstant.SYS_ORGANIZATIONID, organizationId);
        queryWrapper.eq(ProductManageConstant.SYS_SYSID, sysId);
        queryWrapper.eq(ProductionManageOrder.COL_ORDER_NO, orderNo);
        Integer count = dao.selectCount(queryWrapper);
        if (null == count || count == 0) {
            throw new SuperCodeException("该订单号不存在", 500);
        }
//        if (null != pOrderDTO.getRequestType() && pOrderDTO.getRequestType().intValue() == 2) {
//            existOrder.setVerifyStatus(0);
//        }
        existOrder.setClientAreaCode(clientDTO.getAreaCode());
        existOrder.setClientContactPhone(clientDTO.getContactPhone());
        existOrder.setClientDetailAddress(clientDTO.getDetailAddress());
        existOrder.setClientContactMan(clientDTO.getContactMan());
        try {
            existOrder.setClientCategoryName(categoryMapper.selectById(clientDTO.getCategoryId()).getCategoryName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        existOrder.setDeliveryType(pOrderDTO.getDeliveryType());
        existOrder.setClientCategoryId(clientDTO.getCategoryId());
        existOrder.setClientName(clientDTO.getClientName());
        String clientMiddleAddress = commonUtil.getAddressWithAreaCode(clientDTO.getAreaCode());
        existOrder.setClientMiddleAddress(clientMiddleAddress);
        String clientAddress = clientMiddleAddress;
        if (StringUtils.isNotBlank(clientDTO.getDetailAddress())) {
            clientAddress = clientAddress + clientDTO.getDetailAddress();
        }
        existOrder.setClientAddress(clientAddress);
        existOrder.setClientDetailAddress(clientDTO.getDetailAddress());
        existOrder.setSaleRealUserId(pOrderDTO.getSaleRealUserId());
        existOrder.setDeliveryDate(pOrderDTO.getDeliveryDate());
        existOrder.setClientId(clientId);
        existOrder.setDeliveryDate(pOrderDTO.getDeliveryDate());
        existOrder.setOrderQuantity(pOrderDTO.getOrderQuantity());
        existOrder.setOrderRemark(pOrderDTO.getOrderRemark());
        existOrder.setOrderType(pOrderDTO.getOrderType());
        String orderMoney = pOrderDTO.getOrderMoney();
        Double orderWeight = pOrderDTO.getOrderWeight();
        if (StringUtils.isNotBlank(orderMoney)) {
            existOrder.setOrderMoney(new BigDecimal(orderMoney).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
        if (null != orderWeight) {
            existOrder.setOrderWeight(new BigDecimal(orderWeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        existOrder.setUpdateUserId(employee.getEmployeeId());
        existOrder.setUpdateUserName(employee.getName());
        existOrder.setSaleUserId(pOrderDTO.getSaleUserId());
        existOrder.setSaleUserName(pOrderDTO.getSaleUserName());
        existOrder.setUpdateDate(new Date());

        // v1.8新加内容订单中插入销售人员所属的部门信息
        existOrder.setDepartmentId(getDepartmentId(pOrderDTO.getSaleUserId()));

        //设置古耕客户饮食禁忌及客户喜好
        existOrder.setGgDietTaboos(clientDTO.getGgDietTaboos());
        existOrder.setGgCustomersFond(clientDTO.getGgCustomersFond());
        List<ProductionManageOrderProduct> prOrderProducts = productDTOsToOrderProduct(pOrderDTO, existOrder);

        List<Long> deleteOrderProjectIds = pOrderDTO.getDeleteOrderProjectIds();
        /**
         * 新增deleteOrderProjectIds参数，如果不为空则只删除被删除的部分，而不全部删除全部新增，
         */
        if (CollectionUtils.isEmpty(deleteOrderProjectIds)){
            ListIterator<ProductionManageOrderProduct> listIterator = prOrderProducts.listIterator();
            //拆分新增和更新的订单
            while (listIterator.hasNext()) {
                ProductionManageOrderProduct productManageOrderProduct = listIterator.next();
                productManageOrderProduct.setOrderId(orderId);
                productManageOrderProduct.setId(null);
            }
            orderProductService.deleteByOrderId(orderId);
            existOrder.setGgTotalPartionNum(pOrderDTO.getGgTotalPartionNum());
            dao.updateById(existOrder);
            if (!prOrderProducts.isEmpty()) {
                orderProductService.saveBatch(prOrderProducts);
            }
        }else{
            orderProductService.saveOrUpdateBatch(prOrderProducts);
            orderProductService.deleteByIds(deleteOrderProjectIds);
        }
        //新增潜在及订单客户转化率统计
        if (null == pClient.getClientType() || pClient.getClientType() == 0) {
            saleClientNumStatisticsService.updateOrderClientNum(clientDTO.getSaleUserId(), 1);
        }
        //重新统计销售产品
        orderProductDataStatisticsService.reStatistics(prOrderProducts, existOrder.getOrderDate());
    }

    public PageResults<List<ProductManageOrderListVO>> page(ProductManageOrderListDTO orderListDTO) throws Exception {
        orderListDTO.setSysId(commonUtil.getSysId());
        orderListDTO.setOrganizationId(commonUtil.getOrganizationId());
        Page<ProductManageOrderListVO> page = new Page<>(orderListDTO.getDefaultCurrent(), orderListDTO.getDefaultPageSize());
        // 处理交货日期

        String[] dateInterval = LocalDateTimeUtil.substringDate(orderListDTO.getDeliveryDate());
        String[] orderDateInterval = LocalDateTimeUtil.substringDate(orderListDTO.getOrderDate());
        if (null != orderDateInterval && orderDateInterval.length == 2) {
            if (StringUtils.isNotBlank(orderDateInterval[0])) {
                orderListDTO.setStartOrderDate(orderDateInterval[0]);
            }
            if (StringUtils.isNotBlank(orderDateInterval[1])) {
                orderListDTO.setEndOrderDate(orderDateInterval[1]);
            }
        }
        if (null != dateInterval && dateInterval.length == 2) {
            if (StringUtils.isNotBlank(dateInterval[0])) {
                orderListDTO.setStartDeliveryDate(dateInterval[0]);
            }
            if (StringUtils.isNotBlank(dateInterval[1])) {
                orderListDTO.setEndDeliveryDate(dateInterval[1]);
            }
        }
        InterceptorEmployee employeeInfo = commonUtil.getEmployeeInfo();
        String sql = authComponent.getRoleFunAuthSqlByAuthCode(ORDER_MANAGE, "o.CreateUserId"+ProductionManageOrder.COL_CREATE_USER_ID, employeeInfo.getEmployeeId(), "o.AuthDepartmentId");
        if (StringUtils.isNotBlank(sql)){
            StringBuilder builder=new StringBuilder();
            String replaceAnd = sql.replace("and", "");
            builder.append(" and (").append(replaceAnd).append(" or ").append("o."+ProductionManageOrder.COL_SALE_USER_ID+"='").append(employeeInfo.getEmployeeId()).append("')");
            sql=builder.toString();
            log.info("获取订单最终的数据权限sql:"+sql);
        }
        IPage<ProductManageOrderListVO> iPage = dao.pageList(page, orderListDTO,sql);
        return CommonUtil.iPageToPageResults(iPage, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirm(ProductionManageOrderProductReceivedDTO productReceivedDTO) throws SuperCodeException, ParseException {
        Long orderId = productReceivedDTO.getOrderId();
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("Id", orderId);
        columnMap.put(OLD_SYS_ID, commonUtil.getSysId());
        columnMap.put(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId());
        List<ProductionManageOrder> pOrders = dao.selectByMap(columnMap);
        if (null == pOrders || pOrders.isEmpty()) {
            throw new SuperCodeException("该订单不存在", 500);
        }
        ProductionManageOrder pOrder = pOrders.get(0);
        if (!(pOrder.getOrderStatus().intValue() == OrderStatusEnum.UN_RECEIPT.getStatus() || pOrder.getOrderStatus().intValue() == OrderStatusEnum.CONFIRMED.getStatus())) {
            throw new SuperCodeException("该订单不是待收货状态无法完成", 500);
        }
        List<ProductionManageOrderProductReceived> productReceiveds = productReceivedDTO.getOrderProductReceivedList();
        if (null == productReceiveds || productReceiveds.isEmpty()) {
            throw new SuperCodeException("实收数据不能为空", 500);
        }
        BigDecimal receivedOrderMoney = new BigDecimal(0);
        Integer receivedOrderQuantity = null;
        BigDecimal receivedTotalBenefitPrice = new BigDecimal(0);
        BigDecimal receivedOrderWeight = new BigDecimal(0);

        Integer receivedBatchNum=productReceivedService.getMaxReceivedBatchNum(orderId);
        if (null==receivedBatchNum){
            receivedBatchNum=0;
        }
        receivedBatchNum++;
        for (ProductionManageOrderProductReceived productReceived : productReceiveds) {
            productReceived.setOrderId(orderId);
            if (StringUtils.isNotBlank(productReceived.getReceivedBenefitPrice())) {
                receivedTotalBenefitPrice = CommonUtil.bigDecimalAdd(receivedTotalBenefitPrice, new BigDecimal(productReceived.getReceivedBenefitPrice()));
            }
            if (StringUtils.isNotBlank(productReceived.getReceivedProMoney())) {
                receivedOrderMoney = CommonUtil.bigDecimalAdd(receivedOrderMoney, new BigDecimal(productReceived.getReceivedProMoney()));
            }
            if (null != productReceived.getReceivedProWeight()) {
                receivedOrderWeight = CommonUtil.bigDecimalAdd(receivedOrderWeight, new BigDecimal(productReceived.getReceivedProWeight()));
            }
            if (null != productReceived.getReceivedProQuantity()) {
                receivedOrderQuantity = CommonUtil.integerAdd(receivedOrderQuantity, productReceived.getReceivedProQuantity());
            }
            productReceived.setReceivedBatchNum(receivedBatchNum);
        }

        productReceivedService.saveOrUpdateBatch(productReceiveds);
        //更新销售产品数据统计
        orderProductDataStatisticsService.updateReceivedOrderMoney(productReceiveds, pOrder.getOrderDate());

        pOrder.setActualReceivedMark(productReceivedDTO.getActualReceivedMark());
        pOrder.setGgTotalPartionNum(productReceivedDTO.getReceivedGgPartionNum());
        pOrder.setOrderStatus((byte) OrderStatusEnum.UN_RECEIPT.getStatus());
        pOrder.setReceivedOrderWeight(receivedOrderWeight.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        pOrder.setReceivedOrderQuantity(receivedOrderQuantity);
        pOrder.setReceivedOrderMoney(receivedOrderMoney.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        pOrder.setReceivedTotalBenefitPrice(receivedTotalBenefitPrice.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        dao.updateById(pOrder);
    }

    public ProductManageOrderDetailVO detail(Long orderId) throws SuperCodeException {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("Id", orderId);
        columnMap.put(ProductManageConstant.SYS_SYSID, commonUtil.getSysId());
        columnMap.put(ProductManageConstant.SYS_ORGANIZATIONID, commonUtil.getOrganizationId());
        List<ProductionManageOrder> pOrders = dao.selectByMap(columnMap);
        if (null == pOrders || pOrders.isEmpty()) {
            throw new SuperCodeException("该订单不存在", 500);
        }
        ProductionManageOrder pOrder = pOrders.get(0);
        ProductManageOrderDetailVO pOrderDetailVO = new ProductManageOrderDetailVO();
        BeanUtils.copyProperties(pOrder, pOrderDetailVO);
        pOrderDetailVO.setReceivedGgPartionNum(pOrder.getGgTotalPartionNum());
        Map<String, Object> columnMap2 = new HashMap<>();
        columnMap2.put(ProductionManageOrderProduct.COL_ORDER_ID, orderId);
        List<ProductionManageOrderProduct> pOrderProducts = orderProductService.selectByMap(columnMap2);
        List<ProductionManageOrderProductDetailListVO> orderProductDetails = new ArrayList<>();
        BigDecimal totalBenefitPrice = new BigDecimal(0);
        if (null != pOrderProducts && !pOrderProducts.isEmpty()) {
            Map<String, ProductionManageOrderProductDetailListVO> tmpMap = new HashMap<>();
            for (ProductionManageOrderProduct productionManageOrderProduct : pOrderProducts) {
                String productId = productionManageOrderProduct.getProductId();
                ProductionManageOrderProductDetailListVO pDetailListVO = tmpMap.get(productId);
                String benefitPrice = productionManageOrderProduct.getBenefitPrice();
                if (StringUtils.isNotBlank(benefitPrice)) {
                    totalBenefitPrice = CommonUtil.bigDecimalAdd(totalBenefitPrice, new BigDecimal(benefitPrice));
                }
                if (null == pDetailListVO) {
                    pDetailListVO = new ProductionManageOrderProductDetailListVO();
                    pDetailListVO.setProductId(productId);
                    pDetailListVO.setProductName(productionManageOrderProduct.getProductName());
                    List<ProductionManageOrderProduct> pOrderProducts2 = new ArrayList<>();
                    pOrderProducts2.add(productionManageOrderProduct);
                    pDetailListVO.setOrderProducts(pOrderProducts2);
                    tmpMap.put(productId, pDetailListVO);
                } else {
                    pDetailListVO.getOrderProducts().add(productionManageOrderProduct);
                }
            }
            for (Entry<String, ProductionManageOrderProductDetailListVO> entry : tmpMap.entrySet()) {
                orderProductDetails.add(entry.getValue());
            }
        }
        pOrderDetailVO.setOrderProductDetails(orderProductDetails);
        pOrderDetailVO.setTotalBenefitPrice(totalBenefitPrice.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        //获取包装扫码信息
        setOrderDetailPackageMessage(orderId, pOrder, pOrderDetailVO);
        setStorageInfo(orderId, pOrderDetailVO);

        return pOrderDetailVO;
    }

    private void setOrderDetailPackageMessage(Long orderId, ProductionManageOrder pOrder, ProductManageOrderDetailVO pOrderDetailVO) throws SuperCodeException {
        List<ProductionManageOutboundPackageMessage> packageMessages = outboundPackageMessageService.selectByOrderId(orderId);
        List<List<ProductionManageOutboundPackageMessage>> packageMessagesList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(packageMessages)) {
            TreeMap<Integer, List<ProductionManageOutboundPackageMessage>> map = new TreeMap<>();
            for (ProductionManageOutboundPackageMessage outboundPackageMessage : packageMessages) {
                val outboundNum = outboundPackageMessage.getOutboundNum();
                if (null != outboundNum && outboundNum > 0) {
                    List<ProductionManageOutboundPackageMessage> outboundPackageMessages = map.get(outboundNum);
                    if (null == outboundPackageMessages) {
                        outboundPackageMessages = new ArrayList<>();
                    }
                    outboundPackageMessage.setNullToZero();
                    outboundPackageMessages.add(outboundPackageMessage);
                    map.put(outboundNum, outboundPackageMessages);
                }
            }
            for (int i = 1; i <= pOrder.getOutboundNum(); i++) {
                List<ProductionManageOutboundPackageMessage> outboundPackageMessages =
                        map.computeIfAbsent(i, k -> new ArrayList<>());
            }
            for (Integer key : map.keySet()) {
                List<ProductionManageOutboundPackageMessage> outboundPackageMessages = map.get(key);
                if (CollectionUtils.isNotEmpty(outboundPackageMessages)){
                    packageMessagesList.add(map.get(key));
                }
            }
        }
        if (packageMessagesList.isEmpty()){
            pOrderDetailVO.setPackageMessages(new ArrayList());
        }else {
            pOrderDetailVO.setPackageMessages(packageMessagesList);
        }
    }

    private void setStorageInfo(Long orderId, ProductManageOrderDetailVO pOrderDetailVO) {
        //获取发货方式信息
        List<ProductionManageOutboundDeliveryWay> outboundDeliveryWays = outboundDeliveryWayService.getDeliveryWaysByOrderId(orderId);
        List<ProductionManageOutboundDeliveryWayVO> outboundDeliveryWaysVO = new ArrayList<>();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(outboundDeliveryWays)) {
            Long outboundId = outboundDeliveryWays.get(0).getOutboundId();
            ProductionManageOutbound productionManageOutbound = outboundMapper.selectById(outboundId);
            for (ProductionManageOutboundDeliveryWay outboundDeliveryWay : outboundDeliveryWays) {
                ProductionManageOutboundDeliveryWayVO outboundDeliveryWayVO = new ProductionManageOutboundDeliveryWayVO();
                BeanUtils.copyProperties(outboundDeliveryWay, outboundDeliveryWayVO);
                if (DeliveryWayEnum.EXPRESS.getKey() == outboundDeliveryWay.getDeliveryWay()) {
                    List<OutboundDeliveryExpress> outboundDeliveryExpressList = outboundDeliveryWayService.getOutboundDeliveryExpressList(outboundDeliveryWay.getExpressNo(), outboundDeliveryWay.getExpressCo());
                    outboundDeliveryWayVO.setExpressList(outboundDeliveryExpressList);
                }
                //设置二次发货信息
                if (null!=productionManageOutbound){
                    outboundDeliveryWayVO.setSecDeliveryReason(productionManageOutbound.getSecDeliveryReason());
                    outboundDeliveryWayVO.setNeedSecDelivery(productionManageOutbound.getNeedSecDelivery());
                }
                outboundDeliveryWaysVO.add(outboundDeliveryWayVO);
            }
        }
        pOrderDetailVO.setOutboundDeliveryWay(outboundDeliveryWaysVO);

        //获取退货信息
        QueryWrapper<ProductionManageOrderProductReturn> returnQueryWrapper = new QueryWrapper<>();
        returnQueryWrapper.eq(ProductionManageOrderProductReturn.COL_ORDER_ID, orderId);
        List<ProductionManageOrderProductReturn> productReturnList = productReturnService.list(returnQueryWrapper);
        pOrderDetailVO.setReturnProductList(productReturnList);

        //获取实收信息
        QueryWrapper<ProductionManageOrderProductReceived> receivedQueryWrapper = new QueryWrapper<>();
        receivedQueryWrapper.eq(ProductionManageOrderProductReceived.COL_ORDER_ID, orderId);
        List<ProductionManageOrderProductReceived> receivedList = productReceivedService.list(receivedQueryWrapper);
        pOrderDetailVO.setOrderProductReceivedList(receivedList);
    }

    /**
     * pda订单详情使用
     *
     * @param orderId
     * @return
     * @throws SuperCodeException
     */
    public ProductManagePdaOrderDetailVO pdadetail(Long orderId) throws SuperCodeException {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("Id", orderId);
        columnMap.put(ProductManageConstant.SYS_SYSID, commonUtil.getSysId());
        columnMap.put(ProductManageConstant.SYS_ORGANIZATIONID, commonUtil.getOrganizationId());
        List<ProductionManageOrder> pOrders = dao.selectByMap(columnMap);
        if (null == pOrders || pOrders.isEmpty()) {
            throw new SuperCodeException("该订单不存在", 500);
        }
        ProductionManageOrder pOrder = pOrders.get(0);
        ProductManagePdaOrderDetailVO pOrderDetailVO = new ProductManagePdaOrderDetailVO();
        BeanUtils.copyProperties(pOrder, pOrderDetailVO);

        Map<String, Object> columnMap2 = new HashMap<>();
        columnMap2.put(ProductionManageOrderProduct.COL_ORDER_ID, orderId);
        List<ProductionManageOrderProduct> pOrderProducts = orderProductService.selectByMap(columnMap2);
        if (null == pOrderProducts) {
            pOrderProducts = new ArrayList<ProductionManageOrderProduct>();
        }
        ProductionManageClient client = clientService.selectById(pOrder.getClientId());
        ProductionManageClientVO clientVO = new ProductionManageClientVO();
        BeanUtils.copyProperties(client, clientVO);
        clientVO.setLocation();
        pOrderDetailVO.setPdaOrderProductDetails(pOrderProducts);

        setOrderDetailPackageMessage(orderId, pOrder, pOrderDetailVO);
        //设置仓储信息
        setStorageInfo(orderId, pOrderDetailVO);
        //所在地
        String address = commonUtil.getAddressWithAreaCode(pOrder.getClientAreaCode());
        pOrderDetailVO.setClientPdaAddress(address);

        return pOrderDetailVO;
    }

    private List<ProductionManageOrderProduct> productDTOsToOrderProduct(ProductManageOrderDTO orderDTO, ProductionManageOrder pOrder) throws SuperCodeException {
        List<ProductManageOrderProductDTO> productDTOs = orderDTO.getProOrderProductDTOs();

        List<ProductionManageOrderProduct> prOrderProducts = new ArrayList<>();
        StringBuilder productNameBuilder = new StringBuilder();
        Byte orderType = pOrder.getOrderType();
        BigDecimal all_totalPrice_b = new BigDecimal(0);
        all_totalPrice_b.setScale(2, BigDecimal.ROUND_HALF_UP);
        Integer orderProductNum = 0;

        Integer calculateGgPortionNum=0;
        Map<String,ProductManageOrderProductDTO> productDTOMap=new HashMap<>();
        for (ProductManageOrderProductDTO productDTO : productDTOs) {
            String productId = productDTO.getProductId();
            String productLevelCode = productDTO.getProductLevelCode();
            String productDTOMapKey = productId + productLevelCode;
            ProductManageOrderProductDTO productManageOrderProductDTO = productDTOMap.get(productDTOMapKey);
            if (null==productManageOrderProductDTO){
                productManageOrderProductDTO=new ProductManageOrderProductDTO();
                productManageOrderProductDTO.setProductNum(productDTO.getProductNum());
                productManageOrderProductDTO.setOrderWeight(productDTO.getOrderWeight());
                productManageOrderProductDTO.setProductName(productDTO.getProductName());
                productManageOrderProductDTO.setProductLevelName(productDTO.getProductLevelName());
                productManageOrderProductDTO.setProductId(productDTO.getProductId());
                productManageOrderProductDTO.setProductLevelCode(productDTO.getProductLevelCode());
            }else {
                productManageOrderProductDTO.setOrderWeight(CommonUtil.doubleAdd(productManageOrderProductDTO.getOrderWeight(),productDTO.getOrderWeight()));
                productManageOrderProductDTO.setProductNum(CommonUtil.integerAdd(productManageOrderProductDTO.getProductNum(),productDTO.getProductNum()));
            }
            productDTOMap.put(productDTOMapKey,productManageOrderProductDTO);
            String benefitPrice = productDTO.getBenefitPrice();
            Double orderWeight = productDTO.getOrderWeight();
            String unitPrice = productDTO.getUnitPrice();
            String totalPrice = StringUtils.isBlank(productDTO.getTotalPrice())?"0":productDTO.getTotalPrice();

            if (StringUtils.isBlank(unitPrice)) {
                throw new SuperCodeException("产品单价不能为空", 500);
            }
            if (orderType.equals(OrderTypeEnum.NUM.getStatus()) && StringUtils.isBlank(productDTO.getProductLevelCode())) {
                throw new SuperCodeException("按数量卖产品等级不能为空", 500);
            }
            BigDecimal unit_b = new BigDecimal(unitPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal totalPrice_b = new BigDecimal(totalPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

            BigDecimal real_b = caculate(productDTO.getProductName(), benefitPrice, orderType, productDTO.getProductNum(),productDTO.getOrderQuantity(), orderWeight,productDTO.getGgPartionNum(), unit_b, totalPrice_b);
            all_totalPrice_b = all_totalPrice_b.add(real_b);

            ProductionManageOrderProduct product = new ProductionManageOrderProduct();
            product.setId(productDTO.getId());
            product.setProductId(productDTO.getProductId());
            product.setProductName(productDTO.getProductName());
            product.setBenefitPrice(productDTO.getBenefitPrice());
            product.setOrderQuantity(productDTO.getOrderQuantity());
            product.setOrderWeight(productDTO.getOrderWeight());
            product.setPackingSpecCode(productDTO.getPackingSpecCode());
            product.setPackingSpecName(productDTO.getPackingSpecName());
            product.setProductLevelCode(productDTO.getProductLevelCode());
            product.setProductLevelName(productDTO.getProductLevelName());
            product.setProductSpecCode(productDTO.getProductSpecCode());
            product.setProductSpecName(productDTO.getProductSpecName());
            product.setPackingWayCode(productDTO.getPackingWayCode());
            product.setPackingWayName(productDTO.getPackingWayName());
            product.setProductNum(productDTO.getProductNum());
            product.setUnitPrice(productDTO.getUnitPrice());
            product.setTotalPrice(totalPrice_b);
            product.setProductSortId(productDTO.getProductSortId());
            product.setProductSortName(productDTO.getProductSortName());
            product.setGgPartionNum(productDTO.getGgPartionNum());
            prOrderProducts.add(product);
            productNameBuilder.append(productDTO.getProductName()).append(",");
            orderProductNum = orderProductNum + Optional.ofNullable(productDTO.getProductNum()).orElse(0);

            Integer ggPartionNum = productDTO.getGgPartionNum();
            if (null!=ggPartionNum){
                calculateGgPortionNum+=ggPartionNum;
            }
        }
        //校验按份数卖时份数值是否合法
        if (orderType.intValue()==OrderTypeEnum.PORTION.getStatus()){
            Integer ggTotalPartionNum = orderDTO.getGgTotalPartionNum();
            if (null==ggTotalPartionNum || !ggTotalPartionNum.equals(calculateGgPortionNum)){
                CommonUtil.throwSuperCodeExtException(500,"订单份数不合法");
            }
            pOrder.setGgTotalPartionNum(ggTotalPartionNum);
        }

        String orderMoney = pOrder.getOrderMoney();
        pOrder.setOrderProductNum(orderProductNum);
        pOrder.setProductName(productNameBuilder.substring(0, productNameBuilder.length() - 1));
        return prOrderProducts;
    }

    private BigDecimal caculate(String productName, String benefitPrice, Byte orderType, Integer productNum, Integer orderQuantity, Double orderWeight,Integer partionNum,
                                BigDecimal unit_b, BigDecimal totalPrice_b) throws SuperCodeException {
        BigDecimal quantity_b = null;
        if (orderType.intValue() == 1) {
            if (null == orderWeight || orderWeight <= 0) {
                throw new SuperCodeException("订单重量" + orderWeight + "不合法", 500);
            }
            quantity_b = new BigDecimal(orderWeight).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else if (orderType.intValue()==OrderTypeEnum.BOX_NUN.getStatus()){
            if (null == orderQuantity || orderQuantity <= 0) {
                throw new SuperCodeException("订单箱数" + orderQuantity + "不合法", 500);
            }
            quantity_b = new BigDecimal(orderQuantity).setScale(2, BigDecimal.ROUND_HALF_UP);
        }else if (orderType.intValue()==OrderTypeEnum.NUM.getStatus()){
            if (null == productNum || productNum <= 0) {
                throw new SuperCodeException("订单数量" + orderQuantity + "不合法", 500);
            }
            quantity_b = new BigDecimal(productNum).setScale(2, BigDecimal.ROUND_HALF_UP);
        }else if (orderType.intValue()==OrderTypeEnum.PORTION.getStatus()){
            if (null == partionNum || partionNum <= 0) {
                throw new SuperCodeException("订单份数" + orderQuantity + "不合法", 500);
            }
            quantity_b = new BigDecimal(partionNum).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        BigDecimal total_b = unit_b.multiply(quantity_b).setScale(2, BigDecimal.ROUND_HALF_UP);

        BigDecimal real_b = null;
        if (StringUtils.isNotBlank(benefitPrice)) {
            BigDecimal benefit_b = new BigDecimal(benefitPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

            real_b = total_b.subtract(benefit_b).setScale(2, BigDecimal.ROUND_HALF_UP);
//			  if (total_b.compareTo(benefit_b)<0) {
//				  throw new SuperCodeException("优惠金额不能大于总金额，请输入正确优惠金额", 500);
//			  }
        } else {
            real_b = total_b;
        }
//		  if (real_b.compareTo(totalPrice_b)!=0) {
//			  throw new SuperCodeException("产品-"+productName+"订单金额错误，正确值为："+real_b.toString(), 500);
//		  }
        return real_b;
    }

    /**
     * 导出订单信息excel
     *
     * @param orderListDTO
     * @param response
     */
    public void exportOrderExcel(ProductManageOrderListDTO orderListDTO, HttpServletResponse response) throws Exception {
        ArrayList<String> idList = orderListDTO.getIdList();
        List<ProductManageOrderListVO> list;
        // idList为空时导出所有，不为空则导出指定
        if (CollectionUtils.isEmpty(idList)) {
            orderListDTO.setCurrent(1);
            orderListDTO.setPageSize(commonUtil.getExportNumber());
            list = page(orderListDTO).getList();
        } else {
            list = dao.listByIds(idList, commonUtil.getSysId(), commonUtil.getOrganizationId());
        }
        if (null != list && !list.isEmpty()) {
            for (ProductManageOrderListVO productManageOrderListVO : list) {
                productManageOrderListVO.setOrderStatus(OrderStatusEnum.getDesc(productManageOrderListVO.getOrderStatus()));
                productManageOrderListVO.setVerifyStatus(OrderVerifyStatusEnum.getDesc(productManageOrderListVO.getVerifyStatus()));
                productManageOrderListVO.setOutboundStatus(OrderOutBoundStatusEnum.getDesc(productManageOrderListVO.getOutboundStatus()));
            }
        }
        ExcelUtils.listToExcel(list, orderListDTO.exportMetadataToMap(), "订单信息", response);
    }

    /**
     * 修改订单状态
     */
    public void updateOrderStatus(Long orderId) throws SuperCodeException {
        ProductionManageOrder order = baseMapper.selectById(orderId);
        CustomAssert.isNull(order, "此订单信息不存在");
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        boolean isSuccess = update().set(ProductionManageOrder.COL_ORDER_STATUS, OrderStatusEnum.UN_RECEIPT.getStatus())
                .eq("Id", orderId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManageOrder.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .update();
        CustomAssert.isSuccess(isSuccess, "修改订单状态失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void verify(Long id, Integer verifyStatus, String verifyNotPassedReason) throws SuperCodeException {
        ProductionManageOrder order = baseMapper.selectById(id);
        CustomAssert.isNull(order, "此订单信息不存在");
        if (null == verifyStatus || !(verifyStatus == 1 || verifyStatus == -1)) {
            CustomAssert.isNull(order, "审核状态非法");
        }
        Byte orderStatus = order.getOrderStatus();
        if (OrderStatusEnum.UN_DELIVEY.getStatus() != orderStatus.intValue()) {
            CommonUtil.throwSupercodeException(500, "订单需为待发货状态");
        }
//        order.setVerifyStatus(verifyStatus);
        order.setVerifyNotPassedReason(verifyNotPassedReason);
        dao.updateById(order);

        ProductionManageClient client = clientService.selectById(order.getClientId());
        if (null == client) {
            CustomAssert.isNull(order, "该订单客户不存在");
        }
        if (verifyStatus.intValue() == -1) {
            String userId = order.getSaleRealUserId();
            if (StringUtils.isBlank(userId)) {
                try {
                    userId = commonService.getUserIdByEmployId(order.getSaleUserId());
                } catch (Exception e) {
                    log.error("审核不通过时通过员工id：" + order.getSaleUserId() + "调用基础数据获取用户id失败：" + e.getLocalizedMessage());
                }
            }
            informService.sendOrgMessageToAllPartmentUser(userId, order.getSaleUserName(), "【订单通知】您提交的客户名称为" + client.getClientName() + "的订单审核不通过，请检查并重新提交", 0, 0, 0);
        }
    }

    public ProductionManageOrder selectById(Long orderId) {
        return baseMapper.selectById(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) throws SuperCodeException, ParseException {
        ProductionManageOrder order = baseMapper.selectById(orderId);
        if (null == order) {
            CommonUtil.throwSupercodeException(500, "订单不存在");
        }
        Byte orderStatus = Optional.ofNullable(order.getOrderStatus()).orElse((byte) 0);
        if (OrderStatusEnum.UN_DELIVEY.getStatus() != orderStatus.intValue()) {
            CommonUtil.throwSupercodeException(500, "订单状态为：" + OrderStatusEnum.getDesc(String.valueOf(orderStatus)) + " 不可删除");
        }

        QueryWrapper<ProductionManageOrderProduct> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageOrderProduct.COL_ORDER_ID, orderId);
        List<ProductionManageOrderProduct> prOrderProducts = orderProductService.list(queryWrapper);

        baseMapper.deleteById(orderId);
        orderProductService.deleteByOrderId(orderId);

        //重新统计销售产品
        orderProductDataStatisticsService.reStatistics(prOrderProducts, order.getOrderDate());
    }


    /**
     * 内部订单导入
     *
     * @param inputStream
     * @param fileName
     * @param importType
     * @throws IOException
     * @throws SuperCodeExtException
     * @throws ParseException
     */
    @Transactional(rollbackFor = Exception.class)
    public void orderImport(InputStream inputStream, String fileName, Integer importType) throws IOException, SuperCodeExtException, ParseException {

        if (fileName.startsWith("历史")) {
            EasyExcel.read(inputStream, HistoryOrderImportExcelHead.class, new HistoryOrderImportAnalysisEventListener(SpringContext.applicationContext, new Date())).sheet().doRead();
        } else {
            //内部导入
            if (importType == 1) {
                EasyExcel.read(inputStream, OrderImportExcelHead.class, new OrderImportAnalysisEventListener(SpringContext.applicationContext.getBean(OrderImportService.class), new Date(), importType)).headRowNumber(2).sheet().doRead();
            } else {
                EasyExcel.read(inputStream, OuterOrderImportExcelHead.class, new OuterOrderImportAnalysisEventListener(SpringContext.applicationContext.getBean(OrderImportService.class), new Date(), importType)).headRowNumber(2).sheet().doRead();
            }
        }
    }

    /**
     * 从七牛云下载excel导入订单
     *
     * @param importDTO
     * @throws SuperCodeException
     * @throws IOException
     * @throws ParseException
     */
    public void orderImportFromOss(ExcelImportDTO importDTO) throws SuperCodeException, IOException, ParseException {
        // 解析excel文件流
        // 从七牛云获取excel文件流
        EofSensorInputStream inputStream = (EofSensorInputStream) HttpUtil.doGet(OSS_DOMAIN + "/" + importDTO.getUniqueCode());
        if (Objects.isNull(inputStream)) {
            throw new SuperCodeException("获取excel文件失败");
        }
        Integer importType = importDTO.getImportType();
        orderImport(inputStream, importDTO.getFileName(), importType);
    }

    public void orderImportTest(InputStream inputStream) {
        EasyExcel.read(inputStream, OrderImportExcelHead.class, new HistoryOrderImportAnalysisEventListener(SpringContext.applicationContext, new Date())).sheet().doRead();
    }

    /**
     * 获取指定时间内的待发货订单数
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public Integer countWaitDelivery(String startDate, String endDate, String sysId, String organizationId) {
        return query().eq(StringUtils.isNotBlank(sysId), ProductionManageOrder.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageOrder.COL_ORDER_STATUS, OrderStatusEnum.UN_DELIVEY.getStatus())
                .ge(StringUtils.isNotBlank(startDate), ProductionManageOrder.COL_ORDER_DATE, startDate)
                .lt(StringUtils.isNotBlank(endDate), ProductionManageOrder.COL_ORDER_DATE, endDate)
                .count();
    }

    /**
     * 获取指定时间区间内的订单信息, 过滤掉销售人员为空或为''的脏数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public List<ProductionManageStatisticsSaleTargetData> listByStartAndEndDate(String startDate, String endDate, String sysId, String organizationId) {
        QueryWrapper<ProductionManageOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), "o."+ProductionManageOrder.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "o."+ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .ge(StringUtils.isNotBlank(startDate), "o."+ProductionManageOrder.COL_ORDER_DATE, startDate)
                .lt(StringUtils.isNotBlank(endDate), "o."+ProductionManageOrder.COL_ORDER_DATE, endDate)
                .isNotNull("o."+ProductionManageOrder.COL_SALE_USER_ID)
                .ne("o."+ProductionManageOrder.COL_SALE_USER_ID, "")
                .groupBy("DATE_FORMAT(o.order_date,'%Y-%m')", "op."+ProductionManageOrderProduct.COL_PRODUCT_ID, "o."+ProductionManageOrder.COL_SALE_USER_ID);
        return dao.listByStartAndEndDate(queryWrapper);
    }

    /**
     * 订单同步部门信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-05
     * @updateDate 2019-11-05
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void orderDepDatasync() {
        log.info("订单同步部门信息执行开始");
        List<ProductionManageSuperToken> superTokenList = superTokenMapper.getSuperTokenList();

        superTokenList.forEach(superToken -> {
            String sysId = superToken.getSysId();
            String organizationId = superToken.getOrganizationId();
            String token = superToken.getToken();
            // 获取订单中所有的销售人员id集合
            List<ProductionManageOrder> list = query().eq(StringUtils.isNotBlank(sysId), ProductionManageOrder.COL_SYS_ID, sysId)
                    .eq(StringUtils.isNotBlank(organizationId), ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                    .groupBy(ProductionManageOrder.COL_SALE_USER_ID).list();
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            list.forEach(order -> {
                String saleUserId = order.getSaleUserId();
                if (StringUtils.isBlank(saleUserId)) {
                    return;
                }
                EmployeeMsgDTO employeeMsg = commonUtil.getEmployeeMsg(token, saleUserId);
                if (Objects.isNull(employeeMsg) || StringUtils.isBlank(employeeMsg.getDepartmentId())) {
                    return;
                }
                update().set(ProductionManageOrder.COL_DEPARTMENT_ID, employeeMsg.getDepartmentId())
                        .eq(ProductionManageOrder.COL_SALE_USER_ID, saleUserId)
                        .eq(StringUtils.isNotBlank(sysId), ProductionManageOrder.COL_SYS_ID, sysId)
                        .eq(StringUtils.isNotBlank(organizationId), ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .update();
            });
        });

        log.info("订单同步部门信息执行结束");
    }
    @Transactional(rollbackFor = Exception.class)
    public void done(List<Long> orderIds) {
        List<ProductionManageOrder> orderList = baseMapper.selectBatchIds(orderIds);
        if (CollectionUtils.isEmpty(orderList)){
            CommonUtil.throwSuperCodeExtException(500,"订单不存在");
        }
        Date currentDate = CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS);
        byte status = (byte)OrderStatusEnum.DONE.getStatus();
        orderList.forEach(order->{
            if (order.getOrderStatus()==OrderStatusEnum.DONE.getStatus()){
                CommonUtil.throwSuperCodeExtException(500,"订单编号为"+order.getOrderNo()+"的订单已完成，不能重复做完成操作");
            }

            if (order.getOrderStatus()==OrderStatusEnum.UN_DELIVEY.getStatus()){
                CommonUtil.throwSuperCodeExtException(500,"订单编号为"+order.getOrderNo()+"的订单待发货，不能做订单完成操作");
            }
            Long orderId = order.getId();

            List<ProductionManageOrderProductReceived> receivedList1 = productReceivedService.listByOrderId(orderId);
            if (CollectionUtils.isEmpty(receivedList1)){
                if (order.getOrderStatus()==OrderStatusEnum.UN_RECEIPT.getStatus()){
                    Double receivedOrderMoney =0d;
                    Integer receivedOrderQuantity = 0;
                    Double receivedTotalBenefitPrice = 0d;
                    Double receivedOrderWeight = 0d;

                    List<ProductionManageOrderProduct> orderProducts=orderProductService.selectByOrderId(orderId);
                    if (CollectionUtils.isNotEmpty(orderProducts)){
                        List<ProductionManageOrderProductReceived> receivedList=new ArrayList<>();
                        for (ProductionManageOrderProduct pro : orderProducts) {
                            ProductionManageOrderProductReceived received=new ProductionManageOrderProductReceived();
                            BeanUtils.copyProperties(pro,received);
                            received.setReceivedBatchNum(1);
                            received.setReceivedBenefitPrice(pro.getBenefitPrice());
                            received.setReceivedProMoney(pro.getTotalPrice().toString());
                            received.setReceivedProWeight(pro.getOrderWeight());
                            received.setReceivedProQuantity(pro.getOrderQuantity());
                            received.setReceivedProductNum(pro.getProductNum());
                            received.setOrderId(pro.getOrderId());

                            if(StringUtils.isNotBlank(pro.getBenefitPrice())){
                                receivedTotalBenefitPrice=CommonUtil.doubleAdd(receivedTotalBenefitPrice,Double.parseDouble(pro.getBenefitPrice()));
                            }
                            if(StringUtils.isNotBlank(pro.getTotalPrice().toString())){
                                receivedOrderMoney=
                                        CommonUtil.doubleAdd(receivedOrderMoney,pro.getTotalPrice().doubleValue());
                            }
                            receivedOrderQuantity=CommonUtil.integerAdd(receivedOrderQuantity,pro.getOrderQuantity());
                            receivedOrderWeight=CommonUtil.doubleAdd(receivedOrderWeight,pro.getOrderWeight());
                            receivedList.add(received);
                        }
                        productReceivedService.saveOrUpdateBatch(receivedList);
                    }
                    order.setReceivedOrderQuantity(receivedOrderQuantity);
                    order.setReceivedOrderMoney(new BigDecimal(receivedOrderMoney).setScale(2,BigDecimal.ROUND_DOWN).toString());
                    order.setReceivedOrderWeight(new BigDecimal(receivedOrderWeight).setScale(2,BigDecimal.ROUND_DOWN).doubleValue());
                    order.setReceivedTotalBenefitPrice(new BigDecimal(receivedTotalBenefitPrice).setScale(2,BigDecimal.ROUND_DOWN).toString());
                }
                // 获取当前订单的实收信息列表
                List<ProductionManageOrderProductReceived> receivedList = productReceivedService.listByOrderId(order.getId());
                if (CollectionUtils.isNotEmpty(receivedList)) {
                    // 更新产品档案的实收总金额
                    recordService.updateTotalAmountReceived(order, receivedList);
                }
            }
            order.setDoneDate(currentDate);
            order.setOrderStatus(status);
        });
        saveOrUpdateBatch(orderList);
    }

    /**
     * 通过销售人员获取部门id
     *
     * @author shixiongfei
     * @date 2019-11-14
     * @updateDate 2019-11-14
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private String getDepartmentId(String employeeId) {
        // v1.8新加内容订单中插入销售人员所属的部门信息
        EmployeeMsgDTO employeeMsg = commonUtil.getEmployeeMsg(commonUtil.getSuperToken(), employeeId);
        if (Objects.isNull(employeeMsg)) {
            CustomAssert.throwExtException("此销售人员不隶属如何部门, 请检查");
        }

        return employeeMsg.getDepartmentId();
    }
}
