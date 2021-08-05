package com.zxs.server.service.gugeng.storagemanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductManageDeliveryInfoDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.storagemanage.ProductManageDeliveryInfoListDTO;
import net.app315.hydra.intelligent.planting.enums.gugeng.storagemanage.DeliveryInfoTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.storagemanage.ProductManageDeliveryInfo;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.storagemanage.ProductManageDeliveryInfoMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.BaseService;
import net.app315.hydra.user.data.auth.sdk.model.InterceptorUserRoleDataAuth;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static net.app315.hydra.intelligent.planting.common.gugeng.constants.DataPermissionConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-04
 */
@Service
public class ProductManageDeliveryInfoService extends ServiceImpl<ProductManageDeliveryInfoMapper, ProductManageDeliveryInfo> implements BaseService {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    public void add(ProductManageDeliveryInfoDTO deliveryInfoDTO) throws SuperCodeException {
        existCheckExcludeId(null, deliveryInfoDTO.getDeliveryInfoName(), deliveryInfoDTO.getDeliveryType());
        ProductManageDeliveryInfo entity = new ProductManageDeliveryInfo();
        entity.setDeliveryInfoName(deliveryInfoDTO.getDeliveryInfoName());
        entity.setDeliveryType(deliveryInfoDTO.getDeliveryType());
        Employee employee = commonUtil.getEmployee();
        entity.setCreateEmployeeId(employee.getEmployeeId());
        entity.setCreateEmployeeName(employee.getName());
        entity.setCreateDepartmentId(employee.getDepartmentId());
        entity.setCreateDate(CommonUtil.getCurrentDate(DateTimePatternConstant.YYYY_MM_DD_HH_MM_SS));
        baseMapper.insert(entity);
    }

    public void update(ProductManageDeliveryInfoDTO deliveryInfoDTO) throws SuperCodeException {
        existCheckExcludeId(deliveryInfoDTO.getId(), deliveryInfoDTO.getDeliveryInfoName(), deliveryInfoDTO.getDeliveryType());
        ProductManageDeliveryInfo entity = new ProductManageDeliveryInfo();
        entity.setId(deliveryInfoDTO.getId());
        entity.setDeliveryInfoName(deliveryInfoDTO.getDeliveryInfoName());
        baseMapper.updateById(entity);
    }

    @Override
    public IPage<ProductManageDeliveryInfo> pageList(DaoSearch daoSearch) throws SuperCodeException {
        ProductManageDeliveryInfoListDTO deliveryInfoListDTO= (ProductManageDeliveryInfoListDTO) daoSearch;
        Page<ProductManageDeliveryInfo> page = CommonUtil.genPage(deliveryInfoListDTO);
        QueryWrapper<ProductManageDeliveryInfo> queryWrapper = commonUtil.queryTemplate(ProductManageDeliveryInfo.class);
        queryWrapper.eq(ProductManageDeliveryInfo.COL_DELIVERY_TYPE, deliveryInfoListDTO.getDeliveryType());

        // v1.9  添加数据权限
        InterceptorUserRoleDataAuth roleDataAuth= commonUtil.getRoleFunAuthWithAuthCode(DELIVERY_INFO_MANAGEMENT);
        commonUtil.setAuthFilter(queryWrapper, roleDataAuth, DELIVERY_INFO_USER_ID, AUTH_DEPARTMENT_ID,  ProductManageDeliveryInfo.class);

        queryWrapper.orderByDesc(ProductManageDeliveryInfo.COL_ID);
        String search = deliveryInfoListDTO.getSearch();
        queryWrapper.like(StringUtils.isNotBlank(search), ProductManageDeliveryInfo.COL_DELIVERY_INFO_NAME, search);
        return baseMapper.selectPage(page,queryWrapper);
    }

    public void existCheckExcludeId(Long id,String deliveryInfoName,Integer deliveryType) throws SuperCodeException {
        QueryWrapper<ProductManageDeliveryInfo> queryWrapper = commonUtil.queryTemplate(ProductManageDeliveryInfo.class);
        queryWrapper.eq(ProductManageDeliveryInfo.COL_DELIVERY_INFO_NAME, deliveryInfoName);
        queryWrapper.eq(ProductManageDeliveryInfo.COL_DELIVERY_TYPE, deliveryType);
        queryWrapper.ne(!Objects.isNull(id), ProductManageDeliveryInfo.COL_ID, id);
        ProductManageDeliveryInfo productManageDeliveryInfo = baseMapper.selectOne(queryWrapper);
        if (null != productManageDeliveryInfo) {
            String desc = DeliveryInfoTypeEnum.getDesc(deliveryType);
            CommonUtil.throwSuperCodeExtException(500, desc + "已存在");
        }
    }

    public void deleteById(Long id) {
        baseMapper.deleteById(id);
    }
}
