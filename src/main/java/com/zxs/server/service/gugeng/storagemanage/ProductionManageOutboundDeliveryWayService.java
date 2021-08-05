package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import com.jgw.supercodeplatform.utils.RedisLockUtil;
import io.netty.util.internal.MathUtil;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndPackageMessageBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.model.storagemanage.OutboundDeliveryExpress;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ProductionManageCommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddOutboundDeliveryRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderOutBoundStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.OrderStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryWayEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockFlowDetailTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundDeliveryWay;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundDeliveryWayMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackageMessageResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 出库发货方式表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@Service
public class ProductionManageOutboundDeliveryWayService extends ServiceImpl<ProductionManageOutboundDeliveryWayMapper, ProductionManageOutboundDeliveryWay> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageOutboundService outboundService;

    @Autowired
    private ProductManageOrderService orderService;

    @Autowired
    private ProductionManageProductRecordService recordService;

    @Autowired
    private ProductionManageOutboundPackageMessageService messageService;

    @Autowired
    private ProductionManageStockService stockService;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper messageMapper;

    @Autowired
    private ProductionManageStockFlowDetailsService flowDetailsService;

    @Autowired
    private RedisLockUtil lock;


    /**
     * 添加出库发货信息
     * v1.7新增多次发货原因，
     * 如果发货次数大于1，则原因必填
     *
     * @param requestDTO
     * @throws SuperCodeException
     * @date 2019-08-29
     * @since V1.1.1
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddOutboundDeliveryRequestDTO requestDTO) throws SuperCodeException {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        Long orderId = requestDTO.getOrderId();
        ProductionManageOrder manageOrder = orderService.selectById(orderId);
        CustomAssert.isNull(manageOrder, "出库订单不存在");

        // 校验参数是否合法
        // validDeliveryParams(requestDTO);
        // 校验出库主键id是否为空，为空则新增一条出库信息
        Long outboundId = requestDTO.getOutboundId();
        if (Objects.isNull(outboundId) || outboundId < 1) {
            outboundId = outboundService.add(requestDTO.getOrderId());
            requestDTO.setOutboundId(outboundId);
        }

        // 获取包装信息列表，可为空
        List<SearchPackageMessageResponseVO> list = messageService.list(outboundId).getList();

        Date now = new Date();
        ProductionManageOutboundDeliveryWay deliveryWay = new ProductionManageOutboundDeliveryWay();
        BeanUtils.copyProperties(requestDTO, deliveryWay);
        deliveryWay.setOrderId(requestDTO.getOrderId());
        Employee employee = commonUtil.getEmployee();
        deliveryWay.setCreateDate(now);
        deliveryWay.setCreateUserId(employee.getEmployeeId());
        deliveryWay.setCreateUserName(employee.getName());
        if (DeliveryWayEnum.EXPRESS.getKey() == requestDTO.getDeliveryWay()) {
            List<OutboundDeliveryExpress> expressList = requestDTO.getExpressList();
            fillExpressNoAndCo(deliveryWay, expressList);
        }

        CustomAssert.isGreaterThan0(baseMapper.insert(deliveryWay), "新增出库发货信息失败");

        // 更新出库发货时间
        outboundService.updateDeliveryDate(requestDTO.getOutboundId(), now);

        // 更新首次出库时间和销售总额 以及 添加销售客户信息
        recordService.updateSaleAmountAndOutDate(requestDTO.getOutboundId(), now);

        // V1.1.1 如果包装信息不为空，则更新库存和新增库存流水
        int outboundNum = Optional.ofNullable(manageOrder.getOutboundNum()).orElse(0);
        if (CollectionUtils.isNotEmpty(list)) {
            List<Long> packIds = list.stream().map(SearchPackageMessageResponseVO::getId)
                    .collect(Collectors.toList());
            messageService.updateOutboundTimes(packIds, ++outboundNum);
        }

        manageOrder.setOutboundNum(outboundNum);
        manageOrder.setOutboundStatus(OrderOutBoundStatusEnum.PART_DELIVEY.getStatus());
        manageOrder.setOrderStatus((byte) OrderStatusEnum.UN_RECEIPT.getStatus());
        orderService.updateById(manageOrder);
    }

    private void fillExpressNoAndCo(ProductionManageOutboundDeliveryWay deliveryWay, List<OutboundDeliveryExpress> expressList) {
        StringBuilder expressNoBuilder = new StringBuilder();
        StringBuilder expressCoBuilder = new StringBuilder();
        for (OutboundDeliveryExpress express : expressList) {
            expressNoBuilder.append(express.getExpressNo()).append("&");
            expressCoBuilder.append(express.getExpressCo()).append("&");
        }
        deliveryWay.setExpressCo(expressCoBuilder.substring(0, expressCoBuilder.length() - 1));
        deliveryWay.setExpressNo(expressNoBuilder.substring(0, expressNoBuilder.length() - 1));
    }

    /**
     * 对出库发货的参数进行校验
     */
    private void validDeliveryParams(AddOutboundDeliveryRequestDTO requestDTO) throws SuperCodeException {
        String errorMessage = StringUtils.EMPTY;
        if (DeliveryWayEnum.DISTRIBUTION.getKey() == requestDTO.getDeliveryWay()) {
            if (StringUtils.isBlank(requestDTO.getDriverName())) {
                errorMessage = "物流司机不可为空";
            }
            if (StringUtils.isBlank(requestDTO.getCarNo())) {
                errorMessage = "车牌号不可为空";
            }
            if (StringUtils.isBlank(requestDTO.getContactPhone())) {
                errorMessage = "联系方式不可为空";
            }
        }

        if (DeliveryWayEnum.EXPRESS.getKey() == requestDTO.getDeliveryWay()) {
            List<OutboundDeliveryExpress> expressList = requestDTO.getExpressList();
            if (null == expressList || expressList.isEmpty()) {
                for (OutboundDeliveryExpress outboundDeliveryExpress : expressList) {
                    if (StringUtils.isBlank(outboundDeliveryExpress.getExpressCo()) || StringUtils.isBlank(outboundDeliveryExpress.getExpressNo())) {
                        errorMessage = "快递单号及快递公司不可为空";
                        break;
                    }
                }
            }
        }

        if (StringUtils.isNotBlank(errorMessage)) {
            throw new SuperCodeException(errorMessage);
        }
    }

    /**
     * 获取发货信息详情
     *
     * @param outboundId
     */
    public ProductionManageOutboundDeliveryWay getDetail(Long outboundId) throws SuperCodeException {
        QueryWrapper<ProductionManageOutboundDeliveryWay> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageOutboundDeliveryWay.COL_OUTBOUND_ID, outboundId);
        ProductionManageOutboundDeliveryWay deliveryWay = baseMapper.selectOne(queryWrapper);
        return deliveryWay;
    }

    /**
     * 获取发货信息详情
     *
     * @param outboundId
     * @return
     */
    public List<ProductionManageOutboundDeliveryWay> getOutboundDeliveryWays(Long outboundId) {
        QueryWrapper<ProductionManageOutboundDeliveryWay> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageOutboundDeliveryWay.COL_OUTBOUND_ID, outboundId);
        return baseMapper.selectList(queryWrapper);
    }

    /**
     * 校验库存数据是否满足出库需求
     * 仅用于有码库存校验
     */
    public List<SearchPackageMessageResponseVO> groupBydStock(List<SearchPackageMessageResponseVO> list) {

        List<SearchPackageMessageResponseVO> responseVOS = list.stream().distinct().collect(Collectors.toList());
        return Optional.ofNullable(responseVOS).orElse(Collections.emptyList());
    }

    /**
     * 包装出库时校验库存是否满足出库条件
     *
     * @param list            需要校验的包装信息集合
     */
    @Deprecated
    private void validPackageIsLegal(List<SearchPackageMessageResponseVO> list) throws SuperCodeException {
        for (SearchPackageMessageResponseVO responseVO : list) {
            ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(responseVO.getPlantBatchId(), responseVO.getProductLevelCode());
            if (null == stock) {
                CustomAssert.throwException(String.format("批次为[%s]产品等级为[%s]的库存不存在，请检查", responseVO.getPlantBatchId(), responseVO.getProductLevelCode()));
            }
            stock.initStock();
            //根据批次等级和码类型统计所有待发货的包装信息
            OrderAndPackageMessageBO messageBO = messageMapper.getUnDeliveryTotalByBatchIdAndLevelCode(responseVO.getPlantBatchId(), responseVO.getProductLevelCode());
            // 校验当前包装扫码是否为空，为空则赋予初始值
            if (Objects.isNull(messageBO)) {
                messageBO = new OrderAndPackageMessageBO();
                messageBO.setTotalPackingWeight(new BigDecimal(0));
                messageBO.setTotalPackingNum(0);
                messageBO.setTotalPackingBoxNum(0);
            }

            messageBO.init();
            if (MathUtil.compare(stock.getBoxNum(), messageBO.getTotalPackingBoxNum()) == -1) {
                CustomAssert.throwException(String.format("批次为[%s]产品等级为[%s]的库存箱数不足，请检查", responseVO.getPlantBatchId(), responseVO.getProductLevelCode()));
            }

            if (MathUtil.compare(stock.getQuantity(), messageBO.getTotalPackingNum()) == -1) {
                CustomAssert.throwException(String.format("批次为[%s]产品等级为[%s]的库存个数不足，请检查", responseVO.getPlantBatchId(), responseVO.getProductLevelCode()));
            }
            if (CommonUtil.bigDecimalSub(stock.getWeight(), messageBO.getTotalPackingWeight()).signum() == -1) {
                CustomAssert.throwException(String.format("批次为[%s]产品等级为[%s]的库存重量不足，请检查", responseVO.getPlantBatchId(), responseVO.getProductLevelCode()));
            }
        }
    }

    /**
     * 批量扣减库存
     * 可优化内容，这里的批量库存扣减可采用统计方式来进行扣减，减少数据库的负载
     *
     * @author shixiongfei
     * @date 2019-08-29
     * @since V1.1.1
     */
    public void batchSubtractStock(List<SearchPackageMessageResponseVO> list) throws SuperCodeException {
        // 更新库存信息，校验库存数据是否满足出库发货需求
        for (SearchPackageMessageResponseVO responseVO : list) {
            String key = ProductionManageCommonUtil.redisStockCheckSuffix(
                    RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, responseVO.getPlantBatchId(), responseVO.getProductLevelCode());
            try {
                if (!lock.lock(key, 1000L, 3, 200L)) {
                    throw new SuperCodeException("更新库存失败,请稍后重试", 500);
                }
                // 获取库存相关信息
                ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(responseVO.getPlantBatchId(), responseVO.getProductLevelCode());
                stock.initStock();
                Integer packingBoxNum = Optional.ofNullable(responseVO.getPackingBoxNum()).orElse(0);
                Integer packingNum = Optional.ofNullable(responseVO.getPackingNum()).orElse(0);
                BigDecimal packingWeight = Optional.ofNullable(responseVO.getPackingWeight()).orElse(BigDecimal.ZERO);
                // 拼接sql
                UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
                wrapper.set(ProductionManageStock.COL_QUANTITY, Optional.of(stock.getQuantity() - packingNum).filter(num -> num >= 0).orElse(0))
                        .set(ProductionManageStock.COL_BOX_NUM, Optional.of(stock.getBoxNum() - packingBoxNum).filter(num -> num >= 0).orElse(0))
                        .set(ProductionManageStock.COL_WEIGHT, Optional.of(stock.getWeight().subtract(packingWeight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                        .set(ProductionManageStock.COL_TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + packingBoxNum)
                        .set(ProductionManageStock.COL_TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + packingNum)
                        .set(ProductionManageStock.COL_TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(packingWeight));
                stockService.update(stock.getId(), wrapper);
            } finally {
                lock.releaseLock(key);
            }
        }
    }

    /**
     * 批量添加库存流水信息
     */
    public void batchAddStockFlowDetails(List<SearchPackageMessageResponseVO> list, Long outboundId) throws SuperCodeExtException {
        // 统计相同批次，产品等级，码类型, 为了保证唯一性，采用:::来做字段分隔符
        Map<String, List<SearchPackageMessageResponseVO>> listMap = list.stream().collect(Collectors.groupingBy(vo -> vo.getPlantBatchId() + ":::" + vo.getProductLevelCode()));
        listMap.forEach((k, v) -> {
            String[] key = k.split(":::");
            String plantBatchId = key[0];
            String productLevelCode = key[1];

            // 统计当前批次，等级，码类型下的总出库重量
            BigDecimal totalWeight = v.stream().map(responseVO -> Optional.ofNullable(responseVO.getPackingWeight()).orElse(BigDecimal.ZERO)).reduce(BigDecimal.ZERO, BigDecimal::add);
            // 统计当前批次，等级，码类型下的总出库个数
            int totalQuantity = v.stream().mapToInt(responseVO -> Optional.ofNullable(responseVO.getPackingNum()).orElse(0)).sum();
            // 统计当前批次，等级，码类型下的总出库箱数
            int totalBoxNum = v.stream().mapToInt(responseVO -> Optional.ofNullable(responseVO.getPackingBoxNum()).orElse(0)).sum();

            ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
            flowDetails.setBusinessId(outboundId);
            flowDetails.setProductLevelCode(productLevelCode);
            flowDetails.setPlantBatchId(plantBatchId);
            flowDetails.setOutInType(StockFlowDetailTypeEnum.PACKAGING_OUT.getKey());
            flowDetails.setOutInBoxNum(totalBoxNum);
            flowDetails.setOutInNum(totalQuantity);
            flowDetails.setOutInWeight(totalWeight);
            try {
                flowDetailsService.add(flowDetails);
            } catch (Exception e) {
                throw new SuperCodeExtException(e.getMessage(), e);
            }
        });
    }

    public List<ProductionManageOutboundDeliveryWay> getDeliveryWaysByOrderId(Long orderId) {
        QueryWrapper<ProductionManageOutboundDeliveryWay> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageOutboundDeliveryWay.COL_ORDER_ID, orderId);
        queryWrapper.orderByAsc(ProductionManageOutboundDeliveryWay.COL_ID);
        return baseMapper.selectList(queryWrapper);
    }

    public List<OutboundDeliveryExpress> getOutboundDeliveryExpressList(String expressNo, String expressCo) {
        List<OutboundDeliveryExpress> outboundDeliveryExpresses = new ArrayList<>();
        if (StringUtils.isBlank(expressCo) && StringUtils.isBlank(expressNo)) {
            return outboundDeliveryExpresses;
        }

        if (StringUtils.isNotBlank(expressCo) && StringUtils.isNotBlank(expressNo)) {
            String[] expressNoArr = expressNo.split("&");
            String[] expressCoArr = expressCo.split("&");
            for (int i = 0; i < expressNoArr.length; i++) {
                OutboundDeliveryExpress outboundDeliveryExpress = new OutboundDeliveryExpress();
                outboundDeliveryExpress.setExpressCo(expressCoArr[i]);
                outboundDeliveryExpress.setExpressNo(expressNoArr[i]);
                outboundDeliveryExpresses.add(outboundDeliveryExpress);
            }
        }else if (StringUtils.isBlank(expressCo)){
            String[] expressNoArr = expressNo.split("&");
            for (int i = 0; i < expressNoArr.length; i++) {
                OutboundDeliveryExpress outboundDeliveryExpress = new OutboundDeliveryExpress();
                outboundDeliveryExpress.setExpressNo(expressNoArr[i]);
                outboundDeliveryExpresses.add(outboundDeliveryExpress);
            }
        }else if (StringUtils.isBlank(expressNo)){
            String[] expressNoArr = expressCo.split("&");
            for (int i = 0; i < expressNoArr.length; i++) {
                OutboundDeliveryExpress outboundDeliveryExpress = new OutboundDeliveryExpress();
                outboundDeliveryExpress.setExpressCo(expressNoArr[i]);
                outboundDeliveryExpresses.add(outboundDeliveryExpress);
            }
        }
        return outboundDeliveryExpresses;
    }

    /**
     * 获取指定发货方式的总数量, 仅用于发货数据定时任务中
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-23
     * @updateDate 2019-10-23
     * @updatedBy shixiongfei
     */
    public Integer getTotalNumber(String startDate, String endDate, DeliveryWayEnum deliveryWayEnum, String sysId, String organizationId) {
        QueryWrapper<ProductionManageOutboundDeliveryWay> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), "o."+ProductionManageOrder.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "o."+ProductionManageOrder.COL_ORGANIZATION_ID, organizationId)
                .eq("dw."+ProductionManageOutboundDeliveryWay.COL_DELIVERY_WAY, deliveryWayEnum.getKey())
                .ge(StringUtils.isNotBlank(startDate), "dw."+ProductionManageOutboundDeliveryWay.COL_CREATE_DATE, startDate)
                .lt(StringUtils.isNotBlank(endDate), "dw."+ProductionManageOutboundDeliveryWay.COL_CREATE_DATE, endDate);
        return baseMapper.getTotalNumber(wrapper);
    }
}