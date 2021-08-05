package com.zxs.server.service.gugeng.purchasing;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddDetermineSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddPurchasingDetermineProductRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchasingDetermineProduct;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.purchasing.ProductionManagePurchasingDetermineProductMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-11
 */
@Service
public class ProductionManagePurchasingDetermineProductService extends ServiceImpl<ProductionManagePurchasingDetermineProductMapper, ProductionManagePurchasingDetermineProduct> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 通过供应商id集合和采购管理id删除供应商商品信息
     *
     * @param pmtId       采购管理id
     * @param supplierIds 供应商id集合
     */
    public void deleteBySupIdsAndPmtId(Long pmtId, List<Long> supplierIds) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<ProductionManagePurchasingDetermineProduct> wrapper = new QueryWrapper<>();
        wrapper.eq(ProductionManagePurchasingDetermineProduct.COL_PURCHASING_MANAGEMENT_ID, pmtId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingDetermineProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingDetermineProduct.COL_ORGANIZATION_ID, organizationId)
                .in(ProductionManagePurchasingDetermineProduct.COL_SUPPLIER_ID, supplierIds);
        // 暂不考虑是否删除失败
        remove(wrapper);
    }

    /**
     * 批量新增确认供应商及产品价格
     *
     * @param requestDTO
     */
    public void addBatch(AddPurchasingDetermineProductRequestDTO requestDTO) {
        List<AddDetermineSupplierRequestDTO> suppliers = requestDTO.getSuppliers();
        List<ProductionManagePurchasingDetermineProduct> products = suppliers.stream()
                .map(supplier -> supplier.getPurchasingProducts().stream().map(product -> {
                    ProductionManagePurchasingDetermineProduct determineProduct = new ProductionManagePurchasingDetermineProduct();
                    BeanUtils.copyProperties(product, determineProduct);
                    determineProduct.setSupplierId(supplier.getSupplierId());
                    determineProduct.setPaymentWay(supplier.getPaymentWay());
                    determineProduct.setPurchasingManagementId(requestDTO.getPurchasingManagementId());
                    return determineProduct;
                }).collect(Collectors.toList())).collect(Collectors.toList()).stream().flatMap(Collection::stream).collect(Collectors.toList());

        // 批量新增
        saveBatch(products);
    }

    /**
     * 通过采购管理主键id删除供应商关联信息，这里采用的是物理删除
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     */
    public void removeByPurchasingManageId(Long purchasingManagementId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        QueryWrapper<ProductionManagePurchasingDetermineProduct> wrapper = new QueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingDetermineProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingDetermineProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingDetermineProduct.COL_PURCHASING_MANAGEMENT_ID, purchasingManagementId);
        remove(wrapper);
    }

    /**
     * 通过采购管理id来获取确认供应商及价格等信息
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-10-12
     * @updateDate 2019-10-12
     * @updatedBy shixiongfei
     */
    public List<ProductionManagePurchasingDetermineProduct> listSupplierAndProductByManagementId(Long id, String sysId, String organizationId) {
        List<ProductionManagePurchasingDetermineProduct> list = query().eq(ProductionManagePurchasingDetermineProduct.COL_PURCHASING_MANAGEMENT_ID, id)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingDetermineProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingDetermineProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingDetermineProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();

        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    /**
     * 通过供应商及价格id集合来进行软删除
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-20
     * @updateDate 2019-11-20
     * @updatedBy shixiongfei
     */
    public void batchRemoveByIds(List<Long> ids) throws SuperCodeException {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();

        boolean isSuccess = update().set(ProductionManagePurchasingDetermineProduct.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingDetermineProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingDetermineProduct.COL_ORGANIZATION_ID, organizationId)
                .in(ProductionManagePurchasingDetermineProduct.COL_ID, ids)
                .eq(ProductionManagePurchasingDetermineProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .update();

        CustomAssert.isSuccess(isSuccess, "删除供应商及价格失败");

    }

    /**
     * 通过采购管理id获取供应商及价格信息列表
     *
     * @param
     * @return
     * @author shixiongfei
     * @date 2019-11-20
     * @updateDate 2019-11-20
     * @updatedBy shixiongfei
     */
    private List<ProductionManagePurchasingDetermineProduct> listByPurManageId(Long purchasingManageId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return query().eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingDetermineProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingDetermineProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingDetermineProduct.COL_PURCHASING_MANAGEMENT_ID, purchasingManageId)
                .eq(ProductionManagePurchasingDetermineProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();
    }
}
