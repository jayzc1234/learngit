package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.FlowDirectionTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockFlowDetailTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageStatisticsProductProductionData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageSortInstorage;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockFlowDetailsMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchDropDownInStorageResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortStoragePbIdResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortingStorageDetailResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSortingStorageResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.INBOUND_INVENTORY_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InStorageErrorMsgConstants.*;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-16
 */
@Slf4j
@Service
public class ProductionManageSortInstorageService extends ServiceImpl<ProductionManageSortInstorageMapper, ProductionManageSortInstorage> implements BaseService<SearchSortingStorageResponseVO> {


    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageStockService stockService;

    @Autowired
    private ProductionManageSortInstorageMapper sortInstorageMapper;

    @Autowired
    private ProductionManageStockFlowDetailsService flowDetailsService;

    @Autowired
    private ProductionManageProductRecordService recordService;

    @Autowired
    private ProductionManageStockFlowDetailsMapper flowDetailsMapper;

    @Autowired
    private MyRedisLockUtil lockUtil;

    /**
     * 新增入库记录
     * 1. 新增入库数据，
     * 2. 更新库存数据，
     * 3. 库存流水
     *
     * @param requestDTO
     * @throws SuperCodeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddSortStorageRequestDTO requestDTO) {
        log.info("新增入库记录入参:{}", requestDTO);
        // 初始化数值信息
        requestDTO.initNumber();
        // 这个redis锁应该考虑抽离，不应嵌入到业务逻辑代码中，而且不好统一管理维护, TODO 后续做统一抽离
        String key = ProductionManageCommonUtil.redisStockCheckSuffix(RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY,
                requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode());
        try {
            lockUtil.lock(key, SORTING_INBOUND_INVENTORY_FAILED);
            // 添加入库记录
            Long id = addSortStorage(requestDTO);
            // 校验库存数据，存在指定的批次和产品等级则更新库存，不存在则新增
            ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode());
            if (Objects.isNull(stock)) {
                stock = new ProductionManageStock();
                BeanUtils.copyProperties(requestDTO, stock);
                stock.setType(requestDTO.getSortingType());
                stock.setTotalInboundBoxNum(requestDTO.getBoxNum());
                stock.setTotalInboundQuantity(requestDTO.getQuantity());
                stock.setTotalInboundWeight(requestDTO.getWeight());
                stockService.add(stock);
            } else {
                stock.initStock();
                // 组装sql
                UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
                wrapper.set(TOTAL_INBOUND_BOX_NUM, requestDTO.getBoxNum() + stock.getTotalInboundBoxNum())
                        .set(TOTAL_INBOUND_QUANTITY, requestDTO.getQuantity() + stock.getTotalInboundQuantity())
                        .set(TOTAL_INBOUND_WEIGHT, requestDTO.getWeight().add(stock.getTotalInboundWeight()))
                        .set(OLD_BOX_NUM, requestDTO.getBoxNum() + stock.getBoxNum())
                        .set(OLD_QUANTITY, requestDTO.getQuantity() + stock.getQuantity())
                        .set(OLD_WEIGHT, requestDTO.getWeight().add(stock.getWeight()));
                stockService.update(stock.getId(), wrapper);
            }
            // 添加库存流水数据，这里的是否贴码统一采用不贴码(因为不存在贴不贴码的情况)
            addFlowDetails(requestDTO, id);

            // 校验类型是否为采收类型，是则添加/更新产品档案信息，不是则不做如何处理
            addOrUpdateProductRecord(requestDTO.getPlantBatchId(), requestDTO.getSortingType());
        } finally {
            // 无需重试，会在到达过期时间后自动释放
            lockUtil.releaseLock(key);
        }
    }

    /**
     * 新增入库信息（用于包装同步入库）不需要更改库存
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public void addButNotInStock(AddSortStorageRequestDTO requestDTO) {
        log.info("新增入库记录入参:{}", requestDTO);
        // 初始化数值信息
        requestDTO.initNumber();
        // 添加入库记录
        Long id = addSortStorage(requestDTO);
        // 校验库存数据，不存在则新增，存在则不做如何处理（这里应该需要修改库存的修改时间，暂不做处理）
        ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(
                requestDTO.getPlantBatchId(), requestDTO.getProductLevelCode());

        Integer boxNum = requestDTO.getBoxNum();
        Integer quantity = requestDTO.getQuantity();
        BigDecimal weight = requestDTO.getWeight();
        if (Objects.isNull(stock)) {
            stock = new ProductionManageStock();
            BeanUtils.copyProperties(requestDTO, stock);
            stock.setType(requestDTO.getSortingType());
            stock.setTotalInboundBoxNum(boxNum);
            stock.setTotalInboundQuantity(quantity);
            stock.setTotalInboundWeight(weight);
            stock.setTotalOutboundBoxNum(boxNum);
            stock.setTotalOutboundQuantity(quantity);
            stock.setTotalOutboundWeight(weight);
            stock.setBoxNum(0);
            stock.setQuantity(0);
            stock.setWeight(BigDecimal.ZERO);
            stockService.add(stock);
        } else {
            // 更新总入库总出库信息
            stockService.updateInboundAndOutbound(stock, boxNum, quantity, weight);
        }

        // 添加库存流水数据
        addFlowDetails(requestDTO, id);

        // 校验类型是否为采收类型，是则添加/更新产品档案信息，不是则不做如何处理
        addOrUpdateProductRecord(requestDTO.getPlantBatchId(), requestDTO.getSortingType());
    }

    /**
     * 修改流水重量
     *
     * @param businessId
     * @param newWeight
     * @throws SuperCodeException
     */
    private void updateFlowDetails(Long businessId, BigDecimal newWeight) {
        // 获取流水信息数据，修改流水信息
        ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
        flowDetails.setBusinessId(businessId);
        flowDetails.setOutInWeight(newWeight);
        flowDetailsService.updateWeight(flowDetails);
    }

