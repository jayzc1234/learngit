package com.zxs.server.service.gugeng.repairmanage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService.PageResults;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.SerialNumberGenerator;
import net.app315.hydra.intelligent.planting.pojo.gugeng.repairmanage.ProductionManageConsumeStuff;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.repairmanage.ProductionManageConsumeStuffMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author shixiongfei
 * @since 2019-09-30
 */
@Service
public class ProductionManageConsumeStuffService extends ServiceImpl<ProductionManageConsumeStuffMapper, ProductionManageConsumeStuff> {

    // 可在模版中添加相应的service通用方法，编辑模版在resources/templates/serviceImpl.java.vm文件中

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private SerialNumberGenerator numberGenerator;


    @Transactional
    public void add(Object obj) throws SuperCodeException {
        ProductionManageConsumeStuff entity = new ProductionManageConsumeStuff();
        // TODO 添加相应的业务逻辑
        baseMapper.insert(entity);
    }

    @Transactional
    public void update(Object obj) throws SuperCodeException {
        ProductionManageConsumeStuff entity = new ProductionManageConsumeStuff();
        // TODO 添加相应的业务逻辑
        baseMapper.updateById(entity);
    }

    public PageResults list(Object obj) throws SuperCodeException {
        Page<ProductionManageConsumeStuff> page = new Page<>(1, 5);
        QueryWrapper<ProductionManageConsumeStuff> queryWrapper = commonUtil.queryTemplate(ProductionManageConsumeStuff.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public ProductionManageConsumeStuff getById(String id) throws SuperCodeException {
        QueryWrapper<ProductionManageConsumeStuff> queryWrapper = commonUtil.queryTemplate(ProductionManageConsumeStuff.class);
        // TODO 添加相应的业务逻辑
        return null;
    }

    public void deleteByRepairApplyId(Long id) {
        baseMapper.deleteByRepairApplyId(id);
    }
}
