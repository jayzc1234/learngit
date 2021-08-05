package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.exception.SuperCodeExtException;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.model.storagemanage.StockListOther;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.*;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductLevelRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductionManageStockListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.SortingInventoryTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStock;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPLSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchPLSSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchProductionManageStockVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.WarehouseManageConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.PRODUCT_LEVEL_INVENTORY_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryErrorMsgConstants.*;


/**
 * <p>
 * 库存表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@Slf4j
@Service
public class ProductionManageStockService extends ServiceImpl<ProductionManageStockMapper, ProductionManageStock> implements BaseService<SearchProductionManageStockVO> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 添加库存
     *
     * @param
     * @throws SuperCodeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ProductionManageStock entity) {
        CustomAssert.zero2Error(baseMapper.insert(entity), ADD_INVENTORY_MSG_FAILED);
    }

    /**
     * 更新库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, UpdateWrapper<ProductionManageStock> wrapper) throws SuperCodeExtException {
        wrapper.eq("Id", id);
        if (!update(wrapper)) {
            throw new SuperCodeExtException(UPDATE_INVENTORY_MSG_FAILED);
        }
    }

    /**
     * 修改库存
     *
     * @throws SuperCodeException
     */
    public void update(ProductionManageStock stock) throws SuperCodeException {
        baseMapper.updateById(stock);
    }

    /**
     * 获取产品库存列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-06
     * @updateDate 2019-12-06
     * @updatedBy shixiongfei
     */
    @Override
    public PageResults<List<SearchProductionManageStockVO>> list(DaoSearch daosearch) throws SuperCodeException {

        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManageStockListDTO stockListDTO = (ProductionManageStockListDTO) daosearch;

        IPage<ProductionManageStock> iPage = baseMapper.list(stockListDTO, sysId, organizationId);
        StockListOther other = baseMapper.listStatistics(stockListDTO, sysId, organizationId);

        CommonUtil.PMPageResults<SearchProductionManageStockVO> pageResults = new CommonUtil.PMPageResults<>();
        com.jgw.supercodeplatform.common.pojo.common.Page pagination = new com.jgw.supercodeplatform.common.pojo.common.Page(
                (int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());
        pageResults.setPagination(pagination);
        pageResults.setOther(other);

        List<ProductionManageStock> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchProductionManageStockVO> list = records.stream().map(record -> {
            SearchProductionManageStockVO vo = new SearchProductionManageStockVO();
            BeanUtils.copyProperties(record, vo);
            vo.setType(record.getType().toString());
            return vo;
        }).collect(Collectors.toList());

        pageResults.setList(list);
        return pageResults;
    }

    @Override
    public List<SearchProductionManageStockVO> listExcelByIds(List<? extends Serializable> ids) {
        List<ProductionManageStock> list = Optional.ofNullable(baseMapper.selectBatchIds(ids)).orElse(Collections.emptyList());
        return list.stream().map(stock -> {
            SearchProductionManageStockVO vo = new SearchProductionManageStockVO();
            BeanUtils.copyProperties(stock, vo);
            vo.setType(stock.getType().toString());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void dataTransfer(List<SearchProductionManageStockVO> list) {
        list.forEach(stock -> stock.setType(SortingInventoryTypeEnum.getValue(stock.getType())));
    }

    public ProductionManageStock getById(String id) {
        return baseMapper.selectById(id);
    }

    /**
     * 通过批次id和等级code来获取库存信息
     *
     * @param plantBatchId     批次id
     * @param productLevelCode 产品等级code
     * @return
     * @throws SuperCodeException
     */
    public ProductionManageStock getByBatchIdAndLevelCode(String plantBatchId, String productLevelCode) {
        return query().eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .eq(OLD_PLANT_BATCH_ID, plantBatchId)
                .eq(OLD_PRODUCT_LEVEL_CODE, productLevelCode)
                .one();
    }

    public ProductionManageStock sumByProductIdAndLevel(String productId, String productLevelCode) {
        return baseMapper.sumByProductIdAndLevel(productId, productLevelCode, commonUtil.getOrganizationId(), commonUtil.getSysId());
    }

    /**
     * 获取产品等级库存信息列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchPLSResponseVO>> listWithProductLevel(ProductLevelRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        IPage<SearchPLSResponseVO> iPage = baseMapper.listWithProductLevel(requestDTO, sysId, organizationId);

        // 获取产品等级库存的统计数值信息
        SearchPLSSResponseVO statistics = baseMapper.listPLStatistics(requestDTO, sysId, organizationId);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<SearchPLSResponseVO> records = CollectionUtils.elementIsNull(iPage.getRecords());

        records.forEach(record -> record.setInventoryWarning(getWarning(record)));

        CommonUtil.PMPageResults<SearchPLSResponseVO> pageResults = new CommonUtil.PMPageResults<>();
        pageResults.setPagination(pagination);
        pageResults.setList(records);
        pageResults.setOther(statistics);

        return pageResults;
    }

    /**
     * 校验库存数值信息是否满足预警数值，不满足，则预警
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     */
    private String getWarning(SearchPLSResponseVO responseVO) {
        // 获取相关数值，并对null值进行初始化
        Integer warningBoxNum = NumberUtil.initIntegerNum(responseVO.getWarningBoxNum());
        Integer warningQuantity = NumberUtil.initIntegerNum(responseVO.getWarningQuantity());
        BigDecimal warningWeight = NumberUtil.initBigdecimalNum(responseVO.getWarningWeight());
        Integer boxNum = NumberUtil.initIntegerNum(responseVO.getBoxNum());
        Integer quantity = NumberUtil.initIntegerNum(responseVO.getQuantity());
        BigDecimal weight = NumberUtil.initBigdecimalNum(responseVO.getWeight());

        // 存在其中一个数值小于或等于预警数值则触发预警机制
        boolean isTrigger = boxNum > warningBoxNum && quantity > warningQuantity
                && weight.compareTo(warningWeight) == 1;

        return isTrigger ? ADEQUATE_INVENTORY : INVENTORY_SHORTAGE;
    }

    /**
     * 产品等级库存信息导出
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    public void exportWithProductLevel(ProductLevelRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchPLSResponseVO> list = requestDTO.parseStr2List(SearchPLSResponseVO.class);
        if (org.apache.commons.collections4.CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listWithProductLevel(requestDTO).getList();
        }
        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), PRODUCT_LEVEL_INVENTORY_LIST, response);
    }

    /**
     * 扣减库存
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-12-07
     * @updateDate 2019-12-07
     * @updatedBy shixiongfei
     */
    public void deductStock(ProductionManageStock stock, Integer packingBoxNum, Integer packingNum, BigDecimal packingWeight) {
        // 拼接sql
        UpdateWrapper<ProductionManageStock> wrapper = new UpdateWrapper<>();
        wrapper.set(OLD_QUANTITY, Optional.of(stock.getQuantity() - packingNum).filter(num -> num >= 0).orElse(0))
                .set(OLD_BOX_NUM, Optional.of(stock.getBoxNum() - packingBoxNum).filter(num -> num >= 0).orElse(0))
                .set(OLD_WEIGHT, Optional.of(stock.getWeight().subtract(packingWeight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                .set(TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + packingBoxNum)
                .set(TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + packingNum)
                .set(TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(packingWeight));
        update(stock.getId(), wrapper);
    }


    /**
     * 更新库存中的总入库和总出库信息
     *
     * @author shixiongfei
     * @date 2019-12-17
     * @updateDate 2019-12-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void updateInboundAndOutbound(ProductionManageStock stock, Integer boxNum, Integer quantity, BigDecimal weight) {
        stock.initStock();
        // 建议添加版本号，采用乐观锁来处理此类型数据
        boolean isSuccess = update().set(OLD_QUANTITY, Optional.of(stock.getQuantity() - quantity).filter(num -> num >= 0).orElse(0))
                .set(OLD_BOX_NUM, Optional.of(stock.getBoxNum() - boxNum).filter(num -> num >= 0).orElse(0))
                .set(OLD_WEIGHT, Optional.of(stock.getWeight().subtract(weight)).filter(num -> num.signum() != -1).orElse(BigDecimal.ZERO))
                .set(TOTAL_INBOUND_BOX_NUM, stock.getTotalInboundBoxNum() + boxNum)
                .set(TOTAL_INBOUND_QUANTITY, stock.getTotalInboundQuantity() + quantity)
                .set(TOTAL_INBOUND_WEIGHT, stock.getTotalInboundWeight().add(weight))
                .set(TOTAL_OUTBOUND_BOX_NUM, stock.getTotalOutboundBoxNum() + boxNum)
                .set(TOTAL_OUTBOUND_QUANTITY, stock.getTotalOutboundQuantity() + quantity)
                .set(TOTAL_OUTBOUND_WEIGHT, stock.getTotalOutboundWeight().add(weight))
                .eq(OLD_ID, stock.getId())
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .update();
        CustomAssert.false2Error(isSuccess, UPDATE_STOCK_INBOUND_AND_OUTBOUND_MSG_FAILED);
    }
}