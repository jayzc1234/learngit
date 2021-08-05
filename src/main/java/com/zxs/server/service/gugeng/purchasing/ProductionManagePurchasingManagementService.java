package com.zxs.server.service.gugeng.purchasing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.BasePdfUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.PurchasePdfExportUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.pdf.logicdata.PurchasePdfPageEventHelper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.enums.gugeng.RedisKey;
import net.app315.hydra.intelligent.planting.enums.gugeng.procurement.PurchasingStatusEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManageProcurementSupplier;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchasingDetermineProduct;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchasingManagement;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.purchasing.ProductionManagePurchasingManagementMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-11
 */
@Service
public class ProductionManagePurchasingManagementService extends ServiceImpl<ProductionManagePurchasingManagementMapper, ProductionManagePurchasingManagement> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private ProductionManagePurchasingApplicationProductService applicationProductService;

    @Autowired
    private ProductionManagePurchasingDetermineProductService determineProductService;

    @Autowired
    private ProductionManageProcurementSupplierService supplierService;


    /**
     * 添加采购申请
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddPurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        if (StringUtils.isBlank(requestDTO.getPurchaseNumber())) {
            requestDTO.setPurchaseNumber(numberGenerator.getSerialNumber(6, RedisKey.PURCHASING_MANAGEMENT_NO_KEY));
        }
        // 校验计划编号是否重复
        Integer count = query()
                .eq(ProductionManagePurchasingManagement.COL_PURCHASE_NUMBER, requestDTO.getPurchaseNumber())
                .eq(ProductionManagePurchasingManagement.COL_SYS_ID, commonUtil.getSysId())
                .eq(ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, commonUtil.getOrganizationId())
                .count();
        CustomAssert.isNotGreaterThanCustomNumber(count, 0,"采购管理编号已存在，请重新输入");
        // 1. 新增采购管理信息
        ProductionManagePurchasingManagement entity = new ProductionManagePurchasingManagement();
        BeanUtils.copyProperties(requestDTO, entity);
        CustomAssert.isGreaterThan0(baseMapper.insert(entity), "添加采购申请失败");
        // 2. 新增采购申请物品信息
        applicationProductService.addBatchByPurchasingId(requestDTO.getPurchasingApplicationProducts(), entity.getId());
    }

    /**
     * 编辑采购信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdatePurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        ProductionManagePurchasingManagement purchasingManagement = getById(requestDTO.getId());
        CustomAssert.isNull(purchasingManagement, "不存在此采购信息，请检查");

        // 校验当前采购状态是否为待采购或采购中，不是则报错
        boolean isTrue = PurchasingStatusEnum.TO_BE_PURCHASE.getKey() == purchasingManagement.getPurchaseStatus() ||
                PurchasingStatusEnum.IN_PURCHASING.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isSuccess(isTrue, "采购状态非待采购或采购中状态，不可编辑");

        // 1. 更新采购管理信息
        ProductionManagePurchasingManagement entity = new ProductionManagePurchasingManagement();
        BeanUtils.copyProperties(requestDTO, entity);
        CustomAssert.isGreaterThan0(baseMapper.updateById(entity), "编辑采购信息失败");
        // 2. 更新采购申请物品信息
        applicationProductService.updateOrAddBatchByPurchasingId(requestDTO.getPurchasingApplicationProducts(), entity.getId());

    }

    /**
     * 获取采购管理列表信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public PageResults<List<SearchPurchasingManagementResponseVO>> list(SearchPurchasingManagementRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManagePurchasingManagement> iPage = baseMapper.list(requestDTO, sysId, organizationId, commonUtil);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManagePurchasingManagement> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchPurchasingManagementResponseVO> list = records.stream().map(record -> {
            SearchPurchasingManagementResponseVO responseVO = new SearchPurchasingManagementResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            responseVO.setPurchaseStatus(record.getPurchaseStatus().toString());
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 通过id来获取采购信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public SearchPurchasingDetailResponseVO getDetailById(Long id) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        // 1. 获取采购管理详情信息
        ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(id, sysId, organizationId);

        SearchPurchasingDetailResponseVO responseVO = new SearchPurchasingDetailResponseVO();
        BeanUtils.copyProperties(purchasingManagement, responseVO);
        responseVO.setPurchaseStatus(purchasingManagement.getPurchaseStatus().toString());

        // 2. 获取采购申请物品列表
        responseVO.setPurchasingApplicationProducts(applicationProductService.listByPurchasingManageId(id));
        return responseVO;
    }

    /**
     * 移除采购管理信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(id, sysId, organizationId);

        // 校验当前采购管理的状态是否为待采购的状态，不是则报错
        boolean isTrue = PurchasingStatusEnum.TO_BE_PURCHASE.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isSuccess(isTrue, "当前状态非待采购状态，不可删除");

        // 1. 删除采购管理信息
        boolean isSuccess = update().set(ProductionManagePurchasingManagement.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManagePurchasingManagement.COL_ID, id)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingManagement.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, organizationId)
                .update();
        CustomAssert.isSuccess(isSuccess, "删除采购管理信息失败");

        // 2. 删除采购申请物品中关联的信息
        applicationProductService.removeByPurchasingManageId(id);

        // 3. 删除确认供应商及价格关联的信息
        determineProductService.removeByPurchasingManageId(id);
    }

    /**
     * 更新下单日期
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderDate(UpdatePurchasingManagementOrderDateRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

         ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(requestDTO.getId(), sysId, organizationId);
        // 判断当前状态是否为采购完成的状态，是则报错
        boolean isTrue = PurchasingStatusEnum.PROCUREMENT_TO_COMPLETE.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isFalse(isTrue, "当前状态为采购完成状态，不可更新下单日期");

        boolean isSuccess = update().set("order_date", requestDTO.getOrderDate())
                .set(ProductionManagePurchasingManagement.COL_PURCHASE_STAFF_ID, requestDTO.getPurchaseStaffId())
                .set(ProductionManagePurchasingManagement.COL_PURCHASE_STAFF_NAME, requestDTO.getPurchaseStaffName())
                .set(ProductionManagePurchasingManagement.COL_PURCHASE_STATUS, PurchasingStatusEnum.IN_PURCHASING.getKey())
                .eq(ProductionManagePurchasingManagement.COL_ID, requestDTO.getId())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingManagement.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, organizationId)
                .update();

        CustomAssert.isSuccess(isSuccess, "更新下单日期失败");
    }

    /**
     * 通过id获取未删除的采购管理信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private ProductionManagePurchasingManagement getNotDeletedById(Long id, String sysId, String organizationId) throws SuperCodeException {
        ProductionManagePurchasingManagement purchasingManagement =
                query().eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingManagement.COL_SYS_ID, sysId)
                        .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, organizationId)
                        .eq(ProductionManagePurchasingManagement.COL_ID, id)
                        .eq(ProductionManagePurchasingManagement.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                        .one();

        CustomAssert.isNull(purchasingManagement, "不存在此采购管理信息，请检查");

        return purchasingManagement;
    }

    /**
     * 更新采购管理预计到货时间
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateExceptArrivalDate(UpdateExceptArrivalDateRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(requestDTO.getId(), sysId, organizationId);
        // 判断采购状态是否已完成，是则不允许更新
        boolean isTrue = PurchasingStatusEnum.PROCUREMENT_TO_COMPLETE.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isFalse(isTrue, "采购已完成，不可更新预计到货时间");
        boolean isSuccess = update().set(ProductionManagePurchasingManagement.COL_EXPECTED_ARRIVAL_DATE, requestDTO.getExpectedArrivalDate())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingManagement.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingManagement.COL_ID, requestDTO.getId())
                .eq(ProductionManagePurchasingManagement.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .update();
        CustomAssert.isSuccess(isSuccess, "更新预计到货时间失败");
    }


    /**
     * 确认收货
     *
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(UpdateConfirmReceiptRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(requestDTO.getId(), sysId, organizationId);
        // 判断当前采购状态是否为采购中，不是则报错
        boolean isTrue = PurchasingStatusEnum.IN_PURCHASING.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isSuccess(isTrue, "当前状态非采购中的状态，无法确认收货");
        boolean isSuccess = update()
                .set(ProductionManagePurchasingManagement.COL_PURCHASE_STATUS, PurchasingStatusEnum.PROCUREMENT_TO_COMPLETE.getKey())
                .set(ProductionManagePurchasingManagement.COL_ARRIVAL_DATE, requestDTO.getArrivalDate())
                .set(ProductionManagePurchasingManagement.COL_RECEIVING_OFFICER_ID, requestDTO.getReceivingOfficerId())
                .set(ProductionManagePurchasingManagement.COL_RECEIVING_OFFICER_NAME, requestDTO.getReceivingOfficerName())
                .set(ProductionManagePurchasingManagement.COL_REMARK, requestDTO.getRemark())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingManagement.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingManagement.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingManagement.COL_ID, requestDTO.getId())
                .eq(ProductionManagePurchasingManagement.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .update();
        CustomAssert.isSuccess(isSuccess, "确认收货失败");
    }

    /**
     * 添加或者更新采购商品, 这里可能涉及到删除，这里的删除统一采用物理删除
     *
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdatePurchasingProduct(AddPurchasingDetermineProductRequestDTO requestDTO) throws SuperCodeException {
        // 校验当前采购状态是否为已完成，是则报错，不可进行更新或添加采购商品信息
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManagePurchasingManagement purchasingManagement = getNotDeletedById(requestDTO.getPurchasingManagementId(), sysId, organizationId);
        boolean isTrue = PurchasingStatusEnum.PROCUREMENT_TO_COMPLETE.getKey() == purchasingManagement.getPurchaseStatus();
        CustomAssert.isFalse(isTrue, "当前采购状态已完成，不可进行新增或更新确认供应商及价格操作");

        List<Long> supplierIds = requestDTO.getSuppliers().stream().map(AddDetermineSupplierRequestDTO::getSupplierId).collect(Collectors.toList());
        // 校验是否存在失效的供应商,存在则报错
        supplierService.validBatchSupplierIsNotDeleted(supplierIds);
        // 1. 物理删除当前采购管理下的供应商信息
        determineProductService.deleteBySupIdsAndPmtId(requestDTO.getPurchasingManagementId(), supplierIds);
        // 2. 添加当前采购管理下需要的供应商信息
        determineProductService.addBatch(requestDTO);
    }

    /**
     * 采购管理列表导出
     *
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void export(SearchPurchasingManagementRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchPurchasingManagementResponseVO> list = requestDTO.parseStr2List(SearchPurchasingManagementResponseVO.class);

        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
           list = list(requestDTO).getList();
        }

        // 处理特殊需要转换的字段
        list.forEach(responseVO -> responseVO.setPurchaseStatus(PurchasingStatusEnum.getValue(Integer.valueOf(responseVO.getPurchaseStatus()))));

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "采购管理列表", response);
    }

    /**
     * 通过主键id获取确认供应商及价格相关信息
     *
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public SearchDetermineSupAndProductResponseVO getSupplierAndPriceById(Long id) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        List<ProductionManagePurchasingDetermineProduct> determineProducts = determineProductService.listSupplierAndProductByManagementId(id, sysId, organizationId);

        SearchDetermineSupAndProductResponseVO responseVO = new SearchDetermineSupAndProductResponseVO();

        // 过滤获取供应商信息
        List<Long> supplierIds = determineProducts.stream()
                .map(ProductionManagePurchasingDetermineProduct::getSupplierId)
                .distinct().collect(Collectors.toList());

        List<SearchSupAndProductResponseVO> list = supplierIds.stream().map(supplierId -> {
            SearchSupAndProductResponseVO productResponseVO = new SearchSupAndProductResponseVO();
            ProductionManageProcurementSupplier supplier = supplierService.getById(supplierId);
            BeanUtils.copyProperties(supplier, productResponseVO);
            productResponseVO.setSupplierId(supplier.getId());
            productResponseVO.setSupplierTaxType(String.valueOf(supplier.getSupplierTaxType()));
            productResponseVO.setSupplierInvoiceType(String.valueOf(supplier.getSupplierInvoiceType()));
            productResponseVO.setRate(supplier.getRate().toString());

            // 获取指定的供应商采购商品信息集合
            List<ProductionManagePurchasingDetermineProduct> products = determineProducts.stream()
                    .filter(product -> supplierId.equals(product.getSupplierId())).collect(Collectors.toList());
            productResponseVO.setPaymentWay(products.get(0).getPaymentWay());

            List<SearchDetermineProductResponseVO> determineProductResponseVOS = products.stream().map(product -> {
                SearchDetermineProductResponseVO determineProduct = new SearchDetermineProductResponseVO();
                BeanUtils.copyProperties(product, determineProduct);
                return determineProduct;
            }).collect(Collectors.toList());

            productResponseVO.setPurchasingProducts(determineProductResponseVOS);
            return productResponseVO;
        }).collect(Collectors.toList());

        responseVO.setSuppliers(list);
        responseVO.setPurchasingManagementId(id);

        return responseVO;
    }

    /**
     * 采购管理导出pdf
     *
     * @author shixiongfei
     * @date 2019-11-20
     * @updateDate 2019-11-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportPdf(Long id, HttpServletResponse response) throws SuperCodeException {
        SearchPurchasingDetailResponseVO detail = getDetailById(id);
        BasePdfUtils exportUtil = new PurchasePdfExportUtil(detail);
        PdfPageEventHelper eventHelper = new PurchasePdfPageEventHelper();
        exportUtil.exportPdf("采购申请单", eventHelper, response, "采购管理");
    }
}