package com.zxs.server.service.fuding.teagreen;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenProductLevelDO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenProductLevelAddModel;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author caihong
 * @since 2021-07-05
 */
public interface TeaGreenProductLevelService extends IService<TeaGreenProductLevelDO> {
    /**
     * 新增农产品等级
     * @param obj
     */
    void add(TeaGreenProductLevelAddModel obj);

    /**
     * 更新农产品等级
     * @param obj
     * @param id
     */
    void update(TeaGreenProductLevelAddModel obj, Integer id);

    /**
     * 假删除农产品等级
     * @param id
     */
    void delete(Integer id);

    /**
     * 后台列表
     * @param daoSearch
     * @return
     */
    IPage<TeaGreenProductLevelDO> listPage(DaoSearch daoSearch);

    /**
     * 下拉列表
     * @param productLevelName
     * @return
     */
    IPage<TeaGreenProductLevelDO> selectByOrgAndSysId(DaoSearch daoSearch);
}
