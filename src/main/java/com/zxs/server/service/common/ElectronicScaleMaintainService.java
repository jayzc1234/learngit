package com.zxs.server.service.common;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicPushDTO;
import net.app315.hydra.intelligent.planting.dto.common.ElectronicScaleMaintainDTO;
import net.app315.hydra.intelligent.planting.pojo.common.ElectronicScaleMaintain;
import net.app315.hydra.intelligent.planting.server.mapper.common.ElectronicScaleMaintainMapper;
import net.app315.hydra.intelligent.planting.server.service.PageSearchService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2021-02-02
 */
public interface ElectronicScaleMaintainService extends IService<ElectronicScaleMaintain>, PageSearchService<ElectronicScaleMaintainMapper, ElectronicScaleMaintain> {

    /**
     * 编辑
     * @param id
     * @param scaleMaintainDTO
     */
    void update(Long id, ElectronicScaleMaintainDTO scaleMaintainDTO);

    /**
     * 新增
     * @param scaleMaintainDTO
     * @return
     */
    ElectronicScaleMaintain add(ElectronicScaleMaintainDTO scaleMaintainDTO);

    /**
     * 列表
     * @param daoSearch
     * @return
     */
    IPage<ElectronicScaleMaintain> pageList(DaoSearch daoSearch);

    /**
     * 保存推送信息
     * @param electronicPushDTO
     * @return
     */
    void pushInfo(ElectronicPushDTO electronicPushDTO);
}
