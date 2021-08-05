package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import io.netty.util.internal.MathUtil;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddCodeLessStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddTILRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ConfirmCodeLessStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockLossRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.ProductionDataStatisticsEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.LossConfirmationStatusEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockFlowDetailTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockLossOperateTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockLoss;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageProductDataStatisticsService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockLossDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockLossResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InventoryLossConstants.DAMAGE_DATE;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InventoryLossConstants.OPERATE_TYPE;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.INVENTORY_LOSS_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryLossErrorMsgConstants.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * <p>
 * 库存报损 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@Slf4j
@Service
public class ProductionManageStockLossService extends ServiceImpl<ProductionManageStockLossMapper, ProductionManageStockLoss> implements BaseService<SearchStockLossResponseVO> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSortInstorageService storageService;

    @Autowired
    private ProductionManageStockService stockService;

    @Autowired
    private ProductionManageStockFlowDetailsService flowDetailsService;

    @Autowired
    private ProductionManageProductRecordService recordService;

    @Autowired
    private ProductionManageProductDataStatisticsService dataStatisticsService;

    @Autowired
    private MyRedisLockUtil lockUtil;

    /**
     * 获取库存报损列表
     *
     * @param daoSearch
     * @return
     * @throws SuperCodeException
     */
    @Override
    public PageResults<List<SearchStockLossResponseVO>> list(DaoSearch daoSearch) throws SuperCodeException {
        SearchStockLossRequestDTO requestDTO = (SearchStockLossRequestDTO) daoSearch;

        IPage<ProductionManageStockLoss> iPage = baseMapper.list(requestDTO, commonUtil);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        PageResults<List<SearchStockLossResponseVO>> pageResults = new PageResults<>();
        pageResults.setPagination(pagination);
        List<ProductionManageStockLoss> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResults;
        }

        List<SearchStockLossResponseVO> list = records.stream().map(record -> {
            // 初始化数值
            record.initStockLoss(record);
            SearchStockLossResponseVO responseVO = new SearchStockLossResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setType(record.getType().toString());
            responseVO.setOperateType(record.getOperateType().toString());
            responseVO.setLossConfirmationStatus(record.getLossConfirmationStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());

        pageResults.setList(list);

        return pageResults;
    }

    @Override
    public List<SearchStockLossResponseVO> listExcelByIds(List<? extends Serializable> ids) {
        List<ProductionManageStockLoss> list = query().in(OLD_ID, ids).list();
        return list.stream().map(entity -> {
            // 初始化数值
            entity.initStockLoss(entity);
            SearchStockLossResponseVO responseVO = new SearchStockLossResponseVO();
            BeanUtils.copyProperties(entity, responseVO);
            responseVO.setType(entity.getType().toString());
            responseVO.setLossConfirmationStatus(entity.getLossConfirmationStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void dataTransfer(List<SearchStockLossResponseVO> list) {
        // do nothing
    }

    /**
     * 获取库存报损详情
     *
     * @author shixiongfei
     * @date 2019-11-27
     * @updateDate 2019-11-27
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public SearchStockLossDetailResponseVO detail(Long id) throws SuperCodeException {
        QueryWrapper<ProductionManageStockLoss> queryWrapper = commonUtil.queryTemplate(ProductionManageStockLoss.class);
        queryWrapper.eq(OLD_ID, id);
        ProductionManageStockLoss stockLoss = baseMapper.selectOne(queryWrapper);
        CustomAssert.isNull(stockLoss, CURRENT_INVENTORY_LOSS_MSG_IS_NOT_EXISTS);

        //初始化数值
        stockLoss.initStockLoss(stockLoss);

        SearchStockLossDetailResponseVO responseVO = new SearchStockLossDetailResponseVO();
        BeanUtils.copyProperties(stockLoss, responseVO);
        responseVO.setSortingType(stockLoss.getType().toString());

        return responseVO;
    }

    /**
     * 添加通用属性
     *
     * @param stockLoss
     * @return
     * @throws SuperCodeException
     */
    private ProductionManageStockLoss getStockLoss(ProductionManageStockLoss stockLoss) {
        Employee employee = commonUtil.getEmployee();
        stockLoss.setSysId(commonUtil.getSysId());
        stockLoss.setOrganizationId(commonUtil.getOrganizationId());
        stockLoss.setCreateUserId(employee.getEmployeeId());
        stockLoss.setCreateUserName(employee.getName());
        stockLoss.setDamageDate(new Date());
        return stockLoss;
    }

    /**
     * 添加库存报损
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-07
     * @updateDate 2019-09-07
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void codeLessDamage(AddCodeLessStockLossRequestDTO requestDTO) throws SuperCodeException {
        // 初始化请求体相关参数
        Integer reqBoxNum = Optional.ofNullable(requestDTO.getHandleBoxNum()).orElse(0);
        Integer reqQuantity = Optional.ofNullable(requestDTO.getDamageNum()).orElse(0);
        BigDecimal reqWeight = Optional.ofNullable(requestDTO.getDamageWeight()).orElse(BigDecimal.ZERO);

        // 校验库存的相关数据是否支持报损
        validStockCan2Damage(requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode(), reqBoxNum, reqQuantity, reqWeight);

        // 获取首次分拣入库时间下的分拣入库信息
        ProductionManageSortInstorage storage = storageService.getFirstSortingByBatchIdAndLevelCode(
                requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode(), SortingInventoryTypeEnum.values());

        // 添加库存报损信息
        ProductionManageStockLoss stockLoss = new ProductionManageStockLoss();
        BeanUtils.copyProperties(storage, stockLoss);
        getStockLoss(stockLoss);
        stockLoss.setDamageWeight(requestDTO.getDamageWeight());
        stockLoss.setDamageNum(requestDTO.getDamageNum());
        stockLoss.setHandleBoxNum(requestDTO.getHandleBoxNum());
        stockLoss.setType((int) storage.getType());
        stockLoss.setFirstStorageDate(storage.getCreateDate());
        stockLoss.setLossConfirmationStatus(LossConfirmationStatusEnum.TO_BE_CONFIRMED.getKey());
        stockLoss.setDepartmentId(requestDTO.getDepartmentId());
        stockLoss.setDepartmentName(requestDTO.getDepartmentName());
        stockLoss.setOperateType(StockLossOperateTypeEnum.STOCK_LOSS.getKey());
        stockLoss.setReasonDescription(requestDTO.getReasonDescription());
        CustomAssert.isGreaterThan0(baseMapper.insert(stockLoss), ADD_INVENTORY_LOSS_FAILED);
    }

    /**
     * 报损确认
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-07
     * @updateDate 2019-09-07
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmCodeLessStockLoss(ConfirmCodeLessStockLossRequestDTO requestDTO) throws SuperCodeException {
        // 获取库存报损详情
        ProductionManageStockLoss stockLoss = getById(requestDTO.getId());

        // 初始化请求体的数字参数
        requestDTO.setDamageNum(Optional.ofNullable(requestDTO.getDamageNum()).orElse(0));
        requestDTO.setHandleBoxNum(Optional.ofNullable(requestDTO.getHandleBoxNum()).orElse(0));
        requestDTO.setDamageWeight(Optional.ofNullable(requestDTO.getDamageWeight()).orElse(BigDecimal.ZERO));

        // 更新库存
        String key = ProductionManageCommonUtil.redisStockCheckSuffix(
                RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, stockLoss.getPlantBatchId(), stockLoss.getProductLevelCode());
        try {
            lockUtil.lock(key, INVENTORY_LOSS_FAILED);

            // 校验库存是否满足报损需求
            ProductionManageStock stock = validStockCan2Damage(requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode(),
                    requestDTO.getHandleBoxNum(), requestDTO.getDamageNum(), requestDTO.getDamageWeight());
            // 更新库存报损信息
            ProductionManageStockLoss entity = new ProductionManageStockLoss();
            BeanUtils.copyProperties(requestDTO, entity);
            entity.setLossConfirmationStatus(LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED.getKey());
            CustomAssert.isSuccess(updateById(entity), UPDATE_INVENTORY_LOSS_FAILED);

            // 添加库存流水
            ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
            flowDetails.setBusinessId(stockLoss.getId());
            flowDetails.setOutInNum(requestDTO.getDamageNum());
            flowDetails.setOutInBoxNum(requestDTO.getHandleBoxNum());
            flowDetails.setOutInWeight(requestDTO.getDamageWeight());
            flowDetails.setPlantBatchId(requestDTO.getPlantBatchId());
            flowDetails.setProductLevelCode(requestDTO.getProductLevelCode());
            flowDetails.setOutInType(StockFlowDetailTypeEnum.DAMAGE_OUT.getKey());
            flowDetailsService.add(flowDetails);

            // 更新产品档案库存报损信息
            recordService.updateStockLossWeight(requestDTO.getPlantBatchId(), requestDTO.getDamageWeight());

            // 新增库存报损生产数据统计信息
            dataStatisticsService.add(requestDTO.getDamageWeight(), stockLoss.getDamageDate(), ProductionDataStatisticsEnum.STOCK_LOSSES_WEIGHT);

            // 更新库存
            UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
            wrapper.set(OLD_QUANTITY, Optional.of(stock.getQuantity() - requestDTO.getDamageNum()).filter(num -> num >= 0).orElse(0))
                    .set(OLD_BOX_NUM, Optional.of(stock.getBoxNum() - requestDTO.getHandleBoxNum()).filter(num -> num >= 0).orElse(0))
                    .set(OLD_WEIGHT, Optional.of(stock.getWeight().subtract(requestDTO.getDamageWeight())).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                    .set(TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + requestDTO.getDamageNum())
                    .set(TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + requestDTO.getHandleBoxNum())
                    .set(TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(requestDTO.getDamageWeight()));
            stockService.update(stock.getId(), wrapper);
        } finally {
            lockUtil.releaseLock(key);
        }

    }

    /**
     * 校验库存的相关数据是否支持报损
     *
     * @param
     * @return 返回库存信息
     * @author shixiongfei
     * @date 2019-09-07
     * @updateDate 2019-09-07
     * @updatedBy shixiongfei
     */
    private ProductionManageStock validStockCan2Damage(String plantBatchId, String productLevelCode,
                                                       Integer boxNum, Integer quantity, BigDecimal weight) throws SuperCodeException {
        // 通过批次id, 产品等级id获取库存信息
        ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(plantBatchId, productLevelCode);
        CustomAssert.isNull(stock, CURRENT_PLANT_LEVEL_STOCK_MSG_IS_NOT_EXISTS);
        // 初始化库存相关参数
        stock.initStock();

        // 初始化请求体相关参数
        Integer reqBoxNum = Optional.ofNullable(boxNum).orElse(0);
        Integer reqQuantity = Optional.ofNullable(quantity).orElse(0);
        BigDecimal reqWeight = Optional.ofNullable(weight).orElse(BigDecimal.ZERO);

        if (MathUtil.compare(stock.getBoxNum(), reqBoxNum) == -1) {
            CustomAssert.throwException(String.format(LOSS_BOX_NUM_CAN_NOT_GREATER_THAN_INVENTORY_BOX_NUM, stock.getBoxNum()));
        }

        if (MathUtil.compare(stock.getQuantity(), reqQuantity) == -1) {
            CustomAssert.throwException(String.format(LOSS_QUANTITY_CAN_NOT_GREATER_THAN_INVENTORY_QUANTITY, stock.getQuantity()));
        }

        if (stock.getWeight().subtract(reqWeight).signum() == -1) {
            CustomAssert.throwException(String.format(LOSS_WEIGHT_CAN_NOT_GREATER_THAN_INVENTORY_WEIGHT, stock.getWeight()));
        }

        return stock;
    }


    public List<ProductionManageStockLoss> getCurrentStatisticsMsg(String startDate, String endDate, String sysId, String organizationId) {
        List<ProductionManageStockLoss> list =
                query().select(ProductionManageStockLoss.COL_PLANT_BATCH_ID, ProductionManageStockLoss.COL_PLANT_BATCH_NAME,
                        ProductionManageStockLoss.COL_DAMAGE_DATE, ProductionManageStockLoss.COL_DEPARTMENT_ID, ProductionManageStockLoss.COL_DEPARTMENT_NAME,
                        ProductionManageStockLoss.COL_TYPE, "SUM(IFNULL(" + ProductionManageStockLoss.COL_DAMAGE_WEIGHT + ", 0)) AS damageWeight")
                        .eq(OLD_SYS_ID, sysId)
                        .eq(OLD_ORGANIZATION_ID, organizationId)
                        .ge(DAMAGE_DATE, startDate)
                        .lt(DAMAGE_DATE, endDate)
                        .eq(OPERATE_TYPE, StockLossOperateTypeEnum.STOCK_LOSS.getKey())
                        .groupBy(OLD_INBOUND_TYPE, OLD_PLANT_BATCH_ID, OLD_DEPARTMENT_ID)
                        .list();
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    /**
     * 全部库存报损, 报损完确认状态记为已确认
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void totalInventoryLoss(AddTILRequestDTO requestDTO) throws SuperCodeException {

        // 获取redis锁，对接下来的操作进行排他
        String key = ProductionManageCommonUtil.redisStockCheckSuffix(
                RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode());
        try {
            lockUtil.lock(key, ADD_INVENTORY_LOSS_FAILED);

            ProductionManageStock stock = stockService.getById(requestDTO.getId());
            // 校验当前库存的数值信息是否存在，都为0或为null则不进行库存报损
            Integer boxNum = NumberUtil.initIntegerNum(stock.getBoxNum());
            Integer quantity = NumberUtil.initIntegerNum(stock.getQuantity());
            BigDecimal weight = NumberUtil.initBigdecimalNum(stock.getWeight());
            if (boxNum <= 0 && quantity <= 0 && weight.signum() <= 0) {
                CustomAssert.throwExtException(CAN_NOT_PERFORM_FULL_INVENTORY_LOSS);
            }
            // 获取首次分拣入库时间下的分拣入库信息
            ProductionManageSortInstorage storage = storageService.getFirstSortingByBatchIdAndLevelCode(stock.getPlantBatchId(),
                    stock.getProductLevelCode(), SortingInventoryTypeEnum.getEnum(stock.getType()));

            // 添加库存报损信息
            ProductionManageStockLoss stockLoss = new ProductionManageStockLoss();
            BeanUtils.copyProperties(storage, stockLoss);
            getStockLoss(stockLoss);
            stockLoss.setOperateType(StockLossOperateTypeEnum.STOCK_LOSS.getKey());
            stockLoss.setDamageWeight(weight);
            stockLoss.setDamageNum(quantity);
            stockLoss.setHandleBoxNum(boxNum);
            stockLoss.setType((int) stock.getType());
            stockLoss.setFirstStorageDate(storage.getCreateDate());
            stockLoss.setLossConfirmationStatus(LossConfirmationStatusEnum.HAS_BEEN_CONFIRMED.getKey());
            stockLoss.setDepartmentId(requestDTO.getDepartmentId());
            stockLoss.setDepartmentName(requestDTO.getDepartmentName());
            CustomAssert.isGreaterThan0(baseMapper.insert(stockLoss), ADD_INVENTORY_LOSS_FAILED);

            // 更新库存，将库存数值清0
            UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
            wrapper.set(OLD_QUANTITY, ZERO)
                    .set(OLD_BOX_NUM, ZERO)
                    .set(OLD_WEIGHT, BigDecimal.ZERO)
                    .set(TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + quantity)
                    .set(TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + boxNum)
                    .set(TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(weight));
            stockService.update(stock.getId(), wrapper);

            // 更新产品档案库存报损信息
            recordService.updateStockLossWeight(stock.getPlantBatchId(), weight);

            // 新增库存报损生产数据统计信息
            dataStatisticsService.add(weight, stockLoss.getDamageDate(), ProductionDataStatisticsEnum.STOCK_LOSSES_WEIGHT);

            // 添加库存流水
            ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
            flowDetails.setBusinessId(stockLoss.getId());
            flowDetails.setOutInNum(quantity);
            flowDetails.setOutInBoxNum(boxNum);
            flowDetails.setOutInWeight(weight);
            flowDetails.setPlantBatchId(requestDTO.getPlantBatchId());
            flowDetails.setProductLevelCode(requestDTO.getProductLevelCode());
            flowDetails.setOutInType(StockFlowDetailTypeEnum.TOTAL_INVENTORY_LOSS.getKey());
            flowDetailsService.add(flowDetails);

        } finally {
            lockUtil.releaseLock(key);
        }
    }

    /**
     * 库存报损列表导出
     *
     * @author shixiongfei
     * @date 2019-11-14
     * @updateDate 2019-11-14
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportExcel(SearchStockLossRequestDTO listDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = listDTO.getIdList();
        List<SearchStockLossResponseVO> list;
        if (CollectionUtils.isEmpty(idList)) {
            listDTO.setCurrent(1);
            listDTO.setPageSize(commonUtil.getExportNumber());
            list = list(listDTO).getList();

        } else {
            list = excelByIds(idList);
        }

        list.forEach(vo -> {
            int operateType = Integer.valueOf(vo.getOperateType());
            vo.setLossConfirmationStatus(StockLossOperateTypeEnum.STOCK_LOSS.getKey() == operateType
                    ? LossConfirmationStatusEnum.getValue(Byte.valueOf(vo.getLossConfirmationStatus()))
                    : EMPTY);
            vo.setOperateType(StockLossOperateTypeEnum.getValue(operateType));
        });

        ExcelUtils.listToExcel(list, listDTO.exportMetadataToMap(), INVENTORY_LOSS_LIST, response);
    }

    /**
     * 通过id集合获取库存报损信息列表
     *
     * @author shixiongfei
     * @date 2019-11-14
     * @updateDate 2019-11-14
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private List<SearchStockLossResponseVO> excelByIds(List<String> idList) {
        List<ProductionManageStockLoss> list = baseMapper.selectBatchIds(idList);
        return list.stream().map(record -> {
            // 初始化数值
            record.initStockLoss(record);
            SearchStockLossResponseVO responseVO = new SearchStockLossResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setType(record.getType().toString());
            responseVO.setOperateType(record.getOperateType().toString());
            responseVO.setLossConfirmationStatus(record.getLossConfirmationStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());
    }
}