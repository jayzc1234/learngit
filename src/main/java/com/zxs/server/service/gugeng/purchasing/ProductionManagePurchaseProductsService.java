package com.zxs.server.service.gugeng.purchasing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.ExcelUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.*;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchaseProducts;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.purchasing.ProductionManagePurchaseProductsMapper;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.PurchaseConstants.VALID_SUPPLIER_PRODUCT_UNIQUE;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
@Service
public class ProductionManagePurchaseProductsService extends ServiceImpl<ProductionManagePurchaseProductsMapper, ProductionManagePurchaseProducts> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageProcurementSupplierService supplierService;

    /**
     * 批量添加采购商品
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(AddPurchaseProductListRequestDTO requestDTO) throws SuperCodeException {
        List<AddPurchaseProductRequestDTO> purchaseProducts = requestDTO.getPurchaseProducts();

        // 自身校验商品 + 品牌 + 规格型号校验唯一性
        purchaseProducts.stream()
                .collect(Collectors.groupingBy(t -> t.getProductName() + t.getBrand() + t.getSpecModel(),
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            int size = list.size();
                            if (size > 1) {
                                AddPurchaseProductRequestDTO product = list.get(0);
                                CustomAssert.throwExtException(String.format(VALID_SUPPLIER_PRODUCT_UNIQUE,
                                        product.getProductName(), product.getBrand(), product.getSpecModel()));
                            }
                            return list;
                        })));

        // 采购商 + 商品 + 品牌 + 规格型号校验唯一性，通过采购供应商获取采购商品信息列表
        List<ProductionManagePurchaseProducts> products = listBySupplierId(requestDTO.getSupplierId());
        if (CollectionUtils.isNotEmpty(products)) {
            validUnique(requestDTO.getSupplierId(), requestDTO.getPurchaseProducts());
        }

        List<ProductionManagePurchaseProducts> list = purchaseProducts.stream().map(product -> {
            ProductionManagePurchaseProducts entity = new ProductionManagePurchaseProducts();
            BeanUtils.copyProperties(product, entity);
            entity.setSupplierId(requestDTO.getSupplierId());
            entity.setSupplierName(requestDTO.getSupplierName());
            return entity;
        }).collect(Collectors.toList());

        CustomAssert.isSuccess(saveBatch(list), "批量添加采购商品失败");
    }

    /**
     * 编辑采购商品
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdatePurchaseProductRequestDTO requestDTO) throws SuperCodeException {
        ProductionManagePurchaseProducts product = getNotDeletedById(requestDTO.getId());
        List<ProductionManagePurchaseProducts> products = listBySupplierId(requestDTO.getSupplierId());
        // 校验采购商 + 商品 + 品牌 + 规格型号校验唯一性，这里需要过滤掉自己本身的数据
        long count = products.stream()
                .filter(t -> !t.getId().equals(requestDTO.getId())
                        && t.getProductName().equals(requestDTO.getProductName())
                        && t.getBrand().equals(requestDTO.getBrand())
                        && t.getSpecModel().equals(requestDTO.getSpecModel())).count();
        if (count > 0) {
            CustomAssert.throwExtException(String.format(VALID_SUPPLIER_PRODUCT_UNIQUE,
                    product.getProductName(), product.getBrand(), product.getSpecModel()));
        }
        BeanUtils.copyProperties(requestDTO, product);
        CustomAssert.isGreaterThan0(baseMapper.updateById(product), "编辑采购商品失败");
    }

    /**
     * 校验采购商 + 商品 + 品牌 + 规格型号校验唯一性
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-21
     * @updateDate 2019-11-21
     * @updatedBy shixiongfei
     */
    private void validUnique(Long supplierId, List<AddPurchaseProductRequestDTO> purchaseProducts) {
        List<ProductionManagePurchaseProducts> products = listBySupplierId(supplierId);
        purchaseProducts.forEach(product -> {
            long count = products.stream().filter(t -> t.getProductName().equals(product.getProductName())
                    && t.getBrand().equals(product.getBrand())
                    && t.getSpecModel().equals(product.getSpecModel())).count();
            if (count > 0) {
                CustomAssert.throwExtException(String.format(VALID_SUPPLIER_PRODUCT_UNIQUE,
                        product.getProductName(), product.getBrand(), product.getSpecModel()));
            }
        });
    }

    /**
     * 获取采购商品列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchPurchaseProductsResponseVO>> list(SearchPurchaseProductsRequestDTO requestDTO) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManagePurchaseProducts> iPage = baseMapper.list(requestDTO, sysId, organizationId, commonUtil);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        List<ProductionManagePurchaseProducts> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());

        List<SearchPurchaseProductsResponseVO> list = records.stream().map(record -> {
            SearchPurchaseProductsResponseVO responseVO = new SearchPurchaseProductsResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 通过id获取未删除的采购商品
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    private ProductionManagePurchaseProducts getNotDeletedById(Long id) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        ProductionManagePurchaseProducts product = query().eq(ProductionManagePurchaseProducts.COL_ID, id)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .one();
        // 这里以后尽量采用运行时异常，检查异常实在是痛苦，可获取捕获处理成运行时异常
        CustomAssert.isNull(product, "不存在此采购商品信息，请检查");
        return product;
    }

    /**
     * 更新采购商品中的供应商名称，为了数据达到一一致性
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    protected void updateSupplierName(Long id, String supplierName) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        boolean isSuccess = update().set(ProductionManagePurchaseProducts.COL_SUPPLIER_NAME, supplierName)
                .eq(ProductionManagePurchaseProducts.COL_SUPPLIER_ID, id)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .update();
        CustomAssert.isSuccess(isSuccess, "同步更新采购商品中的供应商名称失败");
    }

    /**
     * 通过id移除指定的采购商品信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public void remove(Long id) throws SuperCodeException {
        getNotDeletedById(id);

        boolean isSuccess = update().set(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManagePurchaseProducts.COL_ID, id)
                .update();

        CustomAssert.isSuccess(isSuccess, "移除采购商品失败");
    }

    /**
     * 导出采购商品列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public void export(SearchPurchaseProductsRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchPurchaseProductsResponseVO> list = requestDTO.parseStr2List(SearchPurchaseProductsResponseVO.class);

        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = list(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "采购商品列表", response);
    }

    /**
     * 通过id获取采购商品详情信息以及关联的供应商信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public SearchProductAndSupplierDetailResponseVO getDetailById(Long id) throws SuperCodeException {
        // 获取采购商品详情信息
        ProductionManagePurchaseProducts purchaseProduct = getNotDeletedById(id);
        SearchPurchaseProductsResponseVO product = new SearchPurchaseProductsResponseVO();
        BeanUtils.copyProperties(purchaseProduct, product);

        // 获取供应商详情信息
        SearchSupplierResponseVO supplier = supplierService.getDetailById(product.getSupplierId());

        SearchProductAndSupplierDetailResponseVO responseVO = new SearchProductAndSupplierDetailResponseVO();
        responseVO.setPurchaseProduct(Stream.of(product).collect(Collectors.toList()));
        responseVO.setSupplier(supplier);

        return responseVO;
    }

    /**
     * 移除指定供应商旗下的所有商品信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public void removeBySupplierId(Long supplierId) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        boolean isSuccess = update().set(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(ProductionManagePurchaseProducts.COL_SUPPLIER_ID, supplierId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .update();
        CustomAssert.isSuccess(isSuccess, "移除该供应商关联的商品信息失败");
    }

    /**
     * 通过指定的供应商来获取采购产品列表信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    public PageResults<List<SearchProductFromSupplierResponseVO>> listFromSupplier(SearchProductFromSupplierRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        IPage<ProductionManagePurchaseProducts> iPage = baseMapper.listFromSupplier(requestDTO, sysId, organizationId);
        Page pagination = new Page((int) iPage.getSize(), (int) iPage.getCurrent(), (int) iPage.getTotal());

        AtomicInteger count = new AtomicInteger((requestDTO.getDefaultCurrent() - 1) * requestDTO.getDefaultPageSize() + 1);

        List<ProductionManagePurchaseProducts> records = Optional.ofNullable(iPage.getRecords()).orElse(Collections.emptyList());
        List<SearchProductFromSupplierResponseVO> list = records.stream().map(record -> {
            SearchProductFromSupplierResponseVO responseVO = new SearchProductFromSupplierResponseVO();
            BeanUtils.copyProperties(record, responseVO);
            // 先返回，后加一,可理解为 count++;
            responseVO.setSerialNumber(count.getAndIncrement());
            return responseVO;
        }).collect(Collectors.toList());

        return new PageResults<>(list, pagination);
    }

    /**
     * 通过供应商id和商品名称获取采购商品列表信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     */
    public List<SearchDetermineProductResponseVO> listBySupplierIdAndProductName(Long supplierId, String productName) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 获取采购商品列表信息
        List<ProductionManagePurchaseProducts> list = query().eq(ProductionManagePurchaseProducts.COL_PRODUCT_NAME, productName)
                .eq(ProductionManagePurchaseProducts.COL_SUPPLIER_ID, supplierId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();

        list = Optional.ofNullable(list).orElse(Collections.emptyList());
        return list.stream().map(product -> {
            SearchDetermineProductResponseVO responseVO = new SearchDetermineProductResponseVO();
            BeanUtils.copyProperties(product, responseVO);
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 通过采购商id获取采购商品列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-21
     * @updateDate 2019-11-21
     * @updatedBy shixiongfei
     */
    private List<ProductionManagePurchaseProducts> listBySupplierId(Long supplierId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return query().eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .eq(ProductionManagePurchaseProducts.COL_SUPPLIER_ID, supplierId)
                .list();
    }

    /**
     * 通过采购供应商来导出采购商品信息列表
     *
     * @author shixiongfei
     * @date 2019-11-22
     * @updateDate 2019-11-22
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void exportFromSupplier(SearchProductFromSupplierRequestDTO requestDTO, HttpServletResponse response) throws SuperCodeException {
        List<SearchProductFromSupplierResponseVO> list = requestDTO.parseStr2List(SearchProductFromSupplierResponseVO.class);

        if (CollectionUtils.isEmpty(list)) {
            requestDTO.setCurrent(1);
            requestDTO.setPageSize(commonUtil.getExportNumber());
            list = listFromSupplier(requestDTO).getList();
        }

        ExcelUtils.listToExcel(list, requestDTO.exportMetadataToMap(), "采购商品列表", response);
    }
}