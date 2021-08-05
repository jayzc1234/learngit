package com.zxs.server.service.fuding.impl.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.CooperativeYearOutputBO;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeYearOutputDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.CooperativeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.CooperativeYearOutputMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ICooperativeYearOutputService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeYearOutputModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.CooperativeYearOutputVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 联合体年收购记录表 服务实现类
 * </p>
 *
 * @author 
 * @since 2020-02-17
 */
@Service
public class CooperativeYearOutputServiceImpl extends ServiceImpl<CooperativeYearOutputMapper, CooperativeYearOutputDO> implements ICooperativeYearOutputService {

    @Autowired
    private TeaGreenAcquisitionMapper teaGreenAcquisitionMapper;
    @Autowired
    private HairyTeaAcquisitionMapper hairyTeaAcquisitionMapper;
    @Autowired
    private CooperativeMapper cooperativeMapper;
    /**
        添加 联合体年收购记录表
    */
    @Override
    public String addCooperativeYearOutput(CooperativeYearOutputBO model){
        CooperativeYearOutputDO entity = new CooperativeYearOutputDO();
        BeanUtils.copyProperties(model,entity);
        save(entity);
        return null;
    }

    /**
          修改 联合体年收购记录表
     */
     @Override
     public void updateCooperativeYearOutput(CooperativeYearOutputBO model){
      CooperativeYearOutputDO entity = new CooperativeYearOutputDO();
      BeanUtils.copyProperties(model,entity);
      updateById(entity);
     }

     /**
           获取 联合体年收购记录表 详情
     */
    @Override
    public CooperativeYearOutputBO getCooperativeYearOutput(String id){
         CooperativeYearOutputDO entity = getOne(new QueryWrapper<CooperativeYearOutputDO>().eq("id",id));
         if(entity!=null){
            CooperativeYearOutputBO result = new CooperativeYearOutputBO();
            BeanUtils.copyProperties(entity,result);
            return result;
         }
        return null;
     }

