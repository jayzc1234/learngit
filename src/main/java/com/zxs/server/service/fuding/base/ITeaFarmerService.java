package com.zxs.server.service.fuding.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerBO;
import net.app315.hydra.intelligent.planting.exception.gugeng.base.ExcelException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerBatchListModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerExportDTO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerListVO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerSearchModel;
import net.app315.nail.common.page.service.BasePageService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 茶农 服务类
 * </p>
 *
 * @author chenguanyu
 * @since 2019-11-07
 */
public interface ITeaFarmerService extends IService<TeaFarmerDO>, BasePageService {

    /**
     * 修改茶农状态
     * @param farmerId 茶农id
     */
    void updateTeaFarmerStatus(String farmerId);

    /**
     * 添加茶农
     * @param teaFarmerBo 茶农信息对象
     * @return 茶农id
     */
    String addTeaFarmer(TeaFarmerBO teaFarmerBo);


    /**
     * 批量添加茶农
     * @param model 茶农信息对象列表
     */
    void batchAddTeaFarmer(TeaFarmerBatchListModel model);

    /**
     * 修改茶农信息
     * @param teaFarmerBo 茶农信息对象
     */
    void updateTeaFarmer(TeaFarmerBO teaFarmerBo);

    /**
     * 获取茶农信息
     * @param farmerId
     * @return
     */
    TeaFarmerBO getTeaFarmer(String farmerId);

    /**
     * 获取茶农列表
     * @param model 查询条件对象
     * @return 分页茶农数据
     */
    AbstractPageService.PageResults<List<TeaFarmerListVO>> getTeaFarmerList(TeaFarmerSearchModel model);


    /**
     * 获取茶农列表
     * @return 分页茶农数据
     */
    AbstractPageService.PageResults<List<TeaFarmerListVO>> getTeaFarmerListByCooperativeId(TeaFarmerSearchModel model);

    /**
     * 修改茶农分数
     * @param farmerId 茶农id
     * @param fraction 要添加或减少的分数，正数加，负数减
     * @return void
     */
    void addTeaFarmerFraction(String farmerId, Integer fraction);

    /**
     * 导出茶农
     * @param daoSearch
     * @param response
     */
    void export(TeaFarmerExportDTO daoSearch, HttpServletResponse response) throws ExcelException;

    /**
     * 导入茶农
     * @param file
     */
    void importExcel(InputStream file) throws IOException;

    List<CooperativeDO> calculatorAreaAndQuantity();

    void updateTeaFarmerByCooperativeId(CooperativeDO cooperativeDO);
}
