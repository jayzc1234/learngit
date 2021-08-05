package com.zxs.server.service.anjiwhitetea;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateAddDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateDTO;
import net.app315.hydra.intelligent.planting.dto.anjiwritetea.BambooBasketAssociateListDTO;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.BambooBasketAssociate;
import net.app315.hydra.intelligent.planting.vo.anjiwhitetea.BambooBasketAssociateListVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author shixiongfei
 * @since 2020-04-14
 */
public interface BambooBasketAssociateService extends IService<BambooBasketAssociate> {

    /**
     * 添加竹筐关联
     * @param basketAssociateAddDTO
     */
    void add(BambooBasketAssociateAddDTO basketAssociateAddDTO);

    /**
     * 更新竹筐关联
     * @param associateDTO
     */
    void update(BambooBasketAssociateDTO associateDTO);

    /**
     * 获取关联列表
     * @param associateListDTO
     * @return
     */
    AbstractPageService.PageResults<List<BambooBasketAssociateListVO>> pageList(BambooBasketAssociateListDTO associateListDTO);
}
