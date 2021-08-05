package com.zxs.server.service.gugeng.purchasing;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.AddPurchasingApplicationProductRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.purchasing.UpdatePurchasingApplicationProductRequestDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.DeleteOrNotEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.purchasing.ProductionManagePurchasingApplicationProduct;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.purchasing.ProductionManagePurchasingApplicationProductMapper;
import net.app315.hydra.intelligent.planting.vo.gugeng.purchasing.SearchPurchasingApplicationProductDetailResponseVO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-10-11
 */
@Service
public class ProductionManagePurchasingApplicationProductService extends ServiceImpl<ProductionManagePurchasingApplicationProductMapper, ProductionManagePurchasingApplicationProduct> {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 批量添加采购申请商品
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void addBatchByPurchasingId(List<AddPurchasingApplicationProductRequestDTO> requestDTOList, Long purchasingManagementId) throws SuperCodeException {
        List<ProductionManagePurchasingApplicationProduct> products = requestDTOList.stream().map(requestDTO -> {
            ProductionManagePurchasingApplicationProduct entity = new ProductionManagePurchasingApplicationProduct();
            BeanUtils.copyProperties(requestDTO, entity);
            entity.setPurchasingManagementId(purchasingManagementId);
            return entity;
        }).collect(Collectors.toList());
        // 默认新增上限为1000
        CustomAssert.isSuccess(saveBatch(products), "添加采购申请物品失败");
    }


    /**
     * 批量更新或添加采购申请物品信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void updateOrAddBatchByPurchasingId(List<UpdatePurchasingApplicationProductRequestDTO> purchasingApplicationProducts, Long purchasingManagementId) throws SuperCodeException {
        // 类型转化为pojo
        List<ProductionManagePurchasingApplicationProduct> products = purchasingApplicationProducts.stream().map(product -> {
            ProductionManagePurchasingApplicationProduct applicationProduct = new ProductionManagePurchasingApplicationProduct();
            BeanUtils.copyProperties(product, applicationProduct);
            applicationProduct.setPurchasingManagementId(purchasingManagementId);
            return applicationProduct;
        }).collect(Collectors.toList());

        List<Long> ids = products.stream().filter(t -> Objects.nonNull(t.getId()))
                .map(ProductionManagePurchasingApplicationProduct::getId).collect(Collectors.toList());

        // 查询采购申请物品信息
        List<ProductionManagePurchasingApplicationProduct> oldProducts = getByPurchasingId(purchasingManagementId);
        // 剔除已删除的数据，筛选出和id不匹配的数据库数据，然后移除,不建议采用物理删除
        List<Long> oldIds = oldProducts.stream().filter(old -> !ids.contains(old.getId())).map(ProductionManagePurchasingApplicationProduct::getId).collect(Collectors.toList());
        // 如果不存在已删除数据，则不作处理
        if (CollectionUtils.isNotEmpty(oldIds)) {
            batchRemoveByIds(oldIds);
        }

        // 获取id非空的数据，进行更新操作
        List<ProductionManagePurchasingApplicationProduct> updateProducts = products.stream().filter(product -> Objects.nonNull(product.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(updateProducts)) {
            CustomAssert.isSuccess(updateBatchById(updateProducts), "更新采购申请物品信息失败");
        }

        // 筛选出采购申请id为空的数据, 进行新增操作
        List<ProductionManagePurchasingApplicationProduct> addProducts = products.stream().filter(product -> Objects.isNull(product.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addProducts)) {
            CustomAssert.isSuccess(saveBatch(addProducts), "更新采购申请物品信息失败");
        }
    }

    /**
     * 通过采购管理主键id获取采购申请物品详情列表信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public List<SearchPurchasingApplicationProductDetailResponseVO> listByPurchasingManageId(Long purchasingManageId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        List<ProductionManagePurchasingApplicationProduct> list = query().eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingApplicationProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingApplicationProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingApplicationProduct.COL_PURCHASING_MANAGEMENT_ID, purchasingManageId)
                .eq(ProductionManagePurchasingApplicationProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();

        // 防止出现npe
        list = Optional.ofNullable(list).orElse(Collections.emptyList());

        AtomicInteger count = new AtomicInteger(0);
        return list.stream().map(product -> {
            SearchPurchasingApplicationProductDetailResponseVO responseVO = new SearchPurchasingApplicationProductDetailResponseVO();
            BeanUtils.copyProperties(product, responseVO);
            responseVO.setMaterialType(product.getMaterialType().toString());
            responseVO.setSerialNumber(count.incrementAndGet());
            return responseVO;
        }).collect(Collectors.toList());
    }

    /**
     * 通过采购管理主键id移除指定的采购申请物品信息
     *
     * @author shixiongfei
     * @date 2019-10-11
     * @updateDate 2019-10-11
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void removeByPurchasingManageId(Long purchasingManageId) throws SuperCodeException {
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();

        boolean isSuccess = update().set(ProductionManagePurchasingApplicationProduct.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingApplicationProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingApplicationProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingApplicationProduct.COL_PURCHASING_MANAGEMENT_ID, purchasingManageId)
                // 排除已经删除过的采购申请物品信息
                .eq(ProductionManagePurchasingApplicationProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .update();
        CustomAssert.isSuccess(isSuccess, "删除采购申请物品信息失败");
    }

    /**
     * 通过采购申请商品id集合批次删除（采用软删除）
     *
     * @author shixiongfei
     * @date 2019-11-20
     * @updateDate 2019-11-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    public void batchRemoveByIds(List<Long> ids) throws SuperCodeException {
        String organizationId = commonUtil.getOrganizationId();
        String sysId = commonUtil.getSysId();

        boolean isSuccess = update().set(ProductionManagePurchasingApplicationProduct.COL_IS_DELETED, DeleteOrNotEnum.DELETED.getKey())
                .eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingApplicationProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingApplicationProduct.COL_ORGANIZATION_ID, organizationId)
                .in(ProductionManagePurchasingApplicationProduct.COL_ID, ids)
                // 排除已经删除过的采购申请物品信息, 这里采用乐观锁的更新方式来确保数据一致性
                .eq(ProductionManagePurchasingApplicationProduct.COL_IS_DELETED, DeleteOrNotEnum.NOT_DELETED.getKey())
                .update();
        CustomAssert.isSuccess(isSuccess, "批量删除采购申请物品信息失败");
    }

    /**
     * 通过采购id获取采购申请商品集合信息
     *
     * @author shixiongfei
     * @date 2019-11-20
     * @updateDate 2019-11-20
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    private List<ProductionManagePurchasingApplicationProduct> getByPurchasingId(Long purchasingManageId) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        return query().eq(StringUtils.isNotBlank(sysId), ProductionManagePurchasingApplicationProduct.COL_SYS_ID, sysId)
                .eq(StringUtils.isNotBlank(organizationId), ProductionManagePurchasingApplicationProduct.COL_ORGANIZATION_ID, organizationId)
                .eq(ProductionManagePurchasingApplicationProduct.COL_PURCHASING_MANAGEMENT_ID, purchasingManageId)
                // 排除已经删除过的采购申请物品信息
                .eq(ProductionManagePurchasingApplicationProduct.COL_ID, DeleteOrNotEnum.NOT_DELETED.getKey())
                .list();
    }
}
