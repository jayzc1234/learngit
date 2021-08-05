package com.zxs.server.service.gugeng.datascreen;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.NumberUtil;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.YieldComparisonRequestDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.trace.GreenhouseAreaDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageLastestPickingBatch;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageMonthPlantingInfo;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageSalesData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageSalesOrder;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.datascreen.ProductionManageSaleScreenMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageStatisticsProductionYieldDataMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProductionManageStatisticsProductionYieldDataService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.storagemanage.ProductionManageWeightService;
import net.app315.hydra.intelligent.planting.vo.gugeng.statistics.SearchNewestYCDataVO;
import net.app315.hydra.intelligent.planting.vo.gugeng.storagemanage.WeighingVO;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FarmingService extends CommonUtil {

    @Value("${rest.trace.url}")
    public String traceDomain;

    @Autowired
    public SuperCodeRequests codeRequests;

    @Autowired
    private ProductionManageSaleScreenMapper saleScreenMapper;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ProductionManageStatisticsProductionYieldDataMapper productionYieldDataMapper;

    @Autowired
    private ProductionManageWeightService weightService;

    @Autowired
    private ProductionManageStatisticsProductionYieldDataService yieldDataService;

    public Map<String,Object> selectData() throws Exception {
        String today= DateFormatUtils.format(new Date(), LocalDateTimeUtil.DATE_PATTERN);
        String month = DateFormatUtils.format(new Date(), LocalDateTimeUtil.YEAR_AND_MONTH)+"-01";
        LocalDate weekday=LocalDate.now().plusDays(-6);
        String weekDayStr= weekday.toString();
        String organizationId = getOrganizationId();
        String sysId = getSysId();
        List<ProductionManageSalesOrder> weekSaleList=saleScreenMapper.selectDayOrder(weekDayStr,organizationId, sysId);
        for(int i=0; i<7; i++){
            String day= weekday.plusDays(i).toString().substring(5);
            if(!weekSaleList.stream().anyMatch(e->e.getName().equals(day))){
                weekSaleList.add(i, new ProductionManageSalesOrder(day, 0d, null));
            }
        }

        Map<String,Object> resultMap=new HashMap<String, Object>();
        resultMap.put("salesDataRealTime", saleScreenMapper.selectLastestOrder(getOrganizationId(), getSysId()));
        resultMap.put("regionalRanking", weekSaleList);
        resultMap.put("lastestPickingBatch", selectPickingBatch());
        resultMap.put("harvestPlanList", statisticsService.selectHarvestPlanList());
        resultMap.put("monthPlantingInfoList", getWeightingInfoStatistic());

        List<ProductionManageSalesData> salesDataList= saleScreenMapper.selectSalesData(today, month,getOrganizationId(), getSysId());
        Double  outputValue=salesDataList.get(2).getLumpSum(); //总产值
        resultMap.put("salesData", salesDataList);
        resultMap.put("outputValue",  outputValue==null ? 0d: NumberUtil.retainTwoDecimal(outputValue.toString()));

        List<GreenhouseAreaDTO> areaDTOList= listGreenhouseAreaMsg(getSuperToken());
        Double totalArea=0d;
        for(GreenhouseAreaDTO areaDTO:areaDTOList){
            if(areaDTO.getMassArea()==null){
                continue;
            }

            if(areaDTO.getAreaUnit().equals("016002")){
                totalArea=totalArea+areaDTO.getMassArea().doubleValue();
            } else if(areaDTO.getAreaUnit().equals("016001")){
                totalArea=totalArea+ areaDTO.getMassArea().doubleValue()/666.67d;
            } else if(areaDTO.getAreaUnit().equals("016003")){
                totalArea=totalArea+ areaDTO.getMassArea().doubleValue()*15;
            }
        }
        resultMap.put("productionScale",  NumberUtil.retainTwoDecimal(totalArea.toString())); //生产规模

        return resultMap;
    }

    List<ProductionManageLastestPickingBatch> selectPickingBatch(){
        YieldComparisonRequestDTO requestDTO=new YieldComparisonRequestDTO();
        List<SearchNewestYCDataVO> yieldDataList= yieldDataService.listLastestYieldComparisonData();
        List<ProductionManageLastestPickingBatch> batchList= yieldDataList.stream().map(e->new ProductionManageLastestPickingBatch(e.getPlantBatchName(), e.getTotalProduceWeight().doubleValue(),
                e.getCommodityRate().doubleValue()*100)).collect(Collectors.toList());
        return batchList;
    }

    public List<ProductionManageLastestPickingBatch> getLastestPickingBatchList() throws SuperCodeException {
        String url = traceDomain + "/trace/hainanrunda/massifinfostatistic/getLastestPickingBatchList";
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("super-token", getSuperToken() );
        Map<String, Object> params = new HashMap<>(2);

        try {
            String result = codeRequests.getAndGetResultBySpring(url, params, headerMap, String.class, true);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("state") == 200) {
                JSONArray results = jsonObject.getJSONArray("results");
                List<ProductionManageLastestPickingBatch> responseDTO = results.toJavaList(ProductionManageLastestPickingBatch.class);
                return responseDTO;
            } else {
                CustomAssert.throwException("");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            CustomAssert.throwException("");
        }

        return null;
    }

    public List<ProductionManageMonthPlantingInfo> getWeightingInfoStatistic() throws SuperCodeException {
        List<WeighingVO> weighingVOList= weightService.listByHalfYearMsg();
        List<ProductionManageMonthPlantingInfo> monthPlantingInfos= JSONObject.parseArray(JSONObject.toJSONString(weighingVOList), ProductionManageMonthPlantingInfo.class);
        return monthPlantingInfos;
    }

    public List<ProductionManageMonthPlantingInfo> getPlantingInfoStatistic() throws SuperCodeException {
        String url = traceDomain + "/trace/hainanrunda/massifinfostatistic/getPlantingInfoStatistic";
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("super-token", getSuperToken() );
        Map<String, Object> params = new HashMap<>(2);

        try {
            String result = codeRequests.getAndGetResultBySpring(url, params, headerMap, String.class, true);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("state") == 200) {
                JSONArray results = jsonObject.getJSONObject("results").getJSONArray("dataList");
                List<ProductionManageMonthPlantingInfo> responseDTO = results.toJavaList(ProductionManageMonthPlantingInfo.class);
                return responseDTO;
            } else {
                CustomAssert.throwException("");
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            CustomAssert.throwException("");
        }

        return null;
    }


}
