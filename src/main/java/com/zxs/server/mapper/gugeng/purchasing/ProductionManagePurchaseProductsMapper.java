package com.zxs.server.mapper.gugeng.purchasing;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchProductFromSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchPurchaseProductsRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchaseProducts;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.lang.StringUtils;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PURCHASING_PRODUCT;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.CREATED_BY;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.AUTH_DEPARTMENT_ID;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.PURCHASE_PRODUCT_MAINTAIN_MANAGEMENT;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
public interface ProductionManagePurchaseProductsMapper extends BaseMapper<ProductionManagePurchaseProducts> {


    /**
     * 获取采购商品列表
     *
     * @param requestDTO     请求体对象
     * @param sysId          系统id
     * @param organizationId 组织id
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    default IPage<ProductionManagePurchaseProducts> list(SearchPurchaseProductsRequestDTO requestDTO, String sysId, String organizationId, CommonUtil commonUtil) {

        Page<ProductionManagePurchaseProducts> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManagePurchaseProducts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey());

        String search = requestDTO.getSearch();
        // search为空则进行高级检索，不为空则普通检索
        if (StringUtils.isBlank(search)) {
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getSupplierName()), ProductionManagePurchaseProducts.COL_SUPPLIER_NAME, requestDTO.getSupplierName())
                    .eq(StringUtils.isNotBlank(requestDTO.getProductName()), ProductionManagePurchaseProducts.COL_PRODUCT_NAME, requestDTO.getProductName())
                    .eq(StringUtils.isNotBlank(requestDTO.getBrand()), ProductionManagePurchaseProducts.COL_BRAND, requestDTO.getBrand());
        } else {
            queryWrapper.like(ProductionManagePurchaseProducts.COL_SUPPLIER_NAME, search);
        }

        queryWrapper.orderByDesc(ProductionManagePurchaseProducts.COL_UPDATE_DATE);

        // 添加数据权限
        commonUtil.roleDataAuthFilter(PURCHASING_PRODUCT, queryWrapper, CREATED_BY, StringUtils.EMPTY);
        // v1.9 新加数据权限
        InterceptorUserRoleDataAuth roleDataAuth = commonUtil.getRoleFunAuthWithAuthCode(PURCHASE_PRODUCT_MAINTAIN_MANAGEMENT);
        commonUtil.setAuthFilter(queryWrapper, roleDataAuth, CREATED_BY, AUTH_DEPARTMENT_ID, ProductionManagePurchaseProducts.class);

        return selectPage(page, queryWrapper);
    }

    /**
     * 获取指定供应商下的采购产品列表信息
     *
     * @param requestDTO     请求体对象
     * @param sysId          系统id
     * @param organizationId 组织id
     * @return
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     */
    default IPage<ProductionManagePurchaseProducts> listFromSupplier(SearchProductFromSupplierRequestDTO requestDTO, String sysId, String organizationId) {
        Page<ProductionManagePurchaseProducts> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManagePurchaseProducts> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchaseProducts.COL_ORGANIZATION_ID, organizationId)
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchaseProducts.COL_SYS_ID, sysId)
                .eq(ProductionManagePurchaseProducts.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .eq(ProductionManagePurchaseProducts.COL_SUPPLIER_ID, requestDTO.getSupplierId());
        String search = requestDTO.getSearch();
        if (StringUtils.isNotBlank(search)) {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(StringUtils.isNotBlank(search), ProductionManagePurchaseProducts.COL_PRODUCT_NAME, search)
                    .or().like(StringUtils.isNotBlank(search), ProductionManagePurchaseProducts.COL_BRAND, search));
        }
        queryWrapper.orderByDesc(ProductionManagePurchaseProducts.COL_CREATE_DATE);

        return selectPage(page, queryWrapper);
    }
}
