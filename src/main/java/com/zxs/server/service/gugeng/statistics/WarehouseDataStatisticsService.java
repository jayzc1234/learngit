package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import jxl.write.WritableSheet;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchSortInstorageStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SearchWarehouseDataStatisticsRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.*;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.WarehouseDataStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageOutboundPackageMessageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageSortInstorageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockLossMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.NameValueVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchSortInstorageStatisticsResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchWarehouseDataStatisticsResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author shixiongfei
 * @date 2019-09-21
 * @since v1.3
 */
@Service
public class WarehouseDataStatisticsService extends ServiceImpl<WarehouseDataStatisticsMapper, ProductionManageStock> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageSortInstorageMapper storageMapper;

    @Autowired
    private ProductionManageOutboundPackageMessageMapper messageMapper;

    @Autowired
    private ProductionManageStockLossMapper stockLossMapper;

    /**
     * 获取仓储数据信息
     *
     * @param requestDTO 请求dto
     * @return 仓储数据响应vo
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     */
    public SearchWarehouseDataStatisticsResponseVO listStatistics(SearchWarehouseDataStatisticsRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        SearchWarehouseDataStatisticsResponseVO responseVO = new SearchWarehouseDataStatisticsResponseVO();
        // 获取总库存重量
        responseVO.setCurrentStockWeight(baseMapper.getTotalWeight(requestDTO, sysId, organizationId));
        /*
        统计当前时间区间下的总入库重量和总出库重量
        1. 获取总入库重量（通过时间段获取分拣入库的总入库重量）
        2. 获取总出库重量（通过时间段获取包装出库的总出库重量）
        3. 获取库存报损重量（报损重量算于出库重量之中）
        4. 获取库存盘点的重量, 如果总量大于库存总重量则加入到分拣入库的总重量中, 反之加入到包装出库的总重量中
         */
        // 获取入库总重量
        BigDecimal inboundWeight = getInboundWeight(requestDTO, sysId, organizationId);

        // 获取出库总重量
        BigDecimal outboundWeight = getOutboundWeight(requestDTO, sysId, organizationId);

        // 获取库存报损总重量
        BigDecimal stockLossWeight = getStockLossWeight(requestDTO, sysId, organizationId);

        // 将报损重量, 盘点出库重量加入到出库重量中, 扣除退货入库的总重量
        outboundWeight = outboundWeight.add(stockLossWeight);

        NameValueVO<String, BigDecimal> inboundVO = new NameValueVO<>();
        inboundVO.setName("入库");
        inboundVO.setValue(inboundWeight);
        NameValueVO<String, BigDecimal> outboundVO = new NameValueVO<>();
        outboundVO.setName("出库");
        outboundVO.setValue(outboundWeight);

        responseVO.setValues(Stream.of(inboundVO, outboundVO).collect(Collectors.toList()));
        return responseVO;
    }

    /**
     * 获取库存报损总重量
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-22
     * @updateDate 2019-09-22
     * @updatedBy shixiongfei
     */
    private BigDecimal getStockLossWeight(SearchWarehouseDataStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        List<Integer> types = SortingInventoryTypeEnum.getType(requestDTO.getType());
        // 3.1 拼接sql
        QueryWrapper<ProductionManageStockLoss> stockCheckWrapper = new QueryWrapper<>();
        stockCheckWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageStockLoss.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageStockLoss.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageStockLoss.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), ProductionManageStockLoss.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                .eq(StringUtils.isNotBlank(requestDTO.getProductSpecCode()), ProductionManageStockLoss.COL_PRODUCT_SPEC_CODE, requestDTO.getProductSpecCode())
                .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchId()), ProductionManageStockLoss.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId())
                .eq(StringUtils.isNotBlank(requestDTO.getSortingSpecCode()), ProductionManageStockLoss.COL_SORTING_SPEC_CODE, requestDTO.getSortingSpecCode())
                // 报损状态为已确认
                .eq(ProductionManageStockLoss.COL_LOSS_CONFIRMATION_STATUS, 1)
                .in(CollectionUtils.isNotEmpty(types), ProductionManageStockLoss.COL_TYPE, types)
                .ge(ProductionManageStockLoss.COL_DAMAGE_DATE, requestDTO.getStartQueryDate())
                // 将结束的日期进行添加一天的操作
                .le(ProductionManageStockLoss.COL_DAMAGE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1));
        // TODO 这一块后续查看
        return stockLossMapper.getStockLossWeight(stockCheckWrapper);
    }

    /**
     * 获取分拣入库总重量
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     */
    private BigDecimal getInboundWeight(SearchWarehouseDataStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        // 1.1 解析批次类型和分拣类型 获取实际分拣入库类型
        // 1.1.1 获取批次类型
        Byte type = requestDTO.getType();

        // 1.2 拼接sql
        QueryWrapper<ProductionManageSortInstorage> inboundWrapper = new QueryWrapper<>();
        inboundWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageSortInstorage.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageSortInstorage.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), ProductionManageSortInstorage.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                .eq(StringUtils.isNotBlank(requestDTO.getProductSpecCode()), ProductionManageSortInstorage.COL_PRODUCT_SPEC_CODE, requestDTO.getProductSpecCode())
                .eq(StringUtils.isNotBlank(requestDTO.getSortingSpecCode()), ProductionManageSortInstorage.COL_SORTING_SPEC_CODE, requestDTO.getSortingSpecCode())
                .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchId()), ProductionManageSortInstorage.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId())
                .eq(Objects.nonNull(type), ProductionManageSortInstorage.COL_TYPE, type)
                .ge(ProductionManageSortInstorage.COL_CREATE_DATE, requestDTO.getStartQueryDate())
                // 将结束的日期进行添加一天的操作
                .le(ProductionManageSortInstorage.COL_CREATE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1));
        // 1.2 获取分拣入库总重量
        return storageMapper.getInboundWeight(inboundWrapper);
    }

    /**
     * 获取出库总重量
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-09-21
     * @updateDate 2019-09-21
     * @updatedBy shixiongfei
     */
    private BigDecimal getOutboundWeight(SearchWarehouseDataStatisticsRequestDTO requestDTO, String sysId, String organizationId) {
        // 获取分拣入库类型集合
        List<Integer> types = SortingInventoryTypeEnum.getType(requestDTO.getType());
        // 2.1 拼接sql, 这里并未过滤未出库的包装信息
        QueryWrapper<ProductionManageOutboundPackageMessage> outboundWrapper = new QueryWrapper<>();
        outboundWrapper.eq(StringUtils.isNotBlank(sysId), "mo." + ProductionManageOutbound.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), "mo." + ProductionManageOutbound.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(requestDTO.getProductId()), "opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_ID, requestDTO.getProductId())
                .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), "opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                .eq(StringUtils.isNotBlank(requestDTO.getProductSpecCode()), "opm." + ProductionManageOutboundPackageMessage.COL_PRODUCT_SPEC_CODE, requestDTO.getProductSpecCode())
                .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchId()), "opm." + ProductionManageOutboundPackageMessage.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId())
                .in(CollectionUtils.isNotEmpty(types), "opm." + ProductionManageOutboundPackageMessage.COL_SORTING_TYPE, types)
                // 出库次数大于0
                .gt("opm." + ProductionManageOutboundPackageMessage.COL_OUTBOUND_NUM, 0)
                .ge("opm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE, requestDTO.getStartQueryDate())
                // 将结束的日期进行添加一天的操作
                .le("opm." + ProductionManageOutboundPackageMessage.COL_PACKING_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1));

        // 2.2 获取出库总重量
        return messageMapper.getOutboundWeight(outboundWrapper);
    }


    public AbstractPageService.PageResults<List<SearchSortInstorageStatisticsResponseVO>> listSortInstorageStatistics(SearchSortInstorageStatisticsRequestDTO requestDTO) {
        QueryWrapper<SearchSortInstorageStatisticsResponseVO> queryWrapper = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(requestDTO.getIdList())) {
            queryWrapper.in("t. " + ProductionManageSortInstorage.COL_ID, requestDTO.getIdList());
        } else {
            queryWrapper.eq("t." + ProductionManageSortInstorage.COL_SYS_ID, commonUtil.getSysId())
                    .eq("t." + ProductionManageSortInstorage.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductId()), "t." + ProductionManageSortInstorage.COL_PRODUCT_ID, requestDTO.getProductId())
                    .eq(StringUtils.isNotBlank(requestDTO.getGreenhouseId()), "t." + ProductionManageSortInstorage.COL_GREENHOUSE_ID, requestDTO.getGreenhouseId())
                    .eq(StringUtils.isNotBlank(requestDTO.getPlantBatchId()), "t." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID, requestDTO.getPlantBatchId())
                    .eq(Objects.nonNull(requestDTO.getType()), "t." + ProductionManageSortInstorage.COL_TYPE, requestDTO.getType())
                    .eq(StringUtils.isNotBlank(requestDTO.getBaseName()), "t." + ProductionManageSortInstorage.COL_BASE_NAME, requestDTO.getBaseName())
                    .ge(StringUtils.isNotBlank(requestDTO.getStartQueryDate()), "t." + ProductionManageSortInstorage.COL_CREATE_DATE, requestDTO.getStartQueryDate())
                    .le(StringUtils.isNotBlank(requestDTO.getEndQueryDate()), "t." + ProductionManageSortInstorage.COL_CREATE_DATE, LocalDateTimeUtil.localDatePlusDays(requestDTO.getEndQueryDate(), 1))
                    .eq(StringUtils.isNotBlank(requestDTO.getProductLevelCode()), ProductionManageSortInstorage.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                    .orderByDesc("t." + ProductionManageSortInstorage.COL_GREENHOUSE_ID, " t." + ProductionManageSortInstorage.COL_PLANT_BATCH_ID);
        }
        // 这里采用默认值，为了满足前端组件需求
        Page<SearchSortInstorageStatisticsResponseVO> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        IPage<SearchSortInstorageStatisticsResponseVO> iPage = storageMapper.listSortInstorageStatistics(page, queryWrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        AbstractPageService.PageResults<List<SearchSortInstorageStatisticsResponseVO>> pageResults = new AbstractPageService.PageResults<>();
        pageResults.setPagination(pagination);
        List<SearchSortInstorageStatisticsResponseVO> list = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        pageResults.setList(list);

        // 设置初始值
        list.forEach(vo -> {
            vo.setSortingType(vo.getType() + "");
            if (vo.getDamageWeight() != null && vo.getTotalWeight() != null && vo.getTotalWeight().intValue() != 0) {
                BigDecimal divisor = vo.getTotalWeight().add(vo.getDamageWeight());
                BigDecimal damageRatio = vo.getDamageWeight().multiply(new BigDecimal(100)).divide(divisor, 2, RoundingMode.HALF_UP);
                vo.setDamageRatio(String.valueOf(damageRatio));
            }
        });

        return pageResults;
    }

    public void exportSortInstorageStatistics(SearchSortInstorageStatisticsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        ArrayList<String> idList = requestDTO.getIdList();
        List<SearchSortInstorageStatisticsResponseVO> list = null;
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(10 * 10000);

        if (org.springframework.util.CollectionUtils.isEmpty(idList) || true) {
            AbstractPageService.PageResults<List<SearchSortInstorageStatisticsResponseVO>> page = listSortInstorageStatistics(requestDTO);
            list = page.getList();
        } else {
        /*    QueryWrapper<ProductionManageOutOrder> queryWrapper = commonUtil.queryTemplate(ProductionManageOutOrder.class);
            queryWrapper.and(query -> query.in("Id", idList));
            list = dao.selectList(queryWrapper);*/
        }

        List<MergeCell> greenhouseCells = new ArrayList<>();
        List<MergeCell> planBatchCells = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            SearchSortInstorageStatisticsResponseVO statisticsResponseVO = list.get(i);
            if (StringUtils.isNotEmpty(statisticsResponseVO.getDamageRatio())) {
                statisticsResponseVO.setDamageRatio(statisticsResponseVO.getDamageRatio() + "%");
            }
            statisticsResponseVO.setSortingType(SortingInventoryTypeEnum.getValue(statisticsResponseVO.getType()));
            if (StringUtils.isNotEmpty(statisticsResponseVO.getBaseName()) && StringUtils.isEmpty(statisticsResponseVO.getGreenhouseName())) {
                statisticsResponseVO.setGreenhouseId(statisticsResponseVO.getBaseName());
                statisticsResponseVO.setGreenhouseName(statisticsResponseVO.getBaseName());
            }

            String greenhouseId = statisticsResponseVO.getGreenhouseId();
            if (StringUtils.isNotEmpty(greenhouseId)) {
                MergeCell lastCell = CollectionUtils.isEmpty(greenhouseCells) ? null : greenhouseCells.get(greenhouseCells.size() - 1);
                MergeCell greenhouseCell = lastCell != null && lastCell.getKey().equals(greenhouseId) ? lastCell : null;
                if (greenhouseCell == null) {
                    greenhouseCell = new MergeCell(greenhouseId, i, i);
                    greenhouseCells.add(greenhouseCell);
                } else {
                    greenhouseCell.setEnd(i);
                }
            }

            String plantBatchId = statisticsResponseVO.getPlantBatchId();
            if (StringUtils.isNotEmpty(plantBatchId)) {
                MergeCell lastCell = CollectionUtils.isEmpty(planBatchCells) ? null : planBatchCells.get(planBatchCells.size() - 1);
                MergeCell planBatchCell = lastCell != null && lastCell.getKey().equals(plantBatchId) ? lastCell : null;
                if (planBatchCell == null) {
                    planBatchCell = new MergeCell(plantBatchId, i, i);
                    planBatchCells.add(planBatchCell);
                } else {
                    planBatchCell.setEnd(i);
                }
            }
        }

        List<SearchSortInstorageStatisticsResponseVO> list2 = list;
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "分拣入库", response, new ExcelUtils.WriteSheet() {
            @Override
            public void write(WritableSheet sheet) {
                try {
                    for (MergeCell mergeCell : greenhouseCells) {
                        sheet.mergeCells(0, mergeCell.getStart() + 1, 0, mergeCell.getEnd() + 1);
                    }
                    for (MergeCell mergeCell : planBatchCells) {
                        sheet.mergeCells(1, mergeCell.getStart() + 1, 1, mergeCell.getEnd() + 1);
                        sheet.mergeCells(2, mergeCell.getStart() + 1, 2, mergeCell.getEnd() + 1);
                        sheet.mergeCells(3, mergeCell.getStart() + 1, 3, mergeCell.getEnd() + 1);
                        sheet.mergeCells(4, mergeCell.getStart() + 1, 4, mergeCell.getEnd() + 1);
                        sheet.mergeCells(5, mergeCell.getStart() + 1, 5, mergeCell.getEnd() + 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });//导出文件
    }

    @AllArgsConstructor
    @Data
    public class MergeCell {
        private String key;
        private int start;
        private int end;
    }
}