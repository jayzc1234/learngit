package com.zxs.server.service.fuding.impl.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jgw.supercodeplatform.common.AbstractPageService;
import net.app315.hydra.intelligent.planting.bo.fuding.TeaFarmerYearOutputBO;
import net.app315.hydra.intelligent.planting.enums.fuding.YesOrNoEnum;
import net.app315.hydra.intelligent.planting.exception.gugeng.TeaException;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerYearOutputDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.TeaFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.TeaFarmerYearOutputMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.base.ITeaFarmerYearOutputService;
import net.app315.hydra.intelligent.planting.utils.fuding.copy.CopyUtil;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerYearOutputModel;
import net.app315.hydra.intelligent.planting.vo.fuding.base.TeaFarmerYearOutputVO;
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
 * 茶农社年收购记录表 服务实现类
 * </p>
 *
 * @author 
 * @since 2020-02-17
 */
@Service
public class TeaFarmerYearOutputServiceImpl extends ServiceImpl<TeaFarmerYearOutputMapper, TeaFarmerYearOutputDO> implements ITeaFarmerYearOutputService {

    @Autowired
    private TeaGreenAcquisitionMapper teaGreenAcquisitionMapper;
    @Autowired
    private TeaFarmerMapper teaFarmerMapper;
    /**
     添加 茶农社年收购记录表
     */
    @Override
    public String addTeaFarmerYearOutput(TeaFarmerYearOutputBO model){
        TeaFarmerYearOutputDO entity = new TeaFarmerYearOutputDO();
        BeanUtils.copyProperties(model,entity);
        save(entity);
        return null;
    }

    /**
          修改 茶农社年收购记录表
     */
     @Override
     public void updateTeaFarmerYearOutput(TeaFarmerYearOutputBO model){
      TeaFarmerYearOutputDO entity = new TeaFarmerYearOutputDO();
      BeanUtils.copyProperties(model,entity);
      updateById(entity);
     }

