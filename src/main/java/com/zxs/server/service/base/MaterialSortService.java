package com.zxs.server.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialSortListDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialSort;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialSortMapper;
import net.app315.hydra.intelligent.planting.server.service.PageSearchService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-20
 */
public interface MaterialSortService extends IService<MaterialSort>, PageSearchService<MaterialSortMapper, MaterialSort> {

    /**
     * 更新禁用启用状态
     * @param id
     * @param status
     */
    void updateStatus(Integer id, Integer status);

    /**
     * 新增物料分类
     * @param  materialSortDTO
     */
    void add(MaterialSortDTO materialSortDTO);

    /**
     * 编辑物料分类
     * @param  materialSortDTO
     */
    void update(MaterialSortDTO materialSortDTO);

    /**
     * 列表
     * @param daoSearch
     * @return
     */
    IPage<MaterialSort> pageList(MaterialSortListDTO daoSearch);
}
