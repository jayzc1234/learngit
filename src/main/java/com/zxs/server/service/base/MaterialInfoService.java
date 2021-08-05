package com.zxs.server.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoDTO;
import net.app315.hydra.intelligent.planting.dto.base.MaterialInfoUpdateDTO;
import net.app315.hydra.intelligent.planting.pojo.base.MaterialInfo;
import net.app315.hydra.intelligent.planting.server.mapper.base.MaterialInfoMapper;
import net.app315.hydra.intelligent.planting.server.service.PageSearchService;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoDetailVO;
import net.app315.hydra.intelligent.planting.vo.base.MaterialInfoListVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-08-20
 */

public interface MaterialInfoService extends IService<MaterialInfo>, PageSearchService<MaterialInfoMapper, MaterialInfo> {
    /**
     * 添加物料
     * @param materialInfoDTO
     */
    void add(MaterialInfoDTO materialInfoDTO);

    /**
     * 编辑物料
     * @param id
     * @param materialInfoUpdateDTO
     */
    void update(Long id, MaterialInfoUpdateDTO materialInfoUpdateDTO);

    /**
     * 查看
     * @param id
     * @return
     */
    MaterialInfoDetailVO getDetailById(Long id);

    /**
     * 禁用启用物料
     * @param id
     * @param disableFlag
     */
    void disable(Long id, Integer disableFlag);

    /**
     * 物料列表分页
     * @param daoSearch
     * @param <P>
     * @return
     */
    <P extends DaoSearch> IPage<MaterialInfoListVO> pageList(Integer disableFlag, Long materialSortId, P daoSearch);

    /**
     * 列表下拉
     * @param materialSortId
     * @param materialName
     * @param daoSearch
     * @return
     */
    IPage<MaterialInfoListVO> dropPage(Long materialSortId, String materialName, DaoSearch daoSearch);

}
