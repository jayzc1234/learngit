package com.zxs.server.mapper.gugeng.purchasing;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.SearchSupplierRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManageProcurementSupplier;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.authcode.AuthCodeConstants.PURCHASING_SUPPLIER;
import static net.app315.hydra.intelligent.planting.common.gugeng.constants.CommonConstants.CREATED_BY;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author shixiongfei
 * @since 2019-10-08
 */
public interface ProductionManageProcurementSupplierMapper extends BaseMapper<ProductionManageProcurementSupplier> {



    /**
     * 获取采购供应商列表信息
     *
     * @author shixiongfei
     * @date 2019-10-09
     * @updateDate 2019-10-09
     * @updatedBy shixiongfei
     * @param requestDTO 请求体对象
     * @param sysId 系统id
     * @param organizationId 组织id
     * @param needAuth
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.jgw.supercodeplatform.productmanagement.pojo.purchasing.ProductionManageProcurementSupplier>
     */
    default IPage<ProductionManageProcurementSupplier> list(SearchSupplierRequestDTO requestDTO, String sysId, String organizationId, CommonUtil commonUtil, boolean needAuth) {

        Page<ProductionManageProcurementSupplier> page = new Page<>(requestDTO.getDefaultCurrent(), requestDTO.getDefaultPageSize());

        QueryWrapper<ProductionManageProcurementSupplier> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(StringUtils.isNotBlank(sysId), ProductionManageProcurementSupplier.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManageProcurementSupplier.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManageProcurementSupplier.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey());

        String search = requestDTO.getSearch();
        // search为空则进行高级检索，否则普通检索
        if (StringUtils.isBlank(search)) {
            queryWrapper.eq(StringUtils.isNotBlank(requestDTO.getSupplierNumber()), ProductionManageProcurementSupplier.COL_SUPPLIER_NUMBER, requestDTO.getSupplierNumber())
                    .eq(StringUtils.isNotBlank(requestDTO.getSupplierName()), ProductionManageProcurementSupplier.COL_SUPPLIER_NAME, requestDTO.getSupplierName())
                    .eq(StringUtils.isNotBlank(requestDTO.getContactMan()), ProductionManageProcurementSupplier.COL_CONTACT_MAN, requestDTO.getContactMan())
                    .eq(StringUtils.isNotBlank(requestDTO.getTelephone()), ProductionManageProcurementSupplier.COL_TELEPHONE, requestDTO.getTelephone())
                    .eq(Objects.nonNull(requestDTO.getSupplierTaxType()), ProductionManageProcurementSupplier.COL_SUPPLIER_TAX_TYPE, requestDTO.getSupplierTaxType())
                    .eq(Objects.nonNull(requestDTO.getSupplierInvoiceType()), ProductionManageProcurementSupplier.COL_SUPPLIER_INVOICE_TYPE, requestDTO.getSupplierInvoiceType())
                    .eq(Objects.nonNull(requestDTO.getRate()), ProductionManageProcurementSupplier.COL_RATE, requestDTO.getRate());
        } else {
            queryWrapper.and(wrapper -> wrapper
                    .or().like(ProductionManageProcurementSupplier.COL_SUPPLIER_NUMBER, search)
                    .or().like(ProductionManageProcurementSupplier.COL_SUPPLIER_NAME, search)
                    .or().like(ProductionManageProcurementSupplier.COL_CONTACT_MAN, search)
                    .or().like(ProductionManageProcurementSupplier.COL_TELEPHONE, search));
        }

        // 按照更新时间降序排列
        queryWrapper.orderByDesc(ProductionManageProcurementSupplier.COL_UPDATE_DATE);
        if (needAuth){
            // 添加数据权限
            commonUtil.roleDataAuthFilter(PURCHASING_SUPPLIER, queryWrapper, CREATED_BY, StringUtils.EMPTY);
        }
        return selectPage(page, queryWrapper);
    }

}
