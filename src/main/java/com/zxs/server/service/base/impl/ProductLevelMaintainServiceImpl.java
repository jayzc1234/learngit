package com.zxs.server.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.pojo.base.ProductLevelMaintain;
import net.app315.hydra.intelligent.planting.server.mapper.base.ProductLevelMaintainMapper;
import net.app315.hydra.intelligent.planting.server.service.base.ProductLevelMaintainService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2020-10-14
 */
@Service
public class ProductLevelMaintainServiceImpl extends ServiceImpl<ProductLevelMaintainMapper, ProductLevelMaintain> implements ProductLevelMaintainService {

    @Autowired
    private CommonUtil commonUtil;

    /**
     * 查询该企业下是否有产品等级
     * 没有的话查询组织id和系统id为空的初始化产品等级执行插入
     * 有的话直接返回
     * @param daoSearch
     * @return
     */
    @Override
    public IPage<ProductLevelMaintain> listPage(DaoSearch daoSearch) {

        QueryWrapper<ProductLevelMaintain> productLevelMaintainQueryWrapper = commonUtil.queryTemplate(ProductLevelMaintain.class);
        List<ProductLevelMaintain> productLevelMaintains = baseMapper.selectList(productLevelMaintainQueryWrapper);
        if (CollectionUtils.isEmpty(productLevelMaintains)){
            productLevelMaintainQueryWrapper = new QueryWrapper<>();
            productLevelMaintainQueryWrapper.isNull(ProductLevelMaintain.COL_ORGANIZATION_ID);
            productLevelMaintainQueryWrapper.isNull(ProductLevelMaintain.COL_SYS_ID);
            productLevelMaintains = baseMapper.selectList(productLevelMaintainQueryWrapper);
            for (ProductLevelMaintain productLevelMaintain : productLevelMaintains) {
                productLevelMaintain.setId(null);
            }
            saveBatch(productLevelMaintains);
        }
        Page<ProductLevelMaintain> page = new Page<>(daoSearch.getDefaultCurrent(),daoSearch.getDefaultPageSize());
        IPage<ProductLevelMaintain> productLevelMaintainIPage = baseMapper.selectPage(page, productLevelMaintainQueryWrapper);
        return productLevelMaintainIPage;
    }

    @Override
    public List<ProductLevelMaintain> selectByOrgAndSysId(String organizationId, String sysId,String productLevelName) {
        QueryWrapper<ProductLevelMaintain> productLevelMaintainQueryWrapper = new QueryWrapper<>();
        productLevelMaintainQueryWrapper.eq(ProductLevelMaintain.COL_ORGANIZATION_ID,organizationId);
        productLevelMaintainQueryWrapper.eq(ProductLevelMaintain.COL_SYS_ID,sysId);
        productLevelMaintainQueryWrapper.eq(StringUtils.isNotBlank(productLevelName),ProductLevelMaintain.COL_PRODUCT_LEVEL_NAME,productLevelName);
        return baseMapper.selectList(productLevelMaintainQueryWrapper);
    }
}
