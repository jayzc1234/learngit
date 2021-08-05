package com.zxs.server.service.fuding.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeBO;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeBatchListModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeExportDTO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeSearchModel;
import net.app315.nail.common.page.service.BasePageService;
import net.app315.nail.common.result.RichResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 联合体 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface ICooperativeService extends IService<CooperativeDO> , BasePageService {

    /**
     * 添加联合体
     * @param cooperativeBO
     * @return
     */
    String addCooperative(CooperativeBO cooperativeBO);



    /**
     * 批量添加联合体
     * @param model
     * @return
     */
    RichResult batchAddCooperative(CooperativeBatchListModel model);


    /**
     * 修改联合体
     * @param cooperativeBO
     */
    void updateCooperative(CooperativeBO cooperativeBO);


    /**
     * 修改联合体的预警值
     * @param CooperativeId 联合体id
     * @param warnValue  值
     */
    void updateCooperativeAmountWarn(String CooperativeId, BigDecimal warnValue);

    /**
     * 获取联合体信息
     * @param cooperativeId
     * @return
     */
    CooperativeBO getCooperative(String cooperativeId);

    /**
     * 获取合作分页列表
     * @param model
     * @return
     */
    AbstractPageService.PageResults<List<CooperativeListVO>> getCooperativeList(CooperativeSearchModel model);


    /**
     * 根据部门获取联合体
     * @param departmentId 部门id
     * @return
     */
    public CooperativeListVO getCooperativeListByDepartmentId(String departmentId);


    /**
     * 修改启用禁用状态
     * @param cooperativeId
     */
    void updateCooperativeStatus(String cooperativeId);

    /**
     * 获取编号
     * @return 编号
     */
    String getNo();

    /**
     * 导出联合体
     * @param model
     * @param response
     */
    void export(CooperativeExportDTO daoSearch, HttpServletResponse response) throws ExcelException;


    /**
     * 导入联合体
     * @param file
     */
    void importExcel(InputStream file) throws IOException;

    /**
     * 计算茶园面积和茶青数量
     */
    void calculatorAreaAndQuantity();
}
