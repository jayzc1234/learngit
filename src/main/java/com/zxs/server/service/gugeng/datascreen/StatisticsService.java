package com.zxs.server.service.gugeng.datascreen;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jgw.supercodeplatform.common.AbstractPageService;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import net.app315.hydra.intelligent.planting.common.gugeng.model.Employee;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.DateUtils;
import net.app315.hydra.intelligent.planting.dto.gugeng.datascreen.StatisticsRequestDto;
import net.app315.hydra.intelligent.planting.dto.gugeng.producemanage.SearchHarvestPlanRequestDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageStatisticsRecord;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageStatisticsRecordEx;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.HarvestPlanMonthVo;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.datascreen.ProductionManageStatisticsRecordMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.producemanage.ProductionManageHarvestPlanService;
import net.app315.hydra.intelligent.planting.vo.gugeng.producemanage.SearchHarvestPlanResponseVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private ProductionManageStatisticsRecordMapper baseMapper;

    @Autowired
    private ProductionManageHarvestPlanService harvestPlanService;

    @Autowired
    private SaleScreenService saleScreenService;

    @Autowired
    private CodeStatisticsScreenService codeScreenService;

    @Autowired
    private FarmingService farmingService;

    public void save(StatisticsRequestDto requestDTO) throws Exception {
        ProductionManageStatisticsRecord record= getByPageId(requestDTO.getPageId());
        Employee employee = commonUtil.getEmployee();
        if(record==null){
            record=new ProductionManageStatisticsRecord();
            BeanUtils.copyProperties(requestDTO, record);

            record.setCreateUserId(employee.getEmployeeId());
            record.setCreateUserName(employee.getName());
            record.setCreateDate(LocalDateTime.now());

            record.setSysId(commonUtil.getSysId());
            record.setOrganizationId(commonUtil.getOrganizationId());

            baseMapper.insert(record);
        } else {
            record.setUpdateUserId(employee.getEmployeeId());
            record.setUpdateUserName(employee.getName());
            record.setUpdateDate(LocalDateTime.now());

            if(StringUtils.isNotEmpty(requestDTO.getPageContent())){
                record.setPageContent(requestDTO.getPageContent());
            }
            if(requestDTO.getDataType()!=null){
                record.setDataType(requestDTO.getDataType());
                if(requestDTO.getDataType()==1){
                    record.setPageContent(getDataJson(requestDTO.getPageId()));
                }
            }
            if(StringUtils.isNotEmpty(requestDTO.getTitle())){
                record.setTitle(requestDTO.getTitle());
            }

            baseMapper.updateById(record);
        }
    }

    public ProductionManageStatisticsRecord getByPageId(String pageId) throws SuperCodeException {
        QueryWrapper<ProductionManageStatisticsRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageStatisticsRecord.COL_PAGE_ID, pageId);
        queryWrapper.eq(ProductionManageStatisticsRecord.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq(ProductionManageStatisticsRecord.COL_SYS_ID, commonUtil.getSysId());
        ProductionManageStatisticsRecord harvestPlan = baseMapper.selectOne(queryWrapper);
        return harvestPlan;
    }

    public ProductionManageStatisticsRecordEx selectPageContent(String pageId) throws Exception {
        ProductionManageStatisticsRecord record= getByPageId(pageId);
        if(record==null){
            return null;
        }

        ProductionManageStatisticsRecordEx recordex= new ProductionManageStatisticsRecordEx();
        BeanUtils.copyProperties(record, recordex);

        if((record.getDataType()!=null && record.getDataType()==1)
                || true){
            recordex.setRealContent(getDataJson(pageId));
        }
        return recordex;
    }

    private String getDataJson(String pageId)  throws Exception {
        String jsonData=null;
        switch (pageId){
            case "d1":
                jsonData= JSONObject.toJSONString(farmingService.selectData());
                break;
            case "d3":
                jsonData= JSONObject.toJSONString(saleScreenService.selectData());
                break;
            case "d5":
                jsonData=JSONObject.toJSONString( codeScreenService.selectData() );
                break;
        }
        return jsonData;
    }

    public List<HarvestPlanMonthVo> selectHarvestPlanList() throws Exception{
        SearchHarvestPlanRequestDTO requestDTO=new SearchHarvestPlanRequestDTO();
        requestDTO.setCurrent(1);
        requestDTO.setPageSize(200);
        AbstractPageService.PageResults<List<SearchHarvestPlanResponseVO>> pageResults= harvestPlanService.list(requestDTO);
        List<SearchHarvestPlanResponseVO> harvestPlanResponseVOList= pageResults.getList();
        String startDate= DateUtils.dateFormat( new Date(), "yyyy-MM")+"-01";
        Date startDateVal= DateUtils.parse(startDate, "yyyy-MM-dd");
        harvestPlanResponseVOList= harvestPlanResponseVOList.stream()
                .filter(e-> DateUtils.parse( e.getHarvestDate(), "yyyy-MM-dd").getTime()>=startDateVal.getTime())
                .collect(Collectors.toList());

        List<HarvestPlanMonthVo> harvestPlanMonthVoList=new ArrayList<>();

        for(SearchHarvestPlanResponseVO harvestPlanResponseVO: harvestPlanResponseVOList){
            String id= harvestPlanResponseVO.getGreenhouseName()+" "+ harvestPlanResponseVO.getHarvestDate().substring(0,7)+"月";

            HarvestPlanMonthVo harvestPlanMonthVo= harvestPlanMonthVoList.stream().filter(e->e.getId().equals(id)).findFirst().orElse(null);
            if(harvestPlanMonthVo==null){
                harvestPlanMonthVo=new HarvestPlanMonthVo();
                harvestPlanMonthVo.setId(id);
                harvestPlanMonthVo.setProductionForecast(harvestPlanResponseVO.getProductionForecast());
                harvestPlanMonthVoList.add(harvestPlanMonthVo);
            } else {
                BigDecimal productionForecast = harvestPlanResponseVO.getProductionForecast().add( harvestPlanMonthVo.getProductionForecast());
                harvestPlanMonthVo.setProductionForecast(productionForecast);
            }
        }
        for(HarvestPlanMonthVo harvestPlanMonthVo: harvestPlanMonthVoList){
            harvestPlanMonthVo.setId( harvestPlanMonthVo.getId().replace("2019-","").replace("2020-",""));
        }
        Collections.sort(harvestPlanMonthVoList, (a, b) -> {
            HarvestPlanMonthVo m1 =  a;
            HarvestPlanMonthVo m2 =  b;
            int i = m1.getProductionForecast().compareTo(m2.getProductionForecast());
            if (i > 0){
                return -1;
            }
            if (i == 0){
                return 0;
            }
            if (i < 0){
                return 1;
            }
            return i;
        });

        if(harvestPlanMonthVoList.size()>15)
        {
            harvestPlanMonthVoList=harvestPlanMonthVoList.subList(0,15);
        }
        return harvestPlanMonthVoList;
    }

    public List<ProductionManageStatisticsRecord> selectList() {
        QueryWrapper<ProductionManageStatisticsRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ProductionManageStatisticsRecord.COL_ORGANIZATION_ID, commonUtil.getOrganizationId());
        queryWrapper.eq(ProductionManageStatisticsRecord.COL_SYS_ID, commonUtil.getSysId());
        return baseMapper.selectList(queryWrapper);
    }
}
