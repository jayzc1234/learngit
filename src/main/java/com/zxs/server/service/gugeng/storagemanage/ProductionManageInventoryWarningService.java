package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.AddInventoryWarningRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductionManageInventoryWarning;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductionManageInventoryWarningMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-12
 */
@Service
public class ProductionManageInventoryWarningService extends ServiceImpl<ProductionManageInventoryWarningMapper, ProductionManageInventoryWarning> {

    @Autowired
    private CommonUtil commonUtil;


    /**
     * 添加/编辑库存预警信息
     *
     * @author shixiongfei
     * @date 2019-11-12
     * @updateDate 2019-11-12
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateWarning(AddInventoryWarningRequestDTO requestDTO) {
        String sysId = commonUtil.getSysId();
        String organizationId = commonUtil.getOrganizationId();
        // 1. 通过库存类型 + 产品 + 产品等级校验是否存在此预警信息，存在则新增，不存在则更新
        ProductionManageInventoryWarning warning =
                query().eq(StringUtils.isNotBlank(sysId), ProductionManageInventoryWarning.COL_SYS_ID, sysId)
                        .eq(StringUtils.isNotBlank(organizationId), ProductionManageInventoryWarning.COL_ORGANIZATION_ID, organizationId)
                        .eq(ProductionManageInventoryWarning.COL_PRODUCT_ID, requestDTO.getProductId())
                        .eq(ProductionManageInventoryWarning.COL_PRODUCT_LEVEL_CODE, requestDTO.getProductLevelCode())
                        .one();


        if (Objects.isNull(warning)) {
            ProductionManageInventoryWarning inventoryWarning = new ProductionManageInventoryWarning();
            BeanUtils.copyProperties(requestDTO, inventoryWarning);
            CustomAssert.isFail(save(inventoryWarning), "添加预警信息失败");
        } else {
            boolean isSuccess = update()
                    .set("warning_box_num", requestDTO.getWarningBoxNum())
                    .set("warning_quantity", requestDTO.getWarningQuantity())
                    .set("warning_weight", requestDTO.getWarningWeight())
                    .set("department_id", requestDTO.getDepartmentId())
                    .set("department_name", requestDTO.getDepartmentName())
                    .eq("id", warning.getId())
                    .update();

            CustomAssert.false2Error(isSuccess, "编辑预警信息失败");
        }
    }
}