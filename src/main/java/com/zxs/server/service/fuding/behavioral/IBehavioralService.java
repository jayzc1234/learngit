package com.zxs.server.service.fuding.behavioral;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.BehavioralBO;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.BehavioralDO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.BehavioralVO;
import net.app315.nail.common.page.service.BasePageService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 行为管理 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface IBehavioralService extends IService<BehavioralDO>, BasePageService {

    /**
     * 添加行为
     * @param behavioralBO 行为对象
     * @return java.lang.String 新增的行为记录id
     */
    String addBehavioral(BehavioralBO behavioralBO);


    /**
     * 修改行为信息
     * @param behavioralBO
     */
    void updateBehavioral(BehavioralBO behavioralBO);

    /**
     * 获取行为信息
     * @param behavioralId
     * @return
     */
    BehavioralBO getBehavioral(String behavioralId);

    /**
     * 删除行为信息
     * @param behavioralId
     * @return
     */
    void deleteBehavioral(String behavioralId);


    /**
     * 获取行为列表
     * @param model
     * @return
     */
    AbstractPageService.PageResults<List<BehavioralVO>> getBehavioralList(BehavioralSearchModel model);


    /**
     * 导出行为记录
     * @param model
     * @param response
     */
    void export(BehavioralSearchModel model, HttpServletResponse response);

}
