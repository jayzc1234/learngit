package com.zxs.server.service.gugeng.statistics;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.utils.http.SuperCodeRequests;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CustomAssert;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.DateIntervalListDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.HarvestStatisticsResponseDTO;
import net.app315.hydra.intelligent.planting.dto.gugeng.statistics.SaleUserLineChartDTO;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BaseSummaryStatisticsService {
    private static String defaultNameKey="dateTime";
    private static String defaultValueKey="totalValue";

    @Value("${rest.trace.url}")
    public String traceDomain;

    @Autowired
    public SuperCodeRequests codeRequests;

    protected ProductionManageSuperToken superToken;

    private static final String SYS_ID = "sys_id";

    private static final String ORGANIZATION_ID = "organization_id";

    public static final String STRIKE_THROUGH = "-";

    @Autowired
    private CommonUtil commonUtil;

    public void setSuperToken(ProductionManageSuperToken superToken){
        this.superToken=superToken;
    }

    public void setAccountInfo(DateIntervalListDTO dto){
        dto.setOrganizationId(superToken.getOrganizationId());
        dto.setSysId(superToken.getSysId());
    }

    public QueryWrapper queryTemplate() {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        try{

            queryWrapper.eq(StringUtils.isNotBlank(commonUtil.getSessionSysId()), SYS_ID, commonUtil.getSessionSysId())
                    .eq(StringUtils.isNotBlank(commonUtil.getSessionOrganizationId()), ORGANIZATION_ID, commonUtil.getSessionOrganizationId());
        } catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return queryWrapper;
    }

    public  <T> List<Map<String, Object>> singleFieldSumSelect(String statisticsField, String dateField, String startQueryDate, String endQueryDate, String tableName,
                                                               boolean dateNeddFormart, BaseMapper baseMapper, QueryWrapper<T> queryWrapper, String customNameKey, String customValueKey) throws SuperCodeException {
        //1分拣入库
        String date=dateField;
        if (dateNeddFormart){
            date="DATE_FORMAT("+dateField+",'%Y-%m-%d')";
        }
        String lowerStatisticsField=statisticsField.substring(0,1).toLowerCase()+statisticsField.substring(1,statisticsField.length());
        String lowerDateField=dateField.substring(0,1).toLowerCase()+dateField.substring(1,dateField.length());

        StringBuilder selectBuilder=new StringBuilder();
        selectBuilder.append("sum(").append(statisticsField).append(")").append(" as ");
        if (StringUtils.isBlank(customValueKey)){
            selectBuilder.append(defaultValueKey);
        }else {
            selectBuilder.append(customValueKey);
        }
        selectBuilder.append(",").append(date).append(" as ");
        if (StringUtils.isBlank(customNameKey)){
            selectBuilder.append(defaultNameKey);
        }else {
            selectBuilder.append(customNameKey);
        }
        String selectSql=selectBuilder.toString();
        queryWrapper.select(selectSql);
        queryWrapper.ge(StringUtils.isNotBlank(startQueryDate),date,startQueryDate);
        queryWrapper.lt(StringUtils.isNotBlank(endQueryDate),date,endQueryDate);
        queryWrapper.groupBy(date);
        List<Map<String, Object>> mapList=baseMapper.selectMaps(queryWrapper);
        return mapList;
    }

    public List<Map<String, Object>> listMapAdd(List<Map<String, Object>> ...mapList){
        List<Map<String, Object>> allListMap=new ArrayList<>();
        if (null!=mapList){
            for(int i=0;i<mapList.length;i++){
                allListMap.addAll(mapList[i]);
            }
        }
        return allListMap;
    }

    public List<HarvestStatisticsResponseDTO> listHarvestStatisticsByDateInterval(String startTime, String endTime, String token) throws SuperCodeException {

        //traceDomain="http://PLATFORM-TRACE-SUPERCODE-T1";
        String url = traceDomain + "/trace/hainanrunda/batchinfo/getHarvestWeightAllOrganization";

        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("super-token", token );

        Map<String, Object> params = new HashMap<>(2);
     /*   params.put("startTime",startTime);
        params.put("endTime", endTime);*/

        try {
            String result = codeRequests.getAndGetResultBySpring(url, params, headerMap, String.class, true);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (jsonObject.getIntValue("state") == 200) {
                JSONArray results = jsonObject.getJSONArray("results");
                List<HarvestStatisticsResponseDTO> responseDTO = results.toJavaList(HarvestStatisticsResponseDTO.class);
                return responseDTO;
            } else {
                CustomAssert.throwException("获取采收重量统计信息失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            CustomAssert.throwException("获取采收重量统计信息失败");
        }

        return null;
    }

    public String today(){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(fmt);
    }
    public LineChartVO createLineChartVO(String name){
        LineChartVO chart0=new LineChartVO();
        List<LineChartVO.NameAndValueVO> values0=new ArrayList<>();
        chart0.setValues(values0);
        LineChartVO.Option option0=chart0.new Option();
        option0.setName(name);
        chart0.setOption(option0);
        return chart0;
    }

    public LineChartVO.NameAndValueVO createNameAndValueVO(LineChartVO chart0, String date, Object val){
        LineChartVO.NameAndValueVO valueVO=chart0.new NameAndValueVO();
        valueVO.setName(date);
        valueVO.setValue(val);
        return valueVO;
    }

    public SaleUserLineChartDTO getQueryDTO(String queryDate)
    {
        String[] dateInterval = queryDate.split(BaseSummaryStatisticsService.STRIKE_THROUGH);
        LocalDate startDate = LocalDate.of(Integer.parseInt(dateInterval[0]), Integer.parseInt(dateInterval[1]), 1);
        LocalDate endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());

        SaleUserLineChartDTO dateIntervalDTO=new SaleUserLineChartDTO();
        dateIntervalDTO.setStartQueryDate(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateIntervalDTO.setEndQueryDate(endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        return dateIntervalDTO;
    }
}
