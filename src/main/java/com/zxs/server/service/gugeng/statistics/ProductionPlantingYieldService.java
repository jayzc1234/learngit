package com.zxs.server.service.gugeng.statistics;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.pojo.anjiwhitetea.LaborManage;
import net.app315.hydra.intelligent.planting.pojo.common.Dictionary;
import net.app315.hydra.intelligent.planting.server.facade.HarvestYieldClient;
import net.app315.hydra.intelligent.planting.server.mapper.anjiwhitetea.LaborManageMapper;
import net.app315.hydra.intelligent.planting.server.mapper.common.DictionaryMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageWeightService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductPlantingYieldStatisticVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.ProductPlantingYieldVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.WeighingVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionPlantingYieldService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private HarvestYieldClient harvestYieldClient;

    @Autowired
    private DictionaryMapper dictionaryMapper;

    @Autowired
    private ProductionManageWeightService service;

    @Autowired
    private LaborManageMapper laborManageMapper;

    public ProductPlantingYieldStatisticVO list(){
        QueryWrapper<Dictionary> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(Dictionary.COL_ORGANIZATION_ID,commonUtil.getOrganizationId());
        queryWrapper.eq(Dictionary.COL_TYPE,2);
        queryWrapper.eq(Dictionary.COL_ENABLE,1);
        List<Dictionary> dictionaryList= dictionaryMapper.selectList(queryWrapper);
        List<String> yieldTypes= dictionaryList.stream().map(e->e.getName()).collect(Collectors.toList());

        ProductPlantingYieldStatisticVO statisticVO=harvestYieldClient.getPlantingInfoStatistic().getResults();
        List<ProductPlantingYieldVO> harvestYield=null;
        List<WeighingVO> weighingVOS=null;
        List<ProductPlantingYieldVO> teaHarvestYield=null;

        if(yieldTypes.contains("采收操作")){
            harvestYield=statisticVO.getDataList();
        }
        if(yieldTypes.contains("产品称重")){
            weighingVOS= service.listByHalfYearMsg();
        }
        if(yieldTypes.contains("采茶工用工")){
            String startDay= LocalDate.now().plusMonths(-5).format(DateTimeFormatter.ofPattern(LocalDateTimeUtil.YEAR_AND_MONTH))+"-01 00:00:00";
            QueryWrapper<LaborManage> queryWrapper1= commonUtil.queryTemplate(LaborManage.class);
            queryWrapper1.ge(LaborManage.COL_PICKING_TEA_TIME, startDay);
            teaHarvestYield = laborManageMapper.getListByMonth(queryWrapper1);
        }

        List<ProductPlantingYieldVO> totalYieldList = new ArrayList<>();
        List<String> months = LocalDateTimeUtil.listHalfYearAndMonth();
        for(String month:months){
            ProductPlantingYieldVO yieldVO=new ProductPlantingYieldVO();
            yieldVO.setMonth(month);
            Float harvestQuantity=0f;
            if(harvestYield!=null) {
                ProductPlantingYieldVO vo= harvestYield.stream().filter(e->e.getMonth().equals(month)).findFirst().orElse(null);
                if(vo!=null){
                    harvestQuantity+=vo.getHarvestQuantity();
                }
            }
            if(weighingVOS!=null){
                WeighingVO vo= weighingVOS.stream().filter(e->e.getMonth().equals(month)).findFirst().orElse(null);
                if(vo!=null){
                    harvestQuantity+=vo.getHarvestQuantity().floatValue();
                }
            }
            if(teaHarvestYield!=null){
                ProductPlantingYieldVO vo = teaHarvestYield.stream().filter(e->e.getMonth().equals(month)).findFirst().orElse(null);
                if(vo!=null){
                    harvestQuantity+=vo.getHarvestQuantity();
                }
            }
            yieldVO.setHarvestQuantity(harvestQuantity);
            totalYieldList.add(yieldVO);
        }
        statisticVO.setDataList(totalYieldList);

        return statisticVO;
    }
}
