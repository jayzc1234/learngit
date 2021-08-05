package com.zxs.server.service.fuding.impl.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.TeaFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.behavioral.DailyInspectionDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionBaseDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionCooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.hairytea.HairyTeaAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionBaseDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.TeaFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.behavioral.DailyInspectionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionBaseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionCooperativeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionBaseMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.common.ISynchronizeDataService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ISynchronizeDataServiceImpl implements ISynchronizeDataService {

    @Autowired
    private DailyInspectionMapper dailyInspectionMapper;
    @Autowired
    private HairyTeaAcquisitionBaseMapper hairyTeaAcquisitionBaseMapper;
    @Autowired
    private HairyTeaAcquisitionCooperativeMapper hairyTeaAcquisitionCooperativeMapper;
    @Autowired
    private HairyTeaAcquisitionMapper hairyTeaAcquisitionMapper;
    @Autowired
    private HairyTeaAcquisitionStatisticsMapper hairyTeaAcquisitionStatisticsMapper;
    @Autowired
    private TeaGreenAcquisitionBaseMapper teaGreenAcquisitionBaseMapper;
    @Autowired
    private TeaGreenAcquisitionMapper teaGreenAcquisitionMapper;
    @Autowired
    private TeaGreenAcquisitionFarmerMapper teaGreenAcquisitionFarmerMapper;
    @Autowired
    private TeaGreenAcquisitionStatisticsMapper teaGreenAcquisitionStatisticsMapper;
    @Autowired
    private TeaFarmerMapper teaFarmerMapper;
    /**
     * 同步联合体数据
     *
     * @param cooperativeDO
     */
    @Override
    public void synchronizeCooperativeInfo(CooperativeDO cooperativeDO) {
        //修改巡检记录
        DailyInspectionDO dailyInspectionDO = new DailyInspectionDO();
        dailyInspectionDO.setCooperativeName(cooperativeDO.getCooperativeName());
        dailyInspectionMapper.update(dailyInspectionDO, new QueryWrapper<DailyInspectionDO>().lambda()
                .eq(DailyInspectionDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        //修改毛茶收购记录
        HairyTeaAcquisitionBaseDO hairyTeaAcquisitionBaseDO = new HairyTeaAcquisitionBaseDO();
        hairyTeaAcquisitionBaseDO.setCooperativeName(cooperativeDO.getCooperativeName());
        hairyTeaAcquisitionBaseMapper.update(hairyTeaAcquisitionBaseDO, new QueryWrapper<HairyTeaAcquisitionBaseDO>().lambda()
                .eq(HairyTeaAcquisitionBaseDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        HairyTeaAcquisitionCooperativeDO hairyTeaAcquisitionCooperativeDO = new HairyTeaAcquisitionCooperativeDO();
        hairyTeaAcquisitionCooperativeDO.setCooperativeName(cooperativeDO.getCooperativeName());
        hairyTeaAcquisitionCooperativeMapper.update(hairyTeaAcquisitionCooperativeDO, new QueryWrapper<HairyTeaAcquisitionCooperativeDO>().lambda()
                .eq(HairyTeaAcquisitionCooperativeDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        HairyTeaAcquisitionDO hairyTeaAcquisitionDO = new HairyTeaAcquisitionDO();
        hairyTeaAcquisitionDO.setCooperativeName(cooperativeDO.getCooperativeName());
        hairyTeaAcquisitionMapper.update(hairyTeaAcquisitionDO, new QueryWrapper<HairyTeaAcquisitionDO>().lambda()
                .eq(HairyTeaAcquisitionDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        HairyTeaAcquisitionStatisticsDO hairyTeaAcquisitionStatisticsDO = new HairyTeaAcquisitionStatisticsDO();
        hairyTeaAcquisitionStatisticsDO.setCooperativeName(cooperativeDO.getCooperativeName());
        hairyTeaAcquisitionStatisticsMapper.update(hairyTeaAcquisitionStatisticsDO, new QueryWrapper<HairyTeaAcquisitionStatisticsDO>().lambda()
                .eq(HairyTeaAcquisitionStatisticsDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        //茶青收购记录修改
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = new TeaGreenAcquisitionBaseDO();
        teaGreenAcquisitionBaseDO.setCooperativeName(cooperativeDO.getCooperativeName());
        teaGreenAcquisitionBaseMapper.update(teaGreenAcquisitionBaseDO, new QueryWrapper<TeaGreenAcquisitionBaseDO>().lambda()
                .eq(TeaGreenAcquisitionBaseDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        TeaGreenAcquisitionDO teaGreenAcquisitionDO = new TeaGreenAcquisitionDO();
        teaGreenAcquisitionDO.setCooperativeName(cooperativeDO.getCooperativeName());
        teaGreenAcquisitionMapper.update(teaGreenAcquisitionDO, new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                .eq(TeaGreenAcquisitionDO::getCooperativeId, cooperativeDO.getCooperativeId()));

        TeaGreenAcquisitionStatisticsDO teaGreenAcquisitionStatisticsDO = new TeaGreenAcquisitionStatisticsDO();
        teaGreenAcquisitionStatisticsDO.setCooperativeName(cooperativeDO.getCooperativeName());
        teaGreenAcquisitionStatisticsMapper.update(teaGreenAcquisitionStatisticsDO, new QueryWrapper<TeaGreenAcquisitionStatisticsDO>().lambda()
                .eq(TeaGreenAcquisitionStatisticsDO::getCooperativeId, cooperativeDO.getCooperativeId()));


        TeaFarmerDO teaFarmerDO = new TeaFarmerDO();
        teaFarmerDO.setCooperativeName(cooperativeDO.getCooperativeName());
        teaFarmerMapper.update(teaFarmerDO,new QueryWrapper<TeaFarmerDO>().lambda().eq(TeaFarmerDO::getCooperativeId,cooperativeDO.getCooperativeId()));


    }

    /**
     * 同步茶农数据
     *
     * @param teaFarmerDO
     */
    @Override
    public void synchronizeTeaFarmerInfo(TeaFarmerDO teaFarmerDO) {
        if(StringUtils.isEmpty(teaFarmerDO.getFarmerId())){
            return;
        }
        //修改巡检记录
        DailyInspectionDO dailyInspectionDO = new DailyInspectionDO();
        dailyInspectionDO.setFarmerName(teaFarmerDO.getFarmerName());
        dailyInspectionMapper.update(dailyInspectionDO, new QueryWrapper<DailyInspectionDO>().lambda()
                .eq(DailyInspectionDO::getFarmerId, teaFarmerDO.getFarmerId()));

        //茶青收购记录修改
        TeaGreenAcquisitionBaseDO teaGreenAcquisitionBaseDO = new TeaGreenAcquisitionBaseDO();
        teaGreenAcquisitionBaseDO.setFarmerName(teaFarmerDO.getFarmerName());
        teaGreenAcquisitionBaseMapper.update(teaGreenAcquisitionBaseDO, new QueryWrapper<TeaGreenAcquisitionBaseDO>().lambda()
                .eq(TeaGreenAcquisitionBaseDO::getFarmerId, teaFarmerDO.getFarmerId()));

        TeaGreenAcquisitionFarmerDO teaGreenAcquisitionFarmerDO = new TeaGreenAcquisitionFarmerDO();
        teaGreenAcquisitionFarmerDO.setFarmerName(teaFarmerDO.getFarmerName());
        teaGreenAcquisitionFarmerMapper.update(teaGreenAcquisitionFarmerDO, new QueryWrapper<TeaGreenAcquisitionFarmerDO>().lambda()
                .eq(TeaGreenAcquisitionFarmerDO::getFarmerId, teaFarmerDO.getFarmerId()));

        TeaGreenAcquisitionDO teaGreenAcquisitionDO = new TeaGreenAcquisitionDO();
        teaGreenAcquisitionDO.setFarmerName(teaFarmerDO.getFarmerName());
        teaGreenAcquisitionFarmerDO.setFarmerName(teaFarmerDO.getFarmerName());
        teaGreenAcquisitionMapper.update(teaGreenAcquisitionDO, new QueryWrapper<TeaGreenAcquisitionDO>().lambda()
                .eq(TeaGreenAcquisitionDO::getFarmerId, teaFarmerDO.getFarmerId()));

    }
}
