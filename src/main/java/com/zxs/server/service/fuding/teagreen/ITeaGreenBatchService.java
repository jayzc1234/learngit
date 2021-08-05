package com.zxs.server.service.fuding.teagreen;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.page.DaoSearch;
import net.app315.hydra.intelligent.planting.dto.storehouse.AddSemiInStorageRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenBatch;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaGreenBatchListModel;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenAndSemiProductStatisticsQuery;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.TeaGreenBatchProductLevelVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author caihong
 * @since 2020-09-07
 */
public interface ITeaGreenBatchService extends IService<TeaGreenBatch> {

    AbstractPageService.PageResults<List<TeaGreenBatch>> pageList(TeaGreenBatchListModel model);

    void export(DaoSearch model, HttpServletResponse response) throws SuperCodeException;

    void saveTeaBatch(String organizationId, String token);

    AbstractPageService.PageResults<List<AddSemiInStorageRequestDTO>> listSemi(TeaGreenBatchListModel model);

    /**
     * 茶青成品对比 -茶青数据
     * @param model
     * @param startTime
     * @param endTime
     * @return
     */
    List<TeaGreenBatch> statisticsData(TeaGreenAndSemiProductStatisticsQuery model, String startTime, String endTime);

    /**
     * 获取该产品下的产品等级
     * @param model
     * @return
     */
    AbstractPageService.PageResults<List<TeaGreenBatchProductLevelVO>> productPageList(TeaGreenBatchListModel model);
}
