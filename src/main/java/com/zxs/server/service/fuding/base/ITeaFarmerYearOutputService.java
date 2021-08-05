package com.zxs.server.service.fuding.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerYearOutputBO;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerYearOutputDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerYearOutputModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerYearOutputVO;
import net.app315.nail.common.page.service.BasePageService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 茶农社年收购记录表 服务类
 * </p>
 *
 * @author 
 * @since 2020-02-17
 */
public interface ITeaFarmerYearOutputService extends IService<TeaFarmerYearOutputDO> , BasePageService{

    /**
        添加 茶农社年收购记录表
    */
    public String addTeaFarmerYearOutput(TeaFarmerYearOutputBO model);

    /**
            修改 茶农社年收购记录表
      */
    public void updateTeaFarmerYearOutput(TeaFarmerYearOutputBO model);
    /**
     修改 茶农社年收购记录表
     */
    public void updateOrAddTeaFarmerYearOutputWarnValue(String cooperativeId, String farmerId, BigDecimal warnValue);

    /**
     修改 茶农当年年产收购限量
     */
    public void updateTeaFarmerYearOutputWarnValue(String cooperativeId, String farmerId, BigDecimal warnValue);


    /**
           获取 茶农社年收购记录表 详情
        */
    public TeaFarmerYearOutputBO getTeaFarmerYearOutput(String id);

    /**
     获取 茶农社年收购记录表 详情
     */
    public TeaFarmerYearOutputBO getTeaFarmerYearOutputByFarmerId(String farmerId, String cooperativeId);


    /**
      * 获取茶农社年收购记录表分页列表
      * @return
      */
    public AbstractPageService.PageResults<List<TeaFarmerYearOutputVO>> getTeaFarmerYearOutputList(TeaFarmerYearOutputModel model);

    /**
     * 修改茶农年收购数据  没有原数据就新建，有就修改
     *  * YES 标识添加  NO 标识减少
     * @param teaFarmerYearOutputDO
     */
    public void updateCurrentYearOutPut(TeaFarmerYearOutputDO teaFarmerYearOutputDO, YesOrNoEnum yesOrNoEnum);

    /**
     * 添加当年的年产初始记录
     */
    void addCurrentYearOutPut(TeaFarmerDO teaFarmerDO);
}
