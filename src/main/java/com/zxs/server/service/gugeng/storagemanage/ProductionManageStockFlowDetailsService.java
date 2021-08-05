package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.bo.gugeng.FlowDetailBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddCodeLessPackageMessageRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductionManageStockFlowDetailsListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.SearchStockFlowRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.StockFlowDetailTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageStockFlowDetails;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageStockFlowDetailsMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchSFSResponseVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.SearchStockFlowResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.InventoryFlowConstants.*;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.export.ExportSheetNameConstants.INVENTORY_FLOW_LIST;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryFlowErrorMsgConstants.ADD_INVENTORY_FLOW_FAILED;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.message.error.InventoryFlowErrorMsgConstants.BATCH_AND_LEVEL_CAN_NOT_BE_NULL;

/**
 * <p>
 * 库存流水详情表 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-07-18
 */
@Service
public class ProductionManageStockFlowDetailsService extends ServiceImpl<ProductionManageStockFlowDetailsMapper, ProductionManageStockFlowDetails> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 添加流水数据
     *
     * @param stockFlow
     * @throws SuperCodeException
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(ProductionManageStockFlowDetails stockFlow) {
        Employee employee = commonUtil.getEmployee();
        stockFlow.setCreateUserId(employee.getEmployeeId());
        stockFlow.setCreateUserName(employee.getName());
        stockFlow.setOutInDate(new Date());
        CustomAssert.zero2Error(baseMapper.insert(stockFlow), ADD_INVENTORY_FLOW_FAILED);
    }

    /**
     * 更新流水详情表中的重量值
     *
     * @param flowDetails
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateWeight(ProductionManageStockFlowDetails flowDetails) {
        UpdateWrapper<ProductionManageStockFlowDetails> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set(Objects.nonNull(flowDetails.getOutInWeight()), OUT_IN_WEIGHT, flowDetails.getOutInWeight())
                .set(Objects.nonNull(flowDetails.getOutInBoxNum()), OUT_IN_BOX_NUM, flowDetails.getOutInBoxNum())
                .set(Objects.nonNull(flowDetails.getOutInNum()), OUT_IN_NUM, flowDetails.getOutInNum())
                .eq(BUSINESS_ID, flowDetails.getBusinessId())
                .eq(OUT_IN_TYPE, StockFlowDetailTypeEnum.SORTING_IN.getKey())
                .eq(OLD_SYS_ID, commonUtil.getSysId())
                .eq(OLD_ORGANIZATION_ID, commonUtil.getOrganizationId());
        baseMapper.update(null, updateWrapper);
    }

    /**
     * 通过批次 + 产品等级code获取库存流水详情
     *
     * @author shixiongfei
     * @date 2019-11-22
     * @updateDate 2019-11-22
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<ProductionManageStockFlowDetails>> list(ProductionManageStockFlowDetailsListDTO stockFlowDetailsListDTO) throws SuperCodeException {
        Page<ProductionManageStockFlowDetails> page = new Page<>(stockFlowDetailsListDTO.getDefaultCurrent(), stockFlowDetailsListDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStockFlowDetails> queryWrapper = commonUtil.queryTemplate(ProductionManageStockFlowDetails.class);

        CustomAssert.true2Error(StringUtils.isBlank(stockFlowDetailsListDTO.getPlantBatchId()), BATCH_AND_LEVEL_CAN_NOT_BE_NULL);

        queryWrapper.eq(OLD_PRODUCT_LEVEL_CODE, Optional.ofNullable(stockFlowDetailsListDTO.getProductLevelCode()).orElse(StringUtils.EMPTY));
        queryWrapper.eq(StringUtils.isNotBlank(stockFlowDetailsListDTO.getPlantBatchId()), OLD_PLANT_BATCH_ID, stockFlowDetailsListDTO.getPlantBatchId());
        IPage<ProductionManageStockFlowDetails> ipage = baseMapper.selectPage(page, queryWrapper);

        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) ipage.getSize(), (int) ipage.getCurrent(), (int) ipage.getTotal());

        return new PageResults<>(ipage.getRecords(), pagination);
    }

    /**
     * 获取全部库存流水列表信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchStockFlowResponseVO>> listAll(SearchStockFlowRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 获取库存流水列表数据

        Page<ProductionManageStockFlowDetails> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());
        QueryWrapper<ProductionManageStockFlowDetails> wrapper = baseMapper.getWrapper(requestDTO, sysId, organizationId);

        wrapper.orderByDesc("d." + ProductionManageStockFlowDetails.COL_OUT_IN_DATE);

        IPage<FlowDetailBO> iPage = baseMapper.listAll(page, wrapper);
        com.jgw.supercodeplatform.common.pojo.common.Page pagination =
                new com.jgw.supercodeplatform.common.pojo.common.Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<FlowDetailBO> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchStockFlowResponseVO> list = parseBO2VO(records);

        // 获取库存流水总计数据
        List<ProductionManageStockFlowDetails> stockFlows = baseMapper.list(requestDTO, sysId, organizationId);
        SearchSFSResponseVO sfsResponseVO = statistics(stockFlows);
        CommonUtil.PMPageResults<SearchStockFlowResponseVO> pageResults = new CommonUtil.PMPageResults<>();

        pageResults.setList(list);
        pageResults.setPagination(pagination);
        pageResults.setOther(sfsResponseVO);

        return pageResults;
    }

    /**
     * 统计库存流水总数据
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    private SearchSFSResponseVO statistics(List<ProductionManageStockFlowDetails> stockFlows) {
        SearchSFSResponseVO responseVO = new SearchSFSResponseVO();
        // 入库类型 1 =>分拣入库
        List<ProductionManageStockFlowDetails> inbounds = stockFlows.stream()
                .filter(t -> t.getOutInType() == StockFlowDetailTypeEnum.SORTING_IN.getKey()
                ).collect(Collectors.toList());
        AtomicInteger tiBoxNum = new AtomicInteger(inbounds.stream().mapToInt(ProductionManageStockFlowDetails::getOutInBoxNum).sum());
        int tiQuantity = inbounds.stream().mapToInt(ProductionManageStockFlowDetails::getOutInNum).sum();
        BigDecimal tiWeight = inbounds.stream().map(ProductionManageStockFlowDetails::getOutInWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 出库类型 3 => 包装出库 5 => 报损出库 10 => 全部库存报损
        List<ProductionManageStockFlowDetails> outbounds = stockFlows.stream()
                .filter(t -> t.getOutInType() == StockFlowDetailTypeEnum.PACKAGING_OUT.getKey()
                        || t.getOutInType() == StockFlowDetailTypeEnum.DAMAGE_OUT.getKey()
                        || t.getOutInType() == StockFlowDetailTypeEnum.TOTAL_INVENTORY_LOSS.getKey()
                ).collect(Collectors.toList());
        AtomicInteger toBoxNum = new AtomicInteger(outbounds.stream().mapToInt(ProductionManageStockFlowDetails::getOutInBoxNum).sum());
        int toQuantity = outbounds.stream().mapToInt(ProductionManageStockFlowDetails::getOutInNum).sum();
        BigDecimal toWeight = outbounds.stream().map(ProductionManageStockFlowDetails::getOutInWeight).reduce(BigDecimal.ZERO, BigDecimal::add);

        responseVO.setTiBoxNum(tiBoxNum.get());
        responseVO.setTiQuantity(tiQuantity);
        responseVO.setTiWeight(tiWeight);
        responseVO.setToBoxNum(toBoxNum.get());
        responseVO.setToQuantity(toQuantity);
        responseVO.setToWeight(toWeight);

        return responseVO;
    }

    /**
     * 通过流水类型将具体数值赋值给入库或出库数值
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     */
    private List<SearchStockFlowResponseVO> parseBO2VO(List<FlowDetailBO> records) {

        // 入库类型 1 =>分拣入库
        List<FlowDetailBO> inbounds = records.stream()
                .filter(t -> t.getOutInType() == StockFlowDetailTypeEnum.SORTING_IN.getKey()
                ).collect(Collectors.toList());
        // 处理入库结果集
        List<SearchStockFlowResponseVO> inboundList = inbounds.stream()
                .map(inbound -> {
                    SearchStockFlowResponseVO responseVO = new SearchStockFlowResponseVO();
                    BeanUtils.copyProperties(inbound, responseVO);
                    responseVO.setSortingType(inbound.getSortingType().toString());
                    responseVO.setOutInType(inbound.getOutInType().toString());
                    responseVO.setInboundBoxNum(inbound.getOutInBoxNum());
                    responseVO.setInboundQuantity(inbound.getOutInNum());
                    responseVO.setInboundWeight(inbound.getOutInWeight());
                    return responseVO;
                }).collect(Collectors.toList());
        // 出库类型 3 => 包装出库 5 => 报损出库 10 => 全部库存报损
        List<FlowDetailBO> outbounds = records.stream()
                .filter(t -> t.getOutInType() == StockFlowDetailTypeEnum.PACKAGING_OUT.getKey()
                        || t.getOutInType() == StockFlowDetailTypeEnum.DAMAGE_OUT.getKey()
                        || t.getOutInType() == StockFlowDetailTypeEnum.TOTAL_INVENTORY_LOSS.getKey()
                ).collect(Collectors.toList());
        // 处理出库结果集
        List<SearchStockFlowResponseVO> outboundList = outbounds.stream()
                .map(outbound -> {
                    SearchStockFlowResponseVO responseVO = new SearchStockFlowResponseVO();
                    BeanUtils.copyProperties(outbound, responseVO);
                    responseVO.setSortingType(outbound.getSortingType().toString());
                    responseVO.setOutInType(outbound.getOutInType().toString());
                    responseVO.setOutboundBoxNum(outbound.getOutInBoxNum());
                    responseVO.setOutboundQuantity(outbound.getOutInNum());
                    responseVO.setOutboundWeight(outbound.getOutInWeight());
                    return responseVO;
                }).collect(Collectors.toList());


        // 对最终的结果集进行排序，按出入库时间倒序排列
        return Stream.of(inboundList, outboundList)
                .flatMap(Collection::stream).sorted(Comparator.comparing(SearchStockFlowResponseVO::getOutInDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 库存流水列表数据导出
     *
     * @author shixiongfei
     * @date 2019-11-13
     * @updateDate 2019-11-13
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(SearchStockFlowRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchStockFlowResponseVO> list = requestDTO.parseStr2List(SearchStockFlowResponseVO.class);
        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listAll(requestDTO).getList();
        }
        list.forEach(vo -> vo.setOutInType(StockFlowDetailTypeEnum.getValue(Integer.valueOf(vo.getOutInType()))));

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), INVENTORY_FLOW_LIST, response);
    }

   /**
    * 添加包装出库流水数据
    *
    * @author shixiongfei
    * @date 2019-12-07
    * @updateDate 2019-12-07
    * @updatedBy shixiongfei
    * @param
    * @return
    */
    public void addOutbound(Long outboundId, AddCodeLessPackageMessageRequestDTO packageMessage) {
        ProductionManageStockFlowDetails flowDetails = new ProductionManageStockFlowDetails();
        flowDetails.setBusinessId(outboundId);
        flowDetails.setProductLevelCode(packageMessage.getProductLevelCode());
        flowDetails.setPlantBatchId(packageMessage.getPlantBatchId());
        flowDetails.setOutInType(StockFlowDetailTypeEnum.PACKAGING_OUT.getKey());
        flowDetails.setOutInBoxNum(packageMessage.getPackingBoxNum());
        flowDetails.setOutInNum(packageMessage.getPackingNum());
        flowDetails.setOutInWeight(packageMessage.getPackingWeight());
        Employee employee = commonUtil.getEmployee();
        flowDetails.setCreateUserId(employee.getEmployeeId());
        flowDetails.setCreateUserName(employee.getName());
        flowDetails.setOutInDate(new Date());
        CustomAssert.zero2Error(baseMapper.insert(flowDetails), ADD_INVENTORY_FLOW_FAILED);
    }
}