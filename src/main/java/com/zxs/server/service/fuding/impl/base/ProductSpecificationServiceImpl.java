package com.zxs.server.service.fuding.impl.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.common.pojo.common.Page;
import net.app315.hydra.intelligent.planting.bo.fuding.ProductSpecificationBO;
import net.app315.hydra.intelligent.planting.common.gugeng.model.RestResult;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.enums.EnableOrDisEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.ProductSpecificationDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.ProductSpecificationMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.IProductSpecificationService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationExportVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.ProductSpecificationVO;
import net.app315.hydra.user.sdk.provide.context.UserContextHelper;
import net.app315.hydra.user.sdk.provide.model.AccountCache;
import net.app315.hydra.user.sdk.provide.model.EmployeeCache;
import net.app315.nail.common.utils.UUIDUtil;
import net.app315.nail.common.utils.easyexcel.EasyExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 产品规格 服务实现类
 * </p>
 *
 * @author
 * @since 2019-12-13
 */
@Service
public class ProductSpecificationServiceImpl extends ServiceImpl<ProductSpecificationMapper, ProductSpecificationDO> implements IProductSpecificationService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private UserContextHelper userContextHelper;

    /**
     * 添加 产品规格
     */
    @Override
    public String addProductSpecification(ProductSpecificationBO model) {
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        // 判断产品规格是否已存在
        int count = this.count(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getSpecificationName, model.getSpecificationName())
                .eq(ProductSpecificationDO::getProductId,model.getProductId())
                .eq(ProductSpecificationDO::getOrganizationId,accountCache.getOrganizationCache().getOrganizationId()));
        if(count > 0){
            throw new TeaException("产品规格已存在，请勿重复添加");
        }
        ProductSpecificationDO entity = new ProductSpecificationDO();
        BeanUtils.copyProperties(model, entity);
        entity.setSpecificationId(UUIDUtil.getUUID());
        entity.setDeleted(EnableOrDisEnum.DIS_ENABLE.getStatus());
        entity.setStatus(EnableOrDisEnum.ENABLE.getStatus());
        entity.setOperator(accountCache.getUserId());
        entity.setOperatorName(accountCache.getUserName());
        entity.setOrganizationId(accountCache.getOrganizationCache().getOrganizationId());
        entity.setOrganizationName(accountCache.getOrganizationCache().getOrganizationFullName());
        EmployeeCache department = userContextHelper.getOrganization().getEmployeeCache();
        if (department != null) {
            entity.setDepartmentId(department.getDepartmentId());
            entity.setDepartmentName(department.getDepartmentName());
        }
        save(entity);
        return null;
    }

    /**
     * 修改 产品规格
     */
    @Override
    public void updateProductSpecification(ProductSpecificationBO model) {
        // 判断产品规格是否已存在
        AccountCache accountCache = userContextHelper.getUserLoginCache();
        int count = this.count(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getSpecificationName, model.getSpecificationName())
                .eq(ProductSpecificationDO::getProductId,model.getProductId())
                .ne(ProductSpecificationDO::getSpecificationId,model.getSpecificationId())
                .eq(ProductSpecificationDO::getOrganizationId,accountCache.getOrganizationCache().getOrganizationId()));
        if(count > 0){
            throw new TeaException("产品规格已存在，请勿重复添加");
        }
        ProductSpecificationDO entity = new ProductSpecificationDO();
        BeanUtils.copyProperties(model, entity);
        entity.setUpdateTime(new Date());
        updateById(entity);
    }

    /**
     * 获取 产品规格 详情
     */
    @Override
    public ProductSpecificationBO getProductSpecification(String specificationId) {
        ProductSpecificationDO entity = getOne(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getSpecificationId, specificationId));
        if (entity != null) {
            ProductSpecificationBO result = new ProductSpecificationBO();
            BeanUtils.copyProperties(entity, result);
            return result;
        }
        return null;
    }

    /**
     * 获取产品规格分页列表
     *
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<ProductSpecificationVO>> getProductSpecificationList(ProductSpecificationSearchModel model) {
        model.setGeneralParam(new String[]{"specification_name","product_name"});
        String organizationId = userContextHelper.getOrganizationId();
        model.setOrganizationId(organizationId);
        return selectPage(model, getBaseMapper(), ProductSpecificationVO.class);
    }

    /**
     * 禁用启用
     *
     * @param specificationId
     */
    @Override
    public void updateTeaFarmerStatus(String specificationId) {
        ProductSpecificationDO productSpecificationDO = getOne(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getSpecificationId, specificationId)
                .eq(ProductSpecificationDO::getOrganizationId, userContextHelper.getOrganizationId()));
        if (productSpecificationDO == null) {
            return;
        }
        if (EnableOrDisEnum.DIS_ENABLE.getStatus().equals(productSpecificationDO.getStatus())) {
            productSpecificationDO.setStatus(EnableOrDisEnum.ENABLE.getStatus());
        } else {
            productSpecificationDO.setStatus(EnableOrDisEnum.DIS_ENABLE.getStatus());
        }
        productSpecificationDO.setUpdateTime(new Date());
        updateById(productSpecificationDO);
    }

    /**
     * 根据产品获取规格列表
     * @param productId
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<ProductSpecificationDO>> getProductProductSpecificationPage(String productId) {
        List<ProductSpecificationDO> productSpecificationDOS = baseMapper.selectList(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getProductId, productId)
                .eq(ProductSpecificationDO::getStatus, EnableOrDisEnum.ENABLE.getStatus()));

        return new AbstractPageService.PageResults<>(productSpecificationDOS,new Page());
    }

    @Override
    public RestResult<AbstractPageService.PageResults<List<ProductSpecificationDO>>> specificationByProduct(String productId, DaoSearch daoSearch) {
        QueryWrapper<ProductSpecificationDO> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(productId),"product_id",productId);
        if (StringUtils.isNotBlank(daoSearch.getSearch())){
            queryWrapper.and(w->w.like("specification_name",daoSearch.getSearch()));
        }
        IPage<ProductSpecificationDO> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        IPage<ProductSpecificationDO> productSpecificationDOIPage = baseMapper.selectPage(page, queryWrapper);
        return CommonUtil.pageResult(productSpecificationDOIPage,null);
    }

    /**
     * 根据产品获取规格列表
     * @param productId
     * @return
     */
    @Override
    public List<ProductSpecificationBO> getProductProductSpecification(String productId) {
        List<ProductSpecificationDO> productSpecificationDOS = baseMapper.selectList(new QueryWrapper<ProductSpecificationDO>().lambda()
                .eq(ProductSpecificationDO::getProductId, productId)
                .eq(ProductSpecificationDO::getStatus, EnableOrDisEnum.ENABLE.getStatus()));
        if (CollectionUtils.isEmpty(productSpecificationDOS)) {
            return new ArrayList<>();
        }
        return CopyUtil.copyList(productSpecificationDOS, ProductSpecificationBO::new);
    }

    /**
     * 导出产品规格列表
     * @param model
     * @param response
     */
    @Override
    public void export(ProductSpecificationSearchModel model, HttpServletResponse response) {
        model.setPageSize(100000);
        AbstractPageService.PageResults<List<ProductSpecificationVO>> pageResults = getProductSpecificationList(model);
        try {
            EasyExcelUtil.export(response, ProductSpecificationExportVO.class,new ArrayList<>(),"产品规格",pageResults.getList());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