    @Override
    public void updateOrAddTeaFarmerYearOutputWarnValue(String cooperativeId, String farmerId, BigDecimal warnValue) {
        if(StringUtils.isBlank(cooperativeId) || StringUtils.isBlank(farmerId)){
            return ;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        TeaFarmerYearOutputDO farmerYearOutputDO = getOne(new QueryWrapper<TeaFarmerYearOutputDO>().lambda()
                .eq(TeaFarmerYearOutputDO::getFarmerId, farmerId).eq(TeaFarmerYearOutputDO::getCurrentYear, currentYear));
        if(farmerYearOutputDO == null){
            TeaFarmerYearOutputDO teaFarmerYearOutput = getTeaFarmerYearOutput(currentYear,farmerId);
            this.baseMapper.insert(teaFarmerYearOutput);
        }else {
            farmerYearOutputDO.setAmountWarn(warnValue);
            updateById(farmerYearOutputDO);
        }
    }

    /**
     修改 茶农当年年产收购限量
     */
    @Override
    public void updateTeaFarmerYearOutputWarnValue(String cooperativeId, String farmerId, BigDecimal warnValue) {
        if(StringUtils.isBlank(cooperativeId) || StringUtils.isBlank(farmerId)){
            return ;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        TeaFarmerYearOutputDO farmerYearOutputDO = getOne(new QueryWrapper<TeaFarmerYearOutputDO>().lambda()
                .eq(TeaFarmerYearOutputDO::getFarmerId, farmerId).eq(TeaFarmerYearOutputDO::getCurrentYear, currentYear));
        if(farmerYearOutputDO == null){
            return;
        }
        farmerYearOutputDO.setAmountWarn(warnValue);
        updateById(farmerYearOutputDO);
    }

    /**
           获取 茶农社年收购记录表 详情
     */
    @Override
    public TeaFarmerYearOutputBO getTeaFarmerYearOutput(String id){
         TeaFarmerYearOutputDO entity = getOne(new QueryWrapper<TeaFarmerYearOutputDO>().eq("id",id));
         if(entity!=null){
            TeaFarmerYearOutputBO result = new TeaFarmerYearOutputBO();
            BeanUtils.copyProperties(entity,result);
            return result;
         }
        return null;
     }

    @Override
    public TeaFarmerYearOutputBO getTeaFarmerYearOutputByFarmerId(String farmerId, String cooperativeId) {
        if(StringUtils.isBlank(farmerId)){
            return null;
        }
        String currentYear = String.valueOf(LocalDate.now().getYear());
        TeaFarmerYearOutputDO farmerYearOutputDO = getOne(new QueryWrapper<TeaFarmerYearOutputDO>().lambda()
                .eq(TeaFarmerYearOutputDO::getFarmerId, farmerId)
                .eq(TeaFarmerYearOutputDO::getCurrentYear, currentYear));
        return CopyUtil.copy(farmerYearOutputDO,new TeaFarmerYearOutputBO());
    }

    /**
     * 获取茶农社年收购记录表分页列表
     * @return
     */
    @Override
    public AbstractPageService.PageResults<List<TeaFarmerYearOutputVO>> getTeaFarmerYearOutputList(TeaFarmerYearOutputModel model){
        return selectPage(model,getBaseMapper(), TeaFarmerYearOutputVO.class);
    }

    /**
     * 修改茶农年收购数据  没有原数据就新建，有就修改
     *  * YES 标识添加  NO 标识减少
     * @param teaFarmerYearOutputDO
     */
    @Override
    public void updateCurrentYearOutPut(TeaFarmerYearOutputDO teaFarmerYearOutputDO, YesOrNoEnum yesOrNoEnum){
        if(null == teaFarmerYearOutputDO.getAmount()){
            throw new TeaException("收购金额不能为空！！！");
        }
        if(null == teaFarmerYearOutputDO.getQuantity()){
            throw new TeaException("收购数量不能为空！！！");
        }
        if(StringUtils.isEmpty(teaFarmerYearOutputDO.getFarmerId())){
            throw new TeaException("茶农不能为空！！！");
        }
        if(StringUtils.isEmpty(teaFarmerYearOutputDO.getCurrentYear())){
            teaFarmerYearOutputDO.setCurrentYear(String.valueOf(LocalDate.now().getYear()));
        }
        TeaFarmerYearOutputDO yearOutputDO = this.baseMapper.selectOne(new QueryWrapper<TeaFarmerYearOutputDO>().lambda()
                .eq(TeaFarmerYearOutputDO::getFarmerId,teaFarmerYearOutputDO.getFarmerId())
                .eq(TeaFarmerYearOutputDO::getCurrentYear, teaFarmerYearOutputDO.getCurrentYear()));
        if(YesOrNoEnum.YES.equals(yesOrNoEnum)){
            //不存在当年收购数据
            if(yearOutputDO == null){
                TeaFarmerYearOutputDO teaFarmerYearOutput = getTeaFarmerYearOutput(teaFarmerYearOutputDO.getCurrentYear(),teaFarmerYearOutputDO.getFarmerId());
              /*  teaFarmerYearOutput.setAmount(teaFarmerYearOutput.getAmount().add(teaFarmerYearOutputDO.getAmount()));
                teaFarmerYearOutput.setQuantity(teaFarmerYearOutput.getQuantity().add(teaFarmerYearOutputDO.getQuantity()));*/
                this.baseMapper.insert(teaFarmerYearOutput);
            }else{
                // 修改年收购总量和总金额
                this.baseMapper.addCurrentOutPut(yearOutputDO.getId(),
                        teaFarmerYearOutputDO.getAmount(),teaFarmerYearOutputDO.getQuantity());
            }
        }else {
            // 减少
            if(yearOutputDO == null){
                return;
            }
            this.baseMapper.lessCurrentOutPut(yearOutputDO.getId(),
                    teaFarmerYearOutputDO.getAmount(),teaFarmerYearOutputDO.getQuantity());
        }

    }

    /**
     * 统计茶农当天年份采购限制
     * @param year
     * @param farmerId
     * @return
     */
    private TeaFarmerYearOutputDO getTeaFarmerYearOutput(String year, String farmerId){
        TeaFarmerDO teaFarmerDO = teaFarmerMapper.selectOne(new QueryWrapper<TeaFarmerDO>().lambda()
            .eq(TeaFarmerDO::getFarmerId,farmerId));
        if(teaFarmerDO == null){
            throw new TeaException("茶农信息查询失败！！！");
        }
        TeaFarmerYearOutputDO teaFarmerYearOutput = new TeaFarmerYearOutputDO();
        BeanUtils.copyProperties(teaFarmerDO,teaFarmerYearOutput);
        teaFarmerYearOutput.setId(null);
        teaFarmerYearOutput.setCreateTime(null);
        teaFarmerYearOutput.setUpdateTime(null);
        teaFarmerYearOutput.setAmount(new BigDecimal(0));
        teaFarmerYearOutput.setQuantity(new BigDecimal(0));
        teaFarmerYearOutput.setCurrentYear(year);
        List<TeaFarmerYearOutputDO> list = teaGreenAcquisitionMapper.selectFarmerYearOutPut(year,farmerId);
        if(!CollectionUtils.isEmpty(list)){
            for (TeaFarmerYearOutputDO teaFarmerYearOutputDO : list) {
                teaFarmerYearOutput.setAmount(teaFarmerYearOutput.getAmount().add(teaFarmerYearOutputDO.getAmount()));
                teaFarmerYearOutput.setQuantity(teaFarmerYearOutput.getQuantity().add(teaFarmerYearOutputDO.getQuantity()));
            }
        }
        teaFarmerYearOutput.setAmountWarn(teaFarmerDO.getAmountWarn());
        return teaFarmerYearOutput;
    }

    /**
     * 添加当年的年产初始记录
     */
    @Override
    public void addCurrentYearOutPut(TeaFarmerDO teaFarmerDO) {
        String currentYear = String.valueOf(LocalDate.now().getYear());
        TeaFarmerYearOutputDO teaFarmerYearOutputDO = new TeaFarmerYearOutputDO();
        CopyUtil.copy(teaFarmerDO,teaFarmerYearOutputDO);
        teaFarmerYearOutputDO.setQuantity(new BigDecimal(0));
        teaFarmerYearOutputDO.setAmount(new BigDecimal(0));
        teaFarmerYearOutputDO.setCurrentYear(currentYear);
        teaFarmerYearOutputDO.setId(null);
        this.baseMapper.insert(teaFarmerYearOutputDO);
    }

}
