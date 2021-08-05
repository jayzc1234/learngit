package com.zxs.server.service.fuding.impl.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.pojo.fuding.base.CooperativeDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionFarmerDO;
import net.app315.hydra.intelligent.planting.pojo.fuding.teagreen.TeaGreenAcquisitionStatisticsDO;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.base.CooperativeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionCooperativeMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.hairytea.HairyTeaAcquisitionStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionFarmerMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionMapper;
import net.app315.hydra.intelligent.planting.server.mapper.fuding.teagreen.TeaGreenAcquisitionStatisticsMapper;
import net.app315.hydra.intelligent.planting.server.service.fuding.task.TeaTaskService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.ITeaGreenAcquisitionStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.fuding.teagreen.TeaGreenAcquisitionFarmerService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:chengaunyu
 * @Date:2019/11/20
 */
@Service
@Slf4j
public class TeaTaskServiceImpl implements TeaTaskService {
    @Autowired
    private HairyTeaAcquisitionMapper hairyTeaAcquisitionMapper;
    @Autowired
    private TeaGreenAcquisitionMapper teaGreenAcquisitionMapper;
    @Autowired
    private TeaGreenAcquisitionFarmerService iTeaGreenAcquisitionFarmerService;
    @Autowired
    private ITeaGreenAcquisitionStatisticsService iTeaGreenAcquisitionStatisticsService;
    @Autowired
    private HairyTeaAcquisitionStatisticsMapper hairyTeaAcquisitionStatisticsMapper;
    @Autowired
    private HairyTeaAcquisitionCooperativeMapper hairyTeaAcquisitionCooperativeMapper;
    @Autowired
    private TeaGreenAcquisitionFarmerMapper teaGreenAcquisitionFarmerMapper;
    @Autowired
    private TeaGreenAcquisitionStatisticsMapper teaGreenAcquisitionStatisticsMapper;
    @Autowired
    private CooperativeMapper cooperativeMapper;

    /**
     * 茶青统计任务方法
     *
     * @param dateTime yyyy-MM-dd
     * @param orgId    组织id
     */
    @Override
    public void teaGreenTask(String orgId, String dateTime) {

        try {
            realTeaGreenTask(orgId,dateTime);
        }catch (Exception e){
            e.printStackTrace();
            realTeaGreenTask(orgId,dateTime);
        }
    }

    private void  realTeaGreenTask(String orgId, String dateTime){
        teaGreenAcquisitionFarmerMapper.delete(new QueryWrapper<TeaGreenAcquisitionFarmerDO>().lambda()
                .eq(TeaGreenAcquisitionFarmerDO::getOrganizationId, orgId)
                .eq(TeaGreenAcquisitionFarmerDO::getStatisticsTime, dateTime));
        teaGreenAcquisitionStatisticsMapper.delete(new QueryWrapper<TeaGreenAcquisitionStatisticsDO>().lambda()
                .eq(TeaGreenAcquisitionStatisticsDO::getOrganizationId, orgId)
                .eq(TeaGreenAcquisitionStatisticsDO::getStatisticsTime, dateTime));
        log.info("茶青统计任务方法,组织id={},dateTime={}",orgId,dateTime);
        List<TeaGreenAcquisitionStatisticsDO> hairyTeaAcquisitionStatisticsDOS = teaGreenAcquisitionMapper.dayStatistic(orgId, dateTime);
        if (CollectionUtils.isNotEmpty(hairyTeaAcquisitionStatisticsDOS)) {
            //部门id 联合体的关系
            Map<String, CooperativeDO> cooperativeDOMap = new HashMap<>();
            for (TeaGreenAcquisitionStatisticsDO teaGreenAcquisitionStatisticsDO : hairyTeaAcquisitionStatisticsDOS) {
                if (teaGreenAcquisitionStatisticsDO.getQuantity() == null) {
                    teaGreenAcquisitionStatisticsDO.setQuantity(new BigDecimal(0));
                }
                if (teaGreenAcquisitionStatisticsDO.getTradingVolume() == null) {
                    teaGreenAcquisitionStatisticsDO.setTradingVolume(0);
                }
                if (teaGreenAcquisitionStatisticsDO.getAmount() == null) {
                    teaGreenAcquisitionStatisticsDO.setAmount(new BigDecimal(0));
                }
                if (teaGreenAcquisitionStatisticsDO.getQuantity().doubleValue() > 0) {
                    teaGreenAcquisitionStatisticsDO.setAveragePrice(teaGreenAcquisitionStatisticsDO.getAmount()
                            .divide(teaGreenAcquisitionStatisticsDO.getQuantity(), 2, BigDecimal.ROUND_FLOOR));
                }
                //设置收购人对应的部门对应的联合体信息
                if(StringUtils.isEmpty(teaGreenAcquisitionStatisticsDO.getCooperativeId())){
                    CooperativeDO cooperativeDO = cooperativeDOMap.get(teaGreenAcquisitionStatisticsDO.getDepartmentId());
                    if (cooperativeDO == null) {
                        // 根据部门id获取联合体信息
                        List<CooperativeDO> cooperativeDOList = cooperativeMapper.selectList(new QueryWrapper<CooperativeDO>().lambda()
                                .eq(CooperativeDO::getDepartmentId, teaGreenAcquisitionStatisticsDO.getDepartmentId()));
                        if (CollectionUtils.isNotEmpty(cooperativeDOList) && cooperativeDOList.size() > 0) {
                            cooperativeDO = cooperativeDOList.get(0);
                            cooperativeDOMap.put(teaGreenAcquisitionStatisticsDO.getDepartmentId(), cooperativeDO);
                            teaGreenAcquisitionStatisticsDO.setCooperativeId(cooperativeDO.getCooperativeId());
                            teaGreenAcquisitionStatisticsDO.setCooperativeName(cooperativeDO.getCooperativeName());
                        }
                    } else {
                        teaGreenAcquisitionStatisticsDO.setCooperativeId(cooperativeDO.getCooperativeId());
                        teaGreenAcquisitionStatisticsDO.setCooperativeName(cooperativeDO.getCooperativeName());
                    }
                }
            }
            iTeaGreenAcquisitionStatisticsService.saveBatch(hairyTeaAcquisitionStatisticsDOS);
            List<TeaGreenAcquisitionFarmerDO> teaGreenAcquisitionFarmerDOS = teaGreenAcquisitionMapper.dayTeaFarmers(orgId, dateTime);
            if (CollectionUtils.isNotEmpty(teaGreenAcquisitionFarmerDOS)) {
                //添加当天参与采购的茶农
                iTeaGreenAcquisitionFarmerService.saveBatch(teaGreenAcquisitionFarmerDOS);
            }
        }
    }

}