    /**
     * 获取分拣入库信息列表
     *
     * @param daosearch 请求dto
     * @return
     * @throws SuperCodeException
     */
    @Override
    public PageResults<List<SearchSortingStorageResponseVO>> list(DaoSearch daosearch) throws SuperCodeException {
        SearchSortingStorageRequestDTO requestDTO = (SearchSortingStorageRequestDTO) daosearch;

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManageSortInstorage> iPage = baseMapper.list(requestDTO, sysId, organizationId, commonUtil);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageSortInstorage> records = Optional.ofNullable(iPage.getRecords())
                .orElse(Collections.emptyList());

        List<SearchSortingStorageResponseVO> list = records.stream()
                .map(record -> {
                    // 初始化分拣入库实体类，防止出现null值
                    record.initSortStorage();
                    SearchSortingStorageResponseVO responseVO = new SearchSortingStorageResponseVO();
                    BeanUtils.copyProperties(record, responseVO);
                    responseVO.setSortingType(record.getType().toString());
                    return responseVO;
                }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);

    }

    /**
     * 通过批次类型来获取分拣入库的批次+产品信息集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-27
     * @updateDate 2019-11-27
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchDropDownInStorageResponseVO>> listBatchByType(ProductInStorageDropDownDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<ProductionManageSortInstorage> iPage = baseMapper.listBatchByType(requestDTO, sysId, organizationId);

        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManageSortInstorage> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchDropDownInStorageResponseVO> list = records.stream().map(record -> {
            SearchDropDownInStorageResponseVO responseVO = new SearchDropDownInStorageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    @Override
    public List<SearchSortingStorageResponseVO> listExcelByIds(List<? extends Serializable> ids) {
        List<ProductionManageSortInstorage> storages = baseMapper.selectBatchIds(ids);
        return storages.stream().map(record -> {
            // 初始化分拣入库实体类，防止出现null值
            record.initSortStorage();
            SearchSortingStorageResponseVO responseVO = new SearchSortingStorageResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setSortingType(record.getType().toString());
            return responseVO;
        }).collect(Collectors.toList());
    }


    @Override
    public void dataTransfer(List<SearchSortingStorageResponseVO> list) {
        if (null != list && !list.isEmpty()) {
            try {
                for (SearchSortingStorageResponseVO sortingStorageResponseVO : list) {
                    sortingStorageResponseVO.setSortingType(SortingInventoryTypeEnum.getValue(sortingStorageResponseVO.getSortingType()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 添加分拣入库信息， 返回添加的主键
     */
    private Long addSortStorage(AddSortStorageRequestDTO requestDTO) {
        ProductionManageSortInstorage sortStorage = new ProductionManageSortInstorage();
        BeanUtils.copyProperties(requestDTO, sortStorage);
        sortStorage.setType(requestDTO.getSortingType());
        // 校验是否为外采分拣，如果为外采分拣，则批次id和批次名称保持一致
        boolean isTrue = requestDTO.getSortingType() == SortingInventoryTypeEnum.OUTSIDE_SORTING.getKey();
        if (isTrue) {
            sortStorage.setPlantBatchName(requestDTO.getPlantBatchId());
        }
        sortStorage.setCreateUserId(commonUtil.getEmployee().getEmployeeId());
        sortStorage.setCreateUserName(commonUtil.getEmployee().getName());

        baseMapper.insert(sortStorage);
        return sortStorage.getId();
    }