    @Override
    public CooperativeYearOutputBO getCooperativeYearOutputByCooperativeId(String cooperativeId) {
        if(StringUtils.isBlank(cooperativeId)){
            return null;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        CooperativeYearOutputDO cooperativeYearOutputDO = getOne(new QueryWrapper<CooperativeYearOutputDO>().lambda()
                .eq(CooperativeYearOutputDO::getCooperativeId, cooperativeId).eq(CooperativeYearOutputDO::getCurrentYear,currentYear));
        return CopyUtil.copy(cooperativeYearOutputDO,new CooperativeYearOutputBO());
    }

    /**
     * 修改当年联合体年产限量值
     */
    @Override
    public void updateCooperativeYearOutputWarnValue(String cooperativeId, BigDecimal warnValue) {
        if(StringUtils.isBlank(cooperativeId)){
            return ;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        CooperativeYearOutputDO cooperativeYearOutputDO = getOne(new QueryWrapper<CooperativeYearOutputDO>().lambda()
                .eq(CooperativeYearOutputDO::getCooperativeId, cooperativeId).eq(CooperativeYearOutputDO::getCurrentYear,currentYear));
        if(cooperativeYearOutputDO != null){
            cooperativeYearOutputDO.setAmountWarn(warnValue);
            updateById(cooperativeYearOutputDO);
        }
    }

    @Override
    public void updateOrAddCooperativeYearOutputWarnValue(String cooperativeId, BigDecimal warnValue) {
        if(StringUtils.isBlank(cooperativeId)){
            return ;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        CooperativeYearOutputDO cooperativeYearOutputDO = getOne(new QueryWrapper<CooperativeYearOutputDO>().lambda()
                .eq(CooperativeYearOutputDO::getCooperativeId, cooperativeId).eq(CooperativeYearOutputDO::getCurrentYear,currentYear));
        if(cooperativeYearOutputDO != null){
            cooperativeYearOutputDO.setAmountWarn(warnValue);
            updateById(cooperativeYearOutputDO);
        }else{
            CooperativeYearOutputDO cooperativeYearOutput = getCooperativeYearOutput(cooperativeId,currentYear);
            this.baseMapper.insert(cooperativeYearOutput);
        }
    }

    /**
     * 获取联合体年收购记录表分页列表
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<CooperativeYearOutputVO>> getCooperativeYearOutputList(CooperativeYearOutputModel model){
        return selectPage(model,getBaseMapper(), CooperativeYearOutputVO.class);
    }


    /**
     * 修改联合体年收购数据  没有原数据就新建，有就修改
     * YES 标识添加  NO 标识减少
     * @param cooperativeYearOutputDO
     */
    @Override
    public void updateCurrentYearOutPut(CooperativeYearOutputDO cooperativeYearOutputDO, YesOrNoEnum yesOrNoEnum){
        if(null == cooperativeYearOutputDO.getAmount()){
            throw new TeaException("收购金额不能为空！！！");
        }
        if(null == cooperativeYearOutputDO.getQuantity()){
            throw new TeaException("收购数量不能为空！！！");
        }
        if(StringUtils.isEmpty(cooperativeYearOutputDO.getCooperativeId())){
            throw new TeaException("联合体不能为空！！！");
        }
        if(StringUtils.isEmpty(cooperativeYearOutputDO.getCurrentYear())){
            cooperativeYearOutputDO.setCurrentYear(String.valueOf(LocalDate.now().getYear()));
        }
        CooperativeYearOutputDO yearOutputDO = this.baseMapper.selectOne(new QueryWrapper<CooperativeYearOutputDO>().lambda()
                .eq(CooperativeYearOutputDO::getCooperativeId,cooperativeYearOutputDO.getCooperativeId())
                .eq(CooperativeYearOutputDO::getCurrentYear, cooperativeYearOutputDO.getCurrentYear()));

        if (YesOrNoEnum.YES.equals(yesOrNoEnum)){
            //不存在当年收购数据
            if(yearOutputDO == null){
                CooperativeYearOutputDO cooperativeYearOutput = getCooperativeYearOutput(cooperativeYearOutputDO.getCooperativeId(),cooperativeYearOutputDO.getCurrentYear());
               /* cooperativeYearOutput.setAmount(cooperativeYearOutput.getAmount().add(cooperativeYearOutputDO.getAmount()));
                cooperativeYearOutput.setQuantity(cooperativeYearOutput.getQuantity().add(cooperativeYearOutputDO.getQuantity()));*/
                this.baseMapper.insert(cooperativeYearOutput);
            }else{
                // 修改年收购总量和总金额
                this.baseMapper.updateCurrentOutPut(yearOutputDO.getId(),
                        cooperativeYearOutputDO.getAmount(),cooperativeYearOutputDO.getQuantity());
            }
        }else{
            if(yearOutputDO == null){
                return;
            }
            // 修改年收购总量和总金额
            this.baseMapper.lessCurrentOutPut(yearOutputDO.getId(),
                    cooperativeYearOutputDO.getAmount(),cooperativeYearOutputDO.getQuantity());
        }

    }

    /**
     * 添加当年的年产初始记录
     */
    @Override
    public void addCurrentYearOutPut(CooperativeDO cooperativeDO) {
        CooperativeYearOutputDO cooperativeYearOutputDO = new CooperativeYearOutputDO();
        CopyUtil.copy(cooperativeDO,cooperativeYearOutputDO);
        cooperativeYearOutputDO.setQuantity(new BigDecimal(0));
        cooperativeYearOutputDO.setAmount(new BigDecimal(0));
        cooperativeYearOutputDO.setCurrentYear(String.valueOf(LocalDate.now().getYear()));
        cooperativeYearOutputDO.setId(null);
        this.baseMapper.insert(cooperativeYearOutputDO);
    }


    /**
     * 统计联合体现有年限制数据
     * @param cooperativeId
     * @param year
     * @return
     */
    private CooperativeYearOutputDO getCooperativeYearOutput(String cooperativeId, String year){
        CooperativeDO cooperativeDO = cooperativeMapper.selectOne(new QueryWrapper<CooperativeDO>().lambda()
            .eq(CooperativeDO::getCooperativeId,cooperativeId));
        if(cooperativeDO == null){
            throw new TeaException("联合体信息查询失败！！");
        }
        CooperativeYearOutputDO cooperativeYearOutputDO = new CooperativeYearOutputDO();
        BeanUtils.copyProperties(cooperativeDO,cooperativeYearOutputDO);
        cooperativeYearOutputDO.setId(null);
        cooperativeYearOutputDO.setCreateTime(null);
        cooperativeYearOutputDO.setUpdateTime(null);
        cooperativeYearOutputDO.setAmount(new BigDecimal(0));
        cooperativeYearOutputDO.setQuantity(new BigDecimal(0));
        /*List<CooperativeYearOutputDO> list = teaGreenAcquisitionMapper.selectCooperativeYearOutPut(year,cooperativeId);
        if(!CollectionUtils.isEmpty(list)){
            for (CooperativeYearOutputDO yearOutputDO : list) {
                cooperativeYearOutputDO.setAmount(cooperativeYearOutputDO.getAmount().add(yearOutputDO.getAmount()));
                cooperativeYearOutputDO.setQuantity(cooperativeYearOutputDO.getQuantity().add(yearOutputDO.getQuantity()));
            }
        }*/
        List<CooperativeYearOutputDO> list1 = hairyTeaAcquisitionMapper.selectCooperativeYearOutPut(year,cooperativeId);
        if(!CollectionUtils.isEmpty(list1)){
            for (CooperativeYearOutputDO yearOutputDO : list1) {
                cooperativeYearOutputDO.setAmount(cooperativeYearOutputDO.getAmount().add(yearOutputDO.getAmount()));
                cooperativeYearOutputDO.setQuantity(cooperativeYearOutputDO.getQuantity().add(yearOutputDO.getQuantity()));
            }
        }
        cooperativeYearOutputDO.setCurrentYear(year);
        cooperativeYearOutputDO.setAmountWarn(cooperativeDO.getAmountWarn());
        return cooperativeYearOutputDO;
    }

}
