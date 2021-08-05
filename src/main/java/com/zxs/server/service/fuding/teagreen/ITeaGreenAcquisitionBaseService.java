package com.zxs.server.service.fuding.teagreen;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaGreenAcquisitionBO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionBaseDO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionPdaListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAcquisitionVO;
import net.app315.nail.common.page.service.BasePageService;

import java.util.List;

/**
 * <p>
 * 茶青收购记录 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface ITeaGreenAcquisitionBaseService extends IService<TeaGreenAcquisitionBaseDO>, BasePageService {


    /**
     * 添加茶青收购记录
     * @param teaGreenAcquisitionBo 茶青收购对象
     * @return 茶青收购记录
     */
    TeaGreenAcquisitionVO addAcquisition(TeaGreenAcquisitionBO teaGreenAcquisitionBo);


    /**
     * 茶青收购记录列表查询
     * @return TeaGreenAcquisitionVO 收购记录列表
     */
    AbstractPageService.PageResults<List<TeaGreenAcquisitionPdaListVO>> getPdaTeaGreenAcquisition(TeaGreenAcquisitionSearchModel teaGreenAcquisitionSearchModel);

    TeaGreenAcquisitionVO updateAcquisition(TeaGreenAcquisitionBO teaGreenAcquisitionBO);

}
