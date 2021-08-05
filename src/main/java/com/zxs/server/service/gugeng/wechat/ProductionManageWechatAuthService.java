package com.zxs.server.service.gugeng.wechat;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.common.gugeng.util.wechat.token.WechatAccessToken;
import net.app315.hydra.intelligent.planting.pojo.gugeng.wechat.ProductionManageWechatAuth;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.wechat.ProductionManageWechatAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-11-18
 */
@Service
public class ProductionManageWechatAuthService extends ServiceImpl<ProductionManageWechatAuthMapper, ProductionManageWechatAuth> {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;

    @Autowired
    private WechatAccessToken wechatAccessToken;

    @Transactional
    public void add(String code) throws SuperCodeException {
        String openId= wechatAccessToken.getOpenId(code);
        ProductionManageWechatAuth entity = new ProductionManageWechatAuth();
        entity.setOpenID(openId);
        entity.setCreateUserId(commonUtil.getUserId());
        entity.setOrganizationId(commonUtil.getOrganizationId());
        entity.setCreateDate(new Date());
        baseMapper.insert(entity);
    }

    @Transactional
    public void update(Object obj) throws SuperCodeException {
        ProductionManageWechatAuth entity = new ProductionManageWechatAuth();
        // TODO 添加相应的业务逻辑
        baseMapper.updateById(entity);
    }

    public PageResults list(Object obj) throws SuperCodeException {
        Page<ProductionManageWechatAuth> page = new Page<>(1, 5);
        QueryWrapper<ProductionManageWechatAuth> queryWrapper = commonUtil.queryTemplate(ProductionManageWechatAuth.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public ProductionManageWechatAuth getById(String id) throws SuperCodeException {
        QueryWrapper<ProductionManageWechatAuth> queryWrapper = commonUtil.queryTemplate(ProductionManageWechatAuth.class);
        // TODO 添加相应的业务逻辑
        return null;
    }
}
