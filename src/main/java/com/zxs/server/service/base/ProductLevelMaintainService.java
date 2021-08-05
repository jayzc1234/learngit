package com.zxs.server.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.base.ProductLevelMaintain;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-10-14
 */
public interface ProductLevelMaintainService extends IService<ProductLevelMaintain> {

    /**
     * 获取列表
     * @param daoSearch
     * @return
     */
    IPage<ProductLevelMaintain> listPage(DaoSearch daoSearch);

    /**
     * 根据组织id和系统id获取产品等级
     * @param organizationId
     * @param sysId
     * @return
     */
    List<ProductLevelMaintain> selectByOrgAndSysId(String organizationId, String sysId, String productLevelName);
}
