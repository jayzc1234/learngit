package com.zxs.server.service.fuding.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeYearOutputBO;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeYearOutputDO;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeYearOutputModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeYearOutputVO;
import net.app315.nail.common.page.service.BasePageService;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 联合体年收购记录表 服务类
 * </p>
 *
 * @author 
 * @since 2020-02-17
 */
public interface ICooperativeYearOutputService extends IService<CooperativeYearOutputDO> , BasePageService{

    /**
        添加 联合体年收购记录表
    */
    public String addCooperativeYearOutput(CooperativeYearOutputBO model);

    /**
            修改 联合体年收购记录表
      */
    public void updateCooperativeYearOutput(CooperativeYearOutputBO model);


      /**
           获取 联合体年收购记录表 详情
        */
    public CooperativeYearOutputBO getCooperativeYearOutput(String id);

    /**
     获取 联合体年收购记录表 详情
     */
    public CooperativeYearOutputBO getCooperativeYearOutputByCooperativeId(String cooperativeId);


    /**
     * 修改当年联合体年产限量值
     */
    public  void updateCooperativeYearOutputWarnValue(String cooperativeId, BigDecimal warnValue);

    /**
     * 修改或新增当年联合体年产限量值
     */
    public  void updateOrAddCooperativeYearOutputWarnValue(String cooperativeId, BigDecimal warnValue);


    /**
      * 获取联合体年收购记录表分页列表
      * @return
      */
    public AbstractPageService.PageResults<List<CooperativeYearOutputVO>> getCooperativeYearOutputList(CooperativeYearOutputModel model);

    /**
     * 修改联合体年收购数据  没有原数据就新建，有就修改
     * YES 标识添加  NO 标识减少
     * @param cooperativeYearOutputDO
     */
    public void updateCurrentYearOutPut(CooperativeYearOutputDO cooperativeYearOutputDO, YesOrNoEnum yesOrNoEnum);

    /**
     * 添加当年的年产初始记录
     */
    void addCurrentYearOutPut(CooperativeDO cooperativeDO);
}
