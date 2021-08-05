package com.zxs.server.service.fuding.behavioral;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.DailyInspectionBO;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.DailyInspectionDO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionSearchModel;
import net.app315.hydra.intelligent.planting.vo.fuding.behavioral.DailyInspectionVO;
import net.app315.nail.common.page.service.BasePageService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 日常巡检 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface IDailyInspectionService extends IService<DailyInspectionDO> , BasePageService {

    /**
     * 添加日常巡检记录
     * @param dailyInspectionBO 巡检记录对象
     * @return java.lang.String 记录id
     */
    String addDailyInspection(DailyInspectionBO dailyInspectionBO);


    /**
     * 修改日常巡检记录
     * @param dailyInspectionBO 巡检记录
     */
    void updateDailyInspection(DailyInspectionBO dailyInspectionBO);


    /**
     * 删除巡检记录
     * @param inspectionId 巡检记录id
     * @return
     */
    void deleteDailyInspection(String inspectionId);

    /**
     * 查询日常巡检记录列表
     * @param dailyInspectionSearchModel
     * @return 日常巡检记录列表
     */
    AbstractPageService.PageResults<List<DailyInspectionListVO>> getDailyInspectionList(DailyInspectionSearchModel dailyInspectionSearchModel);

    /**
     * 导出日常巡检数据
     * @param model
     * @param response
     */
    void export(DailyInspectionSearchModel model, HttpServletResponse response);

    /**
     * 获取巡检记录详情
     * @param inspectionId
     */
    DailyInspectionVO getInspectionInfo(String inspectionId);
}
