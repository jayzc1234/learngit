package com.zxs.server.service.fuding.teagreen;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaGreenAcquisitionBO;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionDO;
import net.app315.hydra.intelligent.planting.vo.fuding.common.AcquisitionStatusVO;
import net.app315.hydra.intelligent.planting.vo.fuding.teagreen.*;
import net.app315.nail.common.page.service.BasePageService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 茶青收购记录 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface ITeaGreenAcquisitionService extends IService<TeaGreenAcquisitionDO>, BasePageService {


    /**
     * 获取茶青收购记录详情
     * @param acquisitionId 收购记录id
     * @return java.lang.String
     */
    TeaGreenAcquisitionBO getTeaGreenAcquisitionById(String acquisitionId);

    /**
     * 茶青收购记录列表查询
     * @param teaGreenAcquisitionSearchModel 记录搜索对象
     * @return TeaGreenAcquisitionVO 收购记录列表
     */
    AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> getTeaGreenAcquisition(TeaGreenAcquisitionSearchModel teaGreenAcquisitionSearchModel);


    /**
     * 统计一个组织当天的茶青收购的数据
     * @return 统计数据
     */
    TeaGreenAcquisitionRealTimeStatisticsVO getTeaGreenAcquisitionRealTimeStatistics();


    /**
     * 导出茶青收购记录
     * @param model
     * @param response
     */
    void export(TeaGreenAcquisitionExportModel model, HttpServletResponse response) throws Exception;

    /**
     * 茶青收购记录核销
     * @param acquisitionStatusVO
     */
    void writeOff(AcquisitionStatusVO acquisitionStatusVO);

    /**
     * 茶青收购记录作废
     * @param acquisitionStatusVO
     */
    void disable(AcquisitionStatusVO acquisitionStatusVO);

    /**
     * 批量提交茶青收购记录
     * @param model
     */
    TeaGreenAcquisitionVO batchRecord(TeaGreenAcquisitionAddModel model);


    /**
     * 根据交易id 获取单据全部信息
     * @param acquisitionId
     * @return
     */
    TeaGreenAcquisitionVO getAcquisition(String acquisitionId);

    AbstractPageService.PageResults<List<TeaGreenAcquisitionListVO>> getTeaGreenAcquisitionBareWeight(TeaGreenAcquisitionSearchModel model);

    TeaGreenAcquisitionVO saveBareWeight(TeaGreenAcquisitionAddModel model);

    void exportBareWeight(TeaGreenAcquisitionExportDTO model, HttpServletResponse response) throws ExcelException;

    void exportQuantity(TeaGreenAcquisitionExportDTO model, HttpServletResponse response) throws ExcelException;

    AbstractPageService.PageResults<List<TeaGreenAcquisitionVO>> getTeaGreenAcquisitionMobile(TeaGreenAcquisitionSearchModel model);

    void updateTeaFarmerByCooperativeId(CooperativeDO cooperativeDO);

}
