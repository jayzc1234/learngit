package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.netty.util.internal.MathUtil;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.gugeng.InboundAndPackingMsgBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OrderAndPackageMessageBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.OutboundTotalNumberBO;
import net.app315.hydra.intelligent.planting.bo.gugeng.PackingOutboundNumBO;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.MyRedisLockUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ProductionManageCommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.BatchTypesEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.FlowDirectionTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.NeedSyncInboundEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.po.gugeng.UpdatePmPO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageOrder;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutbound;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageOutboundPackageMessage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockFlowDetailsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.salemanage.ProductManageOrderService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackageMessageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPackingMsgResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.OLD_ORDER_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.PackingOutboundConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.PackingOutboundErrorMsgConstants.*;

/**
 * <p>
 * 出库包装信息表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@Slf4j
@Service
public class ProductionManageOutboundPackageMessageService extends ServiceImpl<ProductionManageOutboundPackageMessageMapper, ProductionManageOutboundPackageMessage> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSortInstorageService storageService;

    @Autowired
    private ProductionManageOutboundService outboundService;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper dao;

    @Autowired
    private ProductionManageStockService stockService;

    @Autowired
    private ProductManageOrderService orderService;

    @Autowired
    private ProductionManageStockFlowDetailsService flowDetailsService;

    @Autowired
    private ProductionManageStockFlowDetailsMapper flowDetailsMapper;

    @Autowired
    private ProductionManageStockMapper stockMapper;

    @Autowired
    public ProductionManageOutboundMapper outboundMapper;

    @Autowired
    private MyRedisLockUtil lockUtil;

    /**
     * 添加包装信息
     */
    private Long addStorage(AddCodeLessPackageMessageRequestDTO packageMessage, Long outboundId, Long orderId) {
        ProductionManageOutboundPackageMessage entity = new ProductionManageOutboundPackageMessage();
        BeanUtils.copyProperties(packageMessage, entity);
        entity.setGgPlantBatchType(packageMessage.getPlantBatchType());
        entity.setGgPackingServings(packageMessage.getPackingServings());
        entity.setGgIsSyncInbound(packageMessage.getAddOrNotSyncInbound());
        /**
         * 这里就很尴尬了 为什么一开始BatchTypesEnum的值得顺序和SortingInventoryTypeEnum不定义成一致呢 现在就需要额外转换了啊
         */
        if (packageMessage.getPlantBatchType() == BatchTypesEnum.PROCESS_PLANT.getKey()){
            entity.setSortingType((byte) BatchTypesEnum.PROCESS_PLANT.getKey());
        }else {
            boolean isPlanting = BatchTypesEnum.PLANT.getKey() == packageMessage.getPlantBatchType();
            entity.setSortingType(isPlanting ? (byte) SortingInventoryTypeEnum.HARVEST_SORTING.getKey()
                    : (byte) SortingInventoryTypeEnum.OUTSIDE_SORTING.getKey());
        }
        // 设置包装出库时间
        entity.setPackingDate(new Date());
        entity.setOutboundId(outboundId);
        entity.setOrderId(orderId);
        // 添加包装出库信息
        CustomAssert.zero2Error(baseMapper.insert(entity), ADD_PACKING_OUTBOUND_MSG_FAILED);

        return entity.getId();
    }


    /**
     * 通过出库信息id来获取包装信息列表
     *
     * @param outboundId 出库信息主键id
     * @return
     */
    public PageResults<List<SearchPackageMessageResponseVO>> list(Long outboundId) throws SuperCodeException {
        PageResults<List<SearchPackageMessageResponseVO>> pageResults = new PageResults<>();
        QueryWrapper<SearchPackageMessageResponseVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("o." + ProductionManageOutbound.COL_ID, outboundId)
                .eq("o." + ProductionManageOutbound.COL_SYS_ID, commonUtil.getSysId())
                .eq("o." + ProductionManageOutbound.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq("pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM, 0)
                .orderByDesc("pm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE);
        // 这里采用默认值，为了满足前端组件需求
        pageResults.setPagination(new Page(1000, 1, 1000));
        List<SearchPackageMessageResponseVO> list = Optional.ofNullable(baseMapper.list(queryWrapper)).orElse(Collections.emptyList());

        // 对null值进行初始化，防止出现npe
        list.forEach(vo -> {
            vo.setPackingNum(Optional.ofNullable(vo.getPackingNum()).orElse(0));
            vo.setPackingBoxNum(Optional.ofNullable(vo.getPackingBoxNum()).orElse(0));
            vo.setPackingWeight(Optional.ofNullable(vo.getPackingWeight()).orElse(BigDecimal.ZERO));
        });

        pageResults.setList(list);
        return pageResults;
    }

    /**
     * 获取包装出库详情信息
     *
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<Map<String, List<ProductionManageOutboundPackageMessage>>> orderDetailPackageMessage(Long orderId) {
        ProductionManageOrder productionManageOrder = orderService.selectById(orderId);
        CustomAssert.null2Error(productionManageOrder, ORDER_IS_NOT_EXISTS);
        List<ProductionManageOutboundPackageMessage> packageMessages = selectByOrderId(orderId);
        List<Map<String, List<ProductionManageOutboundPackageMessage>>> packageMessagesList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(packageMessages)) {
            TreeMap<Integer, List<ProductionManageOutboundPackageMessage>> map = new TreeMap<>();

            packageMessages.forEach(outboundPackageMessage -> {
                if (Objects.nonNull(outboundPackageMessage.getOutboundNum()) && outboundPackageMessage.getOutboundNum() > 0) {
                    List<ProductionManageOutboundPackageMessage> outboundPackageMessages =
                            Optional.ofNullable(map.get(outboundPackageMessage.getOutboundNum())).orElse(Collections.emptyList());
                    outboundPackageMessage.setNullToZero();
                    outboundPackageMessages.add(outboundPackageMessage);
                    map.put(outboundPackageMessage.getOutboundNum(), outboundPackageMessages);
                }
            });

            IntStream.rangeClosed(1, productionManageOrder.getOutboundNum()).forEach(i -> {
                List<ProductionManageOutboundPackageMessage> outboundPackageMessages = map.get(i);
                if (Objects.isNull(outboundPackageMessages)) {
                    outboundPackageMessages = new ArrayList<>();
                    map.put(i, outboundPackageMessages);
                }
            });

            map.keySet().forEach(key -> {
                Map<String, List<ProductionManageOutboundPackageMessage>> packageMessageMap = new HashMap<>(1);
                packageMessageMap.put("packageMessageList", map.get(key));
                packageMessagesList.add(packageMessageMap);
            });

        }
        return packageMessagesList;
    }

    /**
     * 通过订单id获取包装信息列表
     *
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<ProductionManageOutboundPackageMessage> selectByOrderId(Long orderId) {
        return dao.selectByOrderId(orderId);
    }

    /**
     * 包装出库
     *
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void codeLessAdd(AddPackingOutboundRequestDTO requestDTO) {
        log.info("包装出库入参:{}", requestDTO);

        // 对请求体中的参数值进行初始化，防止后续出现npe
        List<AddCodeLessPackageMessageRequestDTO> messageList = requestDTO.getPackageMessageList();
        messageList.forEach(packageMessage -> {
            packageMessage.setPackingBoxNum(Optional.ofNullable(packageMessage.getPackingBoxNum()).orElse(0));
            packageMessage.setPackingWeight(Optional.ofNullable(packageMessage.getPackingWeight()).orElse(BigDecimal.ZERO));
            packageMessage.setPackingServings(Optional.ofNullable(packageMessage.getPackingServings()).orElse(0));
            packageMessage.setPackingNum(Optional.ofNullable(packageMessage.getPackingNum()).orElse(0));
        });

        // 筛选出同步入库的相关数据
        List<AddCodeLessPackageMessageRequestDTO> syncInboundList = messageList.stream()
                .filter(t -> NeedSyncInboundEnum.YES.getKey() == t.getAddOrNotSyncInbound())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(syncInboundList)) {
            addWithSyncInbound(requestDTO, syncInboundList);
        }

        // 筛选出非同步入库的相关数据
        List<AddCodeLessPackageMessageRequestDTO> unSyncInboundList = messageList.stream()
                .filter(t -> NeedSyncInboundEnum.NO.getKey() == t.getAddOrNotSyncInbound())
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(unSyncInboundList)) {
            addWithNonSyncInbound(requestDTO, unSyncInboundList);
        }
    }

    /**
     * 添加包装出库 => 同步入库
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    private void addWithSyncInbound(AddPackingOutboundRequestDTO requestDTO, List<AddCodeLessPackageMessageRequestDTO> syncInboundList) {
        log.info("添加包装出库(同步入库)入参 => 包装信息集合:{}", syncInboundList);
        Long orderId = requestDTO.getOrderId();
        Long outboundId = requestDTO.getOutBoundId();
        syncInboundList.forEach(packageMessage -> {
            Integer packingNum = packageMessage.getPackingNum();
            Integer packingBoxNum = packageMessage.getPackingBoxNum();
            BigDecimal packingWeight = packageMessage.getPackingWeight();
            // 出库
            // 获取出库信息，如果出库信息为空则新增，不为空则更新
            ProductionManageOutbound outbound = outboundService.getByIdAndOrderId(orderId, outboundId);
            // 添加/更新出库信息
            outbound = outboundService.addOrUpdate(outbound, packageMessage, requestDTO);
            // 获取真正的出库主键id
            Long actualOutboundId = outbound.getId();
            // 添加包装出库信息
            Long packingId = addStorage(packageMessage, actualOutboundId, orderId);
            // 入库
            AddSortStorageRequestDTO inboundDTO = new AddSortStorageRequestDTO();
            BeanUtils.copyProperties(packageMessage, inboundDTO);
            // 设置入库数值信息
            inboundDTO.setBoxNum(packingBoxNum);
            inboundDTO.setWeight(packingWeight);
            inboundDTO.setQuantity(packingNum);
            // 设置包装信息主键id
            inboundDTO.setGgPackingId(packingId);
            boolean isHarvest = BatchTypesEnum.PLANT.getKey() == packageMessage.getPlantBatchType();
            // 设置分拣类型
            inboundDTO.setSortingType(isHarvest ? (byte) SortingInventoryTypeEnum.HARVEST_SORTING.getKey()
                    : (byte) SortingInventoryTypeEnum.OUTSIDE_SORTING.getKey());
            storageService.addButNotInStock(inboundDTO);

            // 添加库存流水
            flowDetailsService.addOutbound(actualOutboundId, packageMessage);
        });

    }

    /**
     * 添加包装出库 => 不同步入库
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    private void addWithNonSyncInbound(AddPackingOutboundRequestDTO requestDTO, List<AddCodeLessPackageMessageRequestDTO> syncInboundList) {
        log.info("添加包装出库(不同步入库)入参 => 包装信息集合:{}", syncInboundList);

        Long orderId = requestDTO.getOrderId();
        Long outboundId = requestDTO.getOutBoundId();

        // 校验合法性
        List<InboundAndPackingMsgBO> bos = validPackingIsLegal(syncInboundList);

        bos.forEach(bo -> {
            AddCodeLessPackageMessageRequestDTO packageMessage = bo.getPackageMessage();
            ProductionManageStock stock = bo.getStock();

            String productLevelCode = packageMessage.getProductLevelCode();
            String plantBatchId = packageMessage.getPlantBatchId();
            Integer packingNum = packageMessage.getPackingNum();
            Integer packingBoxNum = packageMessage.getPackingBoxNum();
            BigDecimal packingWeight = packageMessage.getPackingWeight();

            // 添加redis锁, 一次添加多个在同一个事务中，并不保证数据的最终一致性。
            String key = ProductionManageCommonUtil.redisStockCheckSuffix(
                    RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, plantBatchId, productLevelCode);
            try {
                lockUtil.lock(key, ADD_PACKING_OUTBOUND_FAILED);

                // 获取出库信息，如果出库信息为空则新增，不为空则更新
                ProductionManageOutbound outbound = outboundService.getByIdAndOrderId(orderId, outboundId);
                // 添加/更新出库信息
                outbound = outboundService.addOrUpdate(outbound, packageMessage, requestDTO);
                // 获取真正的出库主键id
                Long actualOutboundId = outbound.getId();

                // 添加包装出库信息
                addStorage(packageMessage, actualOutboundId, orderId);

                // 扣减库存
                stockService.deductStock(stock, packingBoxNum, packingNum, packingWeight);

                // 添加库存流水
                flowDetailsService.addOutbound(actualOutboundId, packageMessage);
            } finally {
                lockUtil.releaseLock(key);
            }
        });
    }

    /**
     * 校验包装出库是否合法
     *
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private List<InboundAndPackingMsgBO> validPackingIsLegal(List<AddCodeLessPackageMessageRequestDTO> packageMessageList) {
        log.info("校验包装出库是否合法入参:{}", packageMessageList);
        // 进行入库 + 库存校验
        return packageMessageList.stream().map(packageMessage -> {
            // 1. 校验当前批次-等级是否存在入库记录，不存在则报错
            List<ProductionManageSortInstorage> list = storageService.listByBatchIdAndLevelCode(
                    packageMessage.getPlantBatchId(), packageMessage.getProductLevelCode());
            CustomAssert.empty2Error(list, String.format(
                    CURRENT_BATCH_PRODUCT_LEVEL_NOT_EXISTS_INBOUND_RECORD, packageMessage.getPlantBatchName()));
            // 2. 校验当前出库的信息是否满足库存扣减
            ProductionManageStock stock = validStockIsLegal(packageMessage);

            InboundAndPackingMsgBO bo = new InboundAndPackingMsgBO();
            bo.setPackageMessage(packageMessage);
            bo.setStock(stock);
            return bo;
        }).collect(Collectors.toList());
    }

    /**
     * 校验库存是否合法，用于包装新增校验
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private ProductionManageStock validStockIsLegal(AddCodeLessPackageMessageRequestDTO requestDTO) {
        return validStockIsLegal(requestDTO.getPlantBatchId(), requestDTO.getPlantBatchName(),
                requestDTO.getProductLevelCode(), requestDTO.getPackingBoxNum(),
                requestDTO.getPackingNum(), requestDTO.getPackingWeight());
    }

    /**
     * 校验库存是否合法，用于包装编辑校验
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private ProductionManageStock validStockIsLegal(UpdatePackingMessageRequestDTO requestDTO) {
        return validStockIsLegal(requestDTO.getPlantBatchId(), requestDTO.getPlantBatchName(),
                requestDTO.getProductLevelCode(), requestDTO.getPackingBoxNum(),
                requestDTO.getPackingNum(), requestDTO.getPackingWeight());
    }

    /**
     * 校验库存数据是否满足当前包装出库的数据总和
     * 1. 获取当前库存数据
     * 2. 获取包装出库集合
     * 3. 校验重量，箱数，产品数量是否满足要求
     *
     * @date 2019-08-28
     * @since V1.1.1
     */
    private ProductionManageStock validStockIsLegal(String plantBatchId, String plantBatchName, String productLevelCode,
                                                    Integer boxNum, Integer packingNum, BigDecimal weight) {
        ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(plantBatchId, productLevelCode);
        CustomAssert.null2Error(stock, String.format(CURRENT_BATCH_PRODUCT_LEVEL_NOT_EXISTS_INVENTORY_MSG, plantBatchName));
        // 初始化库存信息
        stock.initStock();

        // 通过批次+等级来获取总箱数，总个数，总重量
        OrderAndPackageMessageBO messageBO =
                Optional.ofNullable(baseMapper.getUnDeliveryTotalByBatchIdAndLevelCode(plantBatchId, productLevelCode))
                        .orElse(new OrderAndPackageMessageBO());
        // 初始化业务实体类
        messageBO.init();
        boxNum = Optional.ofNullable(boxNum).orElse(0);
        packingNum = Optional.ofNullable(packingNum).orElse(0);
        weight = Optional.ofNullable(weight).orElse(BigDecimal.ZERO);

        if (MathUtil.compare(stock.getBoxNum(), messageBO.getTotalPackingBoxNum() + boxNum) == -1) {
            Integer absoluteBoxNum = stock.getBoxNum() - messageBO.getTotalPackingBoxNum();
            CustomAssert.throwExtException(String.format(INVENTORY_BOX_NUM_INSUFFICIENT, absoluteBoxNum));
        }

        if (MathUtil.compare(stock.getQuantity(), messageBO.getTotalPackingNum() + packingNum) == -1) {
            Integer absoluteQuantity = stock.getQuantity() - messageBO.getTotalPackingNum();
            CustomAssert.throwExtException(String.format(INVENTORY_QUANTITY_INSUFFICIENT, absoluteQuantity));
        }

        if (stock.getWeight().subtract(messageBO.getTotalPackingWeight()).subtract(weight).signum() == -1) {
            BigDecimal absoluteWeight = stock.getWeight().subtract(messageBO.getTotalPackingWeight());
            CustomAssert.throwExtException(String.format(INVENTORY_WEIGHT_INSUFFICIENT, absoluteWeight));
        }

        return stock;
    }


    /**
     * 通过订单id获取包装信息列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchPackageMessageResponseVO>> listByOrderId(Long orderId) {
        PageResults<List<SearchPackageMessageResponseVO>> pageResults = new PageResults<>();
        QueryWrapper<SearchPackageMessageResponseVO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("o." + ProductionManageOutbound.COL_ORDER_ID, orderId)
                .eq("pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM, 0)
                .eq("o." + ProductionManageOutbound.COL_SYS_ID, commonUtil.getSysId())
                .eq("o." + ProductionManageOutbound.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .orderByDesc("pm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE);
        // 这里采用默认值，为了满足前端组件需求
        pageResults.setPagination(new Page(1000, 1, 1000));
        List<SearchPackageMessageResponseVO> list = Optional.ofNullable(baseMapper.list(queryWrapper)).orElse(Collections.emptyList());
        // 初始化数值
        list.forEach(responseVO -> {
            responseVO.setPackingBoxNum(Optional.ofNullable(responseVO.getPackingBoxNum()).orElse(0));
            responseVO.setPackingNum(Optional.ofNullable(responseVO.getPackingNum()).orElse(0));
            responseVO.setPackingWeight(Optional.ofNullable(responseVO.getPackingWeight()).orElse(BigDecimal.ZERO));
            responseVO.setPackingServings(Optional.ofNullable(responseVO.getPackingServings()).orElse(0));
        });

        pageResults.setList(list);
        return pageResults;
    }

    /**
     * 编辑包装出库
     * 只可编辑包装规格和包装方式以及重量 + 数量 + 箱数 + 份数
     *
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOutbound(UpdatePackingOutboundRequestDTO requestDTO) {
        // 这里再次校验当前包装信息是否出库，出库了则报错不可编辑
        // 因为这里不会涉及到高并发的场景，所以不会采用锁等细粒度的方式来处理
        List<SearchPackingMsgResponseVO> packages = listNotOutboundMsg(requestDTO.getOrderId());
        CustomAssert.empty2Error(packages, "此订单不存在待发货的信息");

        // 这里未对具体的包装信息进行校验，可能会出现前端恶意篡改包装信息的值，这里采用信任前端的方式处理
        Long orderId = requestDTO.getOrderId();
        Long outboundId = requestDTO.getOutboundId();
        // 筛选出同步入库的数据集
        List<UpdatePackingMessageRequestDTO> packingMessages = requestDTO.getPackageMessageList();
        // 这里对集合的数值进行初始化防止后面的运算操作出现npe
        packingMessages.forEach(packageMessage -> {
            packageMessage.setPackingWeight(Optional.ofNullable(packageMessage.getPackingWeight()).orElse(BigDecimal.ZERO));
            packageMessage.setPackingBoxNum(Optional.ofNullable(packageMessage.getPackingBoxNum()).orElse(0));
            packageMessage.setPackingNum(Optional.ofNullable(packageMessage.getPackingNum()).orElse(0));
            packageMessage.setPackingServings(Optional.ofNullable(packageMessage.getPackingServings()).orElse(0));
        });

        // 更新出库信息
        updateOutboundMsg(outboundId, packingMessages);

        // 筛选出非同步入库的数据集
        List<UpdatePackingMessageRequestDTO> syncInboundList = packingMessages.stream()
                .filter(t -> NeedSyncInboundEnum.YES.getKey() == t.getAddOrNotSyncInbound())
                .collect(Collectors.toList());
        // 编辑同步入库包装信息
        if (CollectionUtils.isNotEmpty(syncInboundList)) {
            updateSyncInbound(orderId, outboundId, syncInboundList);
        }

        // 筛选出非同步入库的数据集
        List<UpdatePackingMessageRequestDTO> unSyncInboundList = packingMessages.stream()
                .filter(t -> NeedSyncInboundEnum.NO.getKey() == t.getAddOrNotSyncInbound())
                .collect(Collectors.toList());
        // 编辑非同步入库包装信息
        if (CollectionUtils.isNotEmpty(unSyncInboundList)) {
            updateUnSyncInbound(orderId, outboundId, unSyncInboundList);
        }
    }

    /**
     * 编辑非同步入库的包装信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void updateUnSyncInbound(Long orderId, Long outboundId, List<UpdatePackingMessageRequestDTO> unSyncInboundList) {
        log.info("编辑非同步入库请求入参:{}", unSyncInboundList);
        // 校验当前编辑的信息是否满足库存要求，不满足则报错
        List<ProductionManageStock> stocks = unSyncInboundList.stream()
                .map(this::validStockIsLegal).collect(Collectors.toList());

        unSyncInboundList.forEach(packageMsg -> {
            String plantBatchId = packageMsg.getPlantBatchId();
            String productLevelCode = packageMsg.getProductLevelCode();
            // 添加redis锁, 因为修改多个库存信息在同一个事务中，并不保证数据的最终一致性。
            String key = ProductionManageCommonUtil.redisStockCheckSuffix(
                            RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, plantBatchId, productLevelCode);
            try {
                lockUtil.lock(key,EDIT_PACKING_MSG_FAILED);
                // 获取包装信息
                ProductionManageOutboundPackageMessage oldPackageMsg = getById(packageMsg.getId());
                CustomAssert.null2Error(oldPackageMsg, PACKING_MSG_IS_NOT_EXISTS);
                // 初始化包装信息
                oldPackageMsg.setNullToZero();
                // 更新包装信息
                updatePackingMsg(orderId, outboundId, packageMsg);
                // 获取当前库存
                ProductionManageStock stock = stocks.stream()
                        .filter(t -> t.getPlantBatchId().equals(packageMsg.getPlantBatchId())
                                && t.getProductLevelCode().equals(packageMsg.getProductLevelCode()))
                        .findFirst().orElse(null);
                // 计算修改后的数值信息
                int updateBoxNum = packageMsg.getPackingBoxNum() - oldPackageMsg.getPackingBoxNum();
                int updateNum = packageMsg.getPackingNum() - oldPackageMsg.getPackingNum();
                BigDecimal updateWeight = packageMsg.getPackingWeight().subtract(oldPackageMsg.getPackingWeight());

                // 修改库存
                stockMapper.update(stock, updateBoxNum, updateNum, updateWeight);

                // 修改库存流水
                flowDetailsMapper.updateNumber(outboundId, packageMsg.getPackingBoxNum(),
                        packageMsg.getPackingNum(), packageMsg.getPackingWeight(),
                        FlowDirectionTypeEnum.PACKAGING_OUT, commonUtil);
            } finally {
                lockUtil.releaseLock(key);
            }
        });

    }

    /**
     * 编辑同步入库的包装信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void updateSyncInbound(Long orderId, Long outboundId, List<UpdatePackingMessageRequestDTO> syncInboundList) {
        // 更新分拣入库的信息
        storageService.updateFromOutbound(syncInboundList);
        // 更新包装信息
        syncInboundList.forEach(packageMsg -> {
                // 获取包装信息
                ProductionManageOutboundPackageMessage oldPackageMsg = getById(packageMsg.getId());
                CustomAssert.null2Error(oldPackageMsg, PACKING_MSG_IS_NOT_EXISTS);
                // 初始化包装信息
                oldPackageMsg.setNullToZero();
                // 更新包装信息
                updatePackingMsg(orderId, outboundId, packageMsg);
                // 修改库存流水
                flowDetailsMapper.updateNumber(outboundId, packageMsg.getPackingBoxNum(),
                        packageMsg.getPackingNum(), packageMsg.getPackingWeight(),
                        FlowDirectionTypeEnum.PACKAGING_OUT, commonUtil);
        });
    }

    /**
     * 更新包装信息，只可编辑包装方式 + 包装规格 + 重量 + 数量 + 箱数 + 份数
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void updatePackingMsg(Long orderId, Long outboundId, UpdatePackingMessageRequestDTO requestDTO) {
        log.info("更新包装信息消息入参 => 订单id:{}, 出库id:{}，请求体:{}", orderId, outboundId, requestDTO);
        boolean isSuccess = update().set(PACKING_BOX_NUM, requestDTO.getPackingBoxNum())
                .set(PACKING_NUM, requestDTO.getPackingNum())
                .set(PACKING_WEIGHT, requestDTO.getPackingWeight())
                .set(PACKING_SERVINGS, requestDTO.getPackingServings())
                .set(PACKING_SPEC_CODE, requestDTO.getPackingSpecCode())
                .set(PACKING_SPEC_NAME, requestDTO.getPackingSpecName())
                .set(PACKING_WAY_CODE, requestDTO.getPackingWayCode())
                .set(PACKING_WAY_NAME, requestDTO.getPackingWayName())
                .eq(OLD_ID, requestDTO.getId())
                // 这里是为了防止前端恶意篡改id值造成错误更新其他的包装信息
                .eq(OUTBOUND_ID, outboundId)
                .eq(OLD_ORDER_ID, orderId)
                .update();
        CustomAssert.false2Error(isSuccess,EDIT_PACKING_MSG_FAILED);
    }

    /**
     * 更新出库信息
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private void updateOutboundMsg(Long outboundId, List<UpdatePackingMessageRequestDTO> list) {
        List<Long> ids = list.stream().map(UpdatePackingMessageRequestDTO::getId).collect(Collectors.toList());
        OutboundTotalNumberBO bo =  baseMapper.getTotalNumberByIds(ids, outboundId);
        // 汇总数值信息
        int totalQuantity = list.stream().mapToInt(UpdatePackingMessageRequestDTO::getPackingNum).sum();
        int totalBoxNum = list.stream().mapToInt(UpdatePackingMessageRequestDTO::getPackingBoxNum).sum();
        int totalServings = list.stream().mapToInt(UpdatePackingMessageRequestDTO::getPackingServings).sum();
        BigDecimal totalWeight = list.stream().map(UpdatePackingMessageRequestDTO::getPackingWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算后的数值
        int updateBoxNum = totalBoxNum - bo.getTotalBoxNum();
        int updateQuantity = totalQuantity - bo.getTotalQuantity();
        int updateServings = totalServings - bo.getTotalServings();
        BigDecimal updateWeight = totalWeight.subtract(bo.getTotalWeight());
        UpdatePmPO po = new UpdatePmPO(outboundId, updateBoxNum, updateQuantity, updateWeight, updateServings);
        outboundMapper.updateNumber(po);
    }
    /**
     * 获取未出库发货的包装信息集合
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<SearchPackingMsgResponseVO> listNotOutboundMsg(Long orderId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<ProductionManageOutboundPackageMessage> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), "o." + ProductionManageOutbound.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "o." + ProductionManageOutbound.COL_ORGANIZATION_ID, organizationId)
                .eq("o." + ProductionManageOutbound.COL_ORDER_ID, orderId)
                // 获取出库次数为0的包装信息
                .eq("pm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM, 0);
        List<SearchPackingMsgResponseVO> vos = Optional.ofNullable(baseMapper.listNotOutboundMsg(wrapper))
                .orElse(Collections.emptyList());
        // 获取分拣入库的相关信息
        vos.forEach(vo -> {
            ProductionManageSortInstorage storage = storageService.getFirstSortingByBatchIdAndLevelCode(vo.getPlantBatchId(), vo.getProductLevelCode(), SortingInventoryTypeEnum.values());
            if (Objects.isNull(storage)) {
                return;
            }
            BeanUtils.copyProperties(storage, vo, "id");
        });

        return vos;
    }

    /**
     * 通过订单主键id集合来获取最新的出库次数
     *
     * @author shixiongfei
     * @date 2019-11-28
     * @updateDate 2019-11-28
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<PackingOutboundNumBO> listMinObNumByOrderIds(List<Long> orderIds) {
        QueryWrapper<ProductionManageOutboundPackageMessage> wrapper = new QueryWrapper<>();
        wrapper.in(OLD_ORDER_ID, orderIds)
                .groupBy(OLD_ORDER_ID);

        return baseMapper.listMinObNumByOrderIds(wrapper);
    }

    /**
     * 批量更新包装信息出库次数
     *
     * @author shixiongfei
     * @date 2019-12-10
     * @updateDate 2019-12-10
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void updateOutboundTimes(List<Long> packIds, int times) {
        boolean isSuccess = update().set(OUTBOUND_NUM, times)
                .in(OLD_ID, packIds)
                .update();
        CustomAssert.false2Error(isSuccess, BATCH_UPDATE_PACKING_OUTBOUND_MSG_FAILED);
    }
}