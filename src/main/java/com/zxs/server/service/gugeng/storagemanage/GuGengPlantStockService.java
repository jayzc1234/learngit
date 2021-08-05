package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CollectionUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchPlantStockRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.GuGengPlantStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.GuGengPlantStockMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchBatchPSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductPSResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.PLANT_STOCK_PLANT_BATCH_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.PLANT_STOCK_PRODUCT_LIST;

/**
 * <p>
 * 种植存量表 服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-12-09
 */
@Service
public class GuGengPlantStockService extends ServiceImpl<GuGengPlantStockMapper, GuGengPlantStock> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 获取批次种植存量列表
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<SearchBatchPSResponseVO>> listBatch(SearchPlantStockRequestDTO requestDTO) {
        IPage<GuGengPlantStock> iPage = baseMapper.listBatch(requestDTO, commonUtil);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<GuGengPlantStock> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchBatchPSResponseVO> list = records.stream().map(record -> {
            SearchBatchPSResponseVO responseVO = new SearchBatchPSResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            return responseVO;
        }).collect(Collectors.toList());

        // 获取统计数据
        BigDecimal totalWeight = baseMapper.getBatchTotalWeight(requestDTO, commonUtil);

        CommonUtil.PMPageResults<SearchBatchPSResponseVO> pageResults = new CommonUtil.PMPageResults<>();
        pageResults.setPagination(pagination);
        pageResults.setList(list);
        pageResults.setOther(totalWeight);
        return pageResults;
    }

    /**
     * 获取产品种植存量列表
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<SearchProductPSResponseVO>> listProduct(SearchPlantStockRequestDTO requestDTO) {
        IPage<GuGengPlantStock> iPage = baseMapper.listProduct(requestDTO, commonUtil);

        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        List<GuGengPlantStock> records = CollectionUtils.elementIsNull(iPage.getRecords());
        List<SearchProductPSResponseVO> list = records.stream().map(record -> {
            SearchProductPSResponseVO responseVO = new SearchProductPSResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            return responseVO;
        }).collect(Collectors.toList());

        // 获取统计数据
        BigDecimal totalWeight = baseMapper.getProductTotalWeight(requestDTO, commonUtil);

        CommonUtil.PMPageResults<SearchProductPSResponseVO> pageResults = new CommonUtil.PMPageResults<>();
        pageResults.setPagination(pagination);
        pageResults.setList(list);
        pageResults.setOther(totalWeight);
        return pageResults;
    }

    /**
     * 种植存量-批次列表导出
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportBatch(SearchPlantStockRequestDTO requestDTO, HttpServletResponse response) {
        List<SearchBatchPSResponseVO> list = requestDTO.parseStr2List(SearchBatchPSResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listBatch(requestDTO).getList();
        }

        try {
            ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), PLANT_STOCK_PLANT_BATCH_LIST, response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage(), e);
        }
    }

    /**
     * 种植存量-产品列表导出
     *
     * @author shixiongfei
     * @date 2019-12-09
     * @updateDate 2019-12-09
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportProduct(SearchPlantStockRequestDTO requestDTO, HttpServletResponse response) {
        List<SearchProductPSResponseVO> list = requestDTO.parseStr2List(SearchProductPSResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listProduct(requestDTO).getList();
        }

        try {
            ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), PLANT_STOCK_PRODUCT_LIST, response);
        } catch (Exception e) {
            throw new SuperCodeExtException(e.getMessage(), e);
        }
    }
}