    /**
     * 获取分拣入库详情信息
     *
     * @param id 分拣入库主键id
     * @return
     */
    public SearchSortingStorageDetailResponseVO getDetail(Long id) {
        ProductionManageSortInstorage sortStorage = baseMapper.selectById(id);
        // 初始化分拣入库的量值，防止出现null
        sortStorage.initSortStorage();
        SearchSortingStorageDetailResponseVO responseVO = new SearchSortingStorageDetailResponseVO();
        BeanUtils.copyProperties(sortStorage, responseVO);
        responseVO.setSortingType(sortStorage.getType().toString());
        return responseVO;
    }

    /**
     * 通过批次id和产品等级code来获取分拣入库信息列表集合
     *
     * @param plantBatchId     批次id
     * @param productLevelCode 产品等级code
     * @return
     * @throws SuperCodeException
     */
    public List<ProductionManageSortInstorage> selectByPlantBatchIdAndProductLevelCode(String plantBatchId, String productLevelCode, SortingInventoryTypeEnum... typeEnums) {
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(OLD_PRODUCT_LEVEL_CODE, productLevelCode)
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .in(OLD_INBOUND_TYPE, Stream.of(typeEnums).map(typeEnum -> (byte) typeEnum.getKey()).collect(Collectors.toList()));
        return sortInstorageMapper.selectList(queryWrapper);
    }

    /**
     * 通过批次id和产品等级code来获取第一次入库信息
     *
     * @param plantBatchId     批次id
     * @param productLevelCode 产品等级code
     * @return
     */
    public ProductionManageSortInstorage getFirstSortingByBatchIdAndLevelCode(String plantBatchId, String productLevelCode, SortingInventoryTypeEnum... typeEnums) {
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(OLD_PRODUCT_LEVEL_CODE, productLevelCode);
        queryWrapper.eq(OLD_PLANT_BATCH_ID, plantBatchId);
        queryWrapper.eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq(OLD_SYS_ID, commonUtil.getSysId());
        queryWrapper.in(OLD_INBOUND_TYPE, Stream.of(typeEnums).map(typeEnum -> (byte) typeEnum.getKey()).collect(Collectors.toList()));
        queryWrapper.orderByAsc(OLD_CREATE_DATE);
        List<ProductionManageSortInstorage> list = sortInstorageMapper.selectList(queryWrapper);
        CustomAssert.empty2Error(list, CURRENT_PLANT_AND_LEVEL_INBOUND_MSG_IS_NULL);
        return list.get(0);
    }

