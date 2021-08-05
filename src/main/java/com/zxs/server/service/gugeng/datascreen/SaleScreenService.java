package com.zxs.server.service.gugeng.datascreen;






import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.LocalDateTimeUtil;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageSalesData;
import net.app315.hydra.intelligent.planting.pojo.gugeng.datascreen.ProductionManageSalesDataBar;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.datascreen.ProductionManageSaleScreenMapper;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SaleScreenService  extends CommonUtil {

    @Autowired
    private ProductionManageSaleScreenMapper saleScreenMapper;

    public Map<String,Object> selectData(){
        Map<String,Object> resultMap=new HashMap<String, Object>();
        resultMap.put("salesOrderAnalysis", saleScreenMapper.selectSalesOrderAnalysis(getOrganizationId(), getSysId()));
        resultMap.put("salesDataRealTime", saleScreenMapper.selectLastestOrder(getOrganizationId(), getSysId()));
        resultMap.put("commodityRanking", saleScreenMapper.commodityRanking(getOrganizationId(), getSysId()));
        resultMap.put("regionalRanking", saleScreenMapper.selectRegionalRanking(getOrganizationId(), getSysId()));
        resultMap.put("topSaleRegional", saleScreenMapper.selectTopSaleRegionalList(getOrganizationId(), getSysId()));

        String today= DateFormatUtils.format(new Date(), LocalDateTimeUtil.DATE_PATTERN);
        String month = DateFormatUtils.format(new Date(), LocalDateTimeUtil.YEAR_AND_MONTH)+"-01";
        List<ProductionManageSalesData> salesDataList= saleScreenMapper.selectSalesData(today, month, getOrganizationId(), getSysId());
        Double  outputValue=salesDataList.get(2).getLumpSum(); //总产值
        resultMap.put("salesAmount",outputValue);
        resultMap.put("orderCustomer",saleScreenMapper.selectClientCount(getOrganizationId(), getSysId()));

        LocalDate weekday=LocalDate.now().plusMonths(-5);
        String start= weekday.toString().substring(0,8)+"01";
        List<ProductionManageSalesDataBar> salesDataBars=saleScreenMapper.selectSalesDataBar(getOrganizationId(), getSysId(), start);
        for(int i=0; i<6; i++){
            String day= weekday.plusMonths(i).toString().substring(0,7);
            if(!salesDataBars.stream().anyMatch(e->e.getMonth().equals(day))){
                salesDataBars.add(i, new ProductionManageSalesDataBar(day, 0d, 0d));
            }
        }

        resultMap.put("salesDataBar", salesDataBars);
        return resultMap;
    }

}