    /**
     * 通过种植批次id获取分拣入库信息列表
     *
     * @param requestDTO
     * @return
     */
    public PageResults<List<SearchSortStoragePbIdResponseVO>> listByPlantBatchId(SearchSortStorageByPbIdRequestDTO requestDTO) {
        Page<ProductionManageSortInstorage> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        IPage<ProductionManageSortInstorage> iPage = query().eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_PLANT_BATCH_ID, requestDTO.getPlantBatchId()).page(page);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        PageResults<List<SearchSortStoragePbIdResponseVO>> pageResults = new PageResults<>();
        pageResults.setPagination(pagination);
        List<ProductionManageSortInstorage> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResults;
        }

        List<SearchSortStoragePbIdResponseVO> list = records.stream().map(record -> {
            SearchSortStoragePbIdResponseVO responseVO = new SearchSortStoragePbIdResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            return responseVO;
        }).collect(Collectors.toList());

        pageResults.setList(list);

        return pageResults;
    }

    /**
     * 分拣入库导出
     *
     * @param requestDTO
     * @param response
     */
    public void exportExcel(SearchSortingStorageRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<String> idList = requestDTO.getIdList();

        List<SearchSortingStorageResponseVO> list;
        // idList为空导出全部，不为空导出指定数据
        if (CollectionUtils.isEmpty(idList)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            PageResults<List<SearchSortingStorageResponseVO>> pageResults = list(requestDTO);
            list = pageResults.getList();
        } else {
            list = listExcelByIds(idList);
        }

        list.forEach(vo -> vo.setSortingType(SortingInventoryTypeEnum.getValue(vo.getSortingType())));

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), INBOUND_INVENTORY_LIST, response);
    }

    /**
     * 添加库存流水，流水添加失败时，不可阻拦正常流程的进行
     *
     * @param requestDTO
     * @param id
     */
    @Transactional
    protected void addFlowDetails(AddSortStorageRequestDTO requestDTO, Long id) {
        // 添加库存流水信息
        ProductionManageStockFlowDetails stockFlow = new ProductionManageStockFlowDetails();
        stockFlow.setOutInBoxNum(requestDTO.getBoxNum());
        stockFlow.setOutInNum(requestDTO.getQuantity());
        stockFlow.setOutInType(StockFlowDetailTypeEnum.getTypeBySortType(requestDTO.getSortingType()).getKey());
        stockFlow.setOutInWeight(requestDTO.getWeight());
        stockFlow.setPlantBatchId(requestDTO.getPlantBatchId());
        stockFlow.setProductLevelCode(requestDTO.getProductLevelCode());
        stockFlow.setBusinessId(id);
        try {
            flowDetailsService.add(stockFlow);
        } catch (Exception e) {
            log.error(ADD_INVENTOR_FLOW_FAILED, e);
        }
    }

    /**
     * 当前入库数据如果为采收分拣，则同步更新产品档案信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    protected void addOrUpdateProductRecord(String plantBatchId, Byte sortingType) {
        // 校验类型是否为采收类型，是则添加/更新产品档案信息，不是则不做如何处理
        if (SortingInventoryTypeEnum.HARVEST_SORTING.getKey() == sortingType) {
            // 添加产品档案信息, 校验是否存在此产品档案信息，不存在则新增，存在则更新
            recordService.sortAddRecord(plantBatchId);
        }
    }

    /**
     * 通过批次id, 产品等级code, 分拣类型来获取分拣入库信息
     *
     * @return
     */
    public List<ProductionManageSortInstorage> listByBatchIdAndLevelCode(String plantBatchId, String productLevelCode) {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return query().eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, commonUtil.getSysId())
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(OLD_PRODUCT_LEVEL_CODE, productLevelCode)
                .list();
    }

    /**
     * 通过批次id获取入库总重量,此方法仅用于定时任务
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-16
     * @updateDate 2019-10-16
     * @updatedBy shixiongfei
     */
    public BigDecimal getInboundWeight(String plantBatchId, String sysId, String organizationId) {
        QueryWrapper<ProductionManageSortInstorage> wrapper = new QueryWrapper<>();
        wrapper.eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .eq(OLD_INBOUND_TYPE, SortingInventoryTypeEnum.HARVEST_SORTING.getKey())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId);
        BigDecimal inboundWeight = baseMapper.getInboundWeight(wrapper);
        return Objects.isNull(inboundWeight) ? BigDecimal.ZERO : inboundWeight;
    }

    /**
     * 通过批次信息获取分拣入库重量等相关信息
     * 该方法仅用于数据统计模块
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-29
     * @updateDate 2019-10-29
     * @updatedBy shixiongfei
     */
    public List<ProductionManageStatisticsProductProductionData> listByBatchId(String plantBatchId, String sysId, String organizationId) {
        List<ProductionManageSortInstorage> list = query().select("SUM(IFNULL(Weight, 0)) AS weight",
                "ProductLevelCode AS productLevelCode", "ProductLevelName AS productLevelName", "IFNULL(SUM(BoxNum), 0) AS boxNum")
                .eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(plantBatchId), OLD_PLANT_BATCH_ID, plantBatchId)
                .groupBy(OLD_PRODUCT_ID, OLD_PRODUCT_LEVEL_CODE)
                .list();

        list = net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils.elementIsNull(list);

        return list.stream().map(storage -> {
            ProductionManageStatisticsProductProductionData data = new ProductionManageStatisticsProductProductionData();
            BeanUtils.copyProperties(storage, data);
            data.setInboundWeight(storage.getWeight());
            data.setPackingBoxNum(storage.getBoxNum());
            return data;
        }).collect(Collectors.toList());

    }

    /**
     * 获取指定批次下的分拣入库的装箱数集合
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-18
     * @updateDate 2019-11-18
     * @updatedBy shixiongfei
     */
    public Integer getInboundBoxNum(String plantBatchId, String sysId, String organizationId) {
        QueryWrapper<ProductionManageSortInstorage> wrapper = new QueryWrapper<>();
        wrapper.eq(OLD_SYS_ID, sysId)
                .eq(OLD_ORGANIZATION_ID, organizationId)
                .eq(OLD_INBOUND_TYPE, SortingInventoryTypeEnum.HARVEST_SORTING.getKey())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId);
        return baseMapper.getInboundBoxNum(wrapper);
    }

    /**
     * 通过批次id和产品等级code获取入库信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public ProductionManageSortInstorage getByBatchIdAndLevelCode(String plantBatchId, String productLevelCode) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManageSortInstorage> list = query().eq(StringUtils.isNotBlank(sysId), OLD_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), OLD_SYS_ID, organizationId)
                .eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(OLD_PRODUCT_LEVEL_CODE, productLevelCode)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        return list.get(0);
    }

    /**
     * 根据包装出库来修改分拣入库的信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     */
    public void updateFromOutbound(List<UpdatePackingMessageRequestDTO> list) {
        list.forEach(msg -> {
            // 获取分拣入库信息
            ProductionManageSortInstorage inboundMsg = baseMapper.getByPackingId(msg.getId(), commonUtil);
            CustomAssert.null2Error(inboundMsg, INBOUND_MSG_IS_NOT_EXISTS);
            // 初始化入库相关数值信息，防止出现npe
            inboundMsg.initSortStorage();
            // 更新入库信息
            baseMapper.updateNumber(inboundMsg, msg.getPackingBoxNum(), msg.getPackingNum(), msg.getPackingWeight());
            // 更新入库的库存流水
            flowDetailsMapper.updateNumber(inboundMsg.getId(), msg.getPackingBoxNum(), msg.getPackingNum(),
                    msg.getPackingWeight(), FlowDirectionTypeEnum.SORTING_IN, commonUtil);
        });

    }

    /**
     * 编辑无码分拣入库信息
     *
     * @param requestDTO
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateCodeLessSortingStorageRequestDTO requestDTO) throws SuperCodeException {
        // 获取修改前的重量
        QueryWrapper<ProductionManageSortInstorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq(OLD_ID, requestDTO.getId());
        ProductionManageSortInstorage sortStorage = baseMapper.selectOne(queryWrapper);
        CustomAssert.isNull(sortStorage, INBOUND_MSG_IS_NOT_EXISTS);

        // 获取修改前的重量
        BigDecimal weight = Optional.ofNullable(sortStorage.getWeight()).orElse(BigDecimal.ZERO);
        // 获取即将修改的重量
        BigDecimal newWeight = Optional.ofNullable(requestDTO.getWeight()).orElse(BigDecimal.ZERO);
        // 获取修改前的箱数
        Integer boxNum = Optional.ofNullable(sortStorage.getBoxNum()).orElse(0);
        // 获取即将修改的箱数
        Integer newBoxNum = Optional.ofNullable(requestDTO.getBoxNum()).orElse(0);
        // 获取修改前的个数
        Integer quantity = Optional.ofNullable(sortStorage.getQuantity()).orElse(0);
        // 获取即将修改的个数
        Integer newQuantity = Optional.ofNullable(requestDTO.getQuantity()).orElse(0);

        update().set(OLD_BOX_NUM, newBoxNum)
                .set(OLD_QUANTITY, newQuantity)
                .set(OLD_WEIGHT, newWeight)
                .eq(OLD_ID, requestDTO.getId())
                .update();

        // 获取流水信息数据，修改流水信息
        ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
        flowDetails.setBusinessId(requestDTO.getId());
        flowDetails.setOutInWeight(newWeight);
        flowDetails.setOutInBoxNum(newBoxNum);
        flowDetails.setOutInNum(newQuantity);

        flowDetailsService.updateWeight(flowDetails);

        String key = ProductionManageCommonUtil.redisStockCheckSuffix(RedisKey.STORAGE_MANAGE_STOCK_UPDATE_KEY, sortStorage.getPlantBatchId(), sortStorage.getProductLevelCode());
        try {
            lockUtil.lock(key, UPDATE_INVENTORY_FAILED);
            // 获取库存消息，修改库存重量
            ProductionManageStock stock = stockService.getByBatchIdAndLevelCode(sortStorage.getPlantBatchId(), sortStorage.getProductLevelCode());
            // 初始化库存实体类，防止npe
            stock.initStock();
            // 拼接sql
            UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
            // 库存箱数的计算逻辑为： 当前库存箱数 - 修改前的库存箱数 + 修改后的库存箱数
            wrapper.set(OLD_BOX_NUM, Optional.of(stock.getBoxNum() - boxNum + newBoxNum).filter(num -> num >= 0).orElse(0))
                    // 库存个数的计算逻辑为： 当前库存个数 - 修改前的库存个数 + 修改后的库存个数
                    .set(OLD_QUANTITY, Optional.of(stock.getQuantity() - quantity + newQuantity).filter(num -> num >= 0).orElse(0))
                    // 库存重量的计算逻辑为：当前库存重量 - 修改前库存重量 + 修改后库存重量
                    .set(OLD_WEIGHT, Optional.of(stock.getWeight().subtract(weight).add(newWeight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                    .set(TOTAL_INBOUND_BOX_NUM, Optional.of(stock.getTotalInboundBoxNum() - boxNum + newBoxNum).filter(num -> num >= 0).orElse(0))
                    .set(TOTAL_INBOUND_QUANTITY, Optional.of(stock.getTotalInboundQuantity() - quantity + newQuantity).filter(num -> num >= 0).orElse(0))
                    .set(TOTAL_INBOUND_WEIGHT, Optional.of(stock.getTotalInboundWeight().subtract(weight).add(newWeight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO));
            stockService.update(stock.getId(), wrapper);
        } finally {
            lockUtil.releaseLock(key);
        }
    }

}