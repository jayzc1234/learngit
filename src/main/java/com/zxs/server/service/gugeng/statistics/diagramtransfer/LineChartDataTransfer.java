package com.zxs.server.service.gugeng.statistics.diagramtransfer;


import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.vo.gugeng.common.LineChartVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineChartDataTransfer {
    private static BigDecimal defaultTotalValue=new BigDecimal(0);
    /**
     * 构建lineChart数据结构
     * @param dateList
     * @param dataList
     * @param nameKey
     * @param valueKey
     * @param desc
     * @return
     */
    public  static LineChartVO transfer(List<String> dateList, List<? extends Map<String, Object>> dataList, String nameKey, String valueKey, String desc) {
        LineChartVO lineChartVO=new LineChartVO();
        List<LineChartVO.NameAndValueVO> values=new ArrayList<>();
        //构建option
        LineChartVO.Option option=lineChartVO.new Option();
        option.setName(desc);
        lineChartVO.setOption(option);
        BigDecimal totalValue =new BigDecimal(0);
        if (null==dataList || dataList.isEmpty()){
            for (String date: dateList) {
                LineChartVO.NameAndValueVO nameAndValueVO=lineChartVO.new NameAndValueVO();
                nameAndValueVO.setName(date);
                nameAndValueVO.setValue(0);
                values.add(nameAndValueVO);
            }
            lineChartVO.setTotalValue(defaultTotalValue);
        }else {
            Map<String,List<Map<String,Object>>> dataMap=new HashMap<>();
            for (Map<String,Object> map:dataList) {
                if (null!=map){
                    List<Map<String,Object>> mapList=null;
                    Object o = map.get(nameKey);
                    if (null == o){
                        mapList=new ArrayList<>();
                    }else {
                        mapList=dataMap.get(o.toString());
                        if (null==mapList){
                            mapList=new ArrayList<>();
                        }
                    }
                    mapList.add(map);
                    dataMap.put(map.get(nameKey).toString(),mapList);
                }
            }
            for (String date: dateList) {
                LineChartVO.NameAndValueVO nameAndValueVO=lineChartVO.new NameAndValueVO();
                nameAndValueVO.setName(date);
                //汇总当天多个统计维度的值的总和(比如一个list里有日期2019-09-01日期的多个map,需要对相同日期的数据做汇总)
                BigDecimal currentDaytotalValue =new BigDecimal(0);
                if (null!=dataMap.get(date)){
                    Object value=null;
                    List<Map<String,Object>> mapList=dataMap.get(date);
                    for (Map<String,Object> map:mapList) {
                        value=map.get(valueKey);
                        if (null!=value){
                            if (value instanceof Number){
                                Number valueNum= (Number) value;
                                BigDecimal bigDecimalValue=new BigDecimal(valueNum.toString());
                                currentDaytotalValue= CommonUtil.bigDecimalAdd(currentDaytotalValue,bigDecimalValue);
                                totalValue=CommonUtil.bigDecimalAdd(totalValue,bigDecimalValue);
                            }else if(value instanceof  String){
                                boolean isNum=CommonUtil.isNumber(value.toString());
                                if (isNum){
                                    BigDecimal bigDecimalValue=new BigDecimal(value.toString());
                                    currentDaytotalValue=CommonUtil.bigDecimalAdd(currentDaytotalValue,bigDecimalValue);
                                    totalValue=CommonUtil.bigDecimalAdd(totalValue,bigDecimalValue);
                                }
                            }
                        }
                        nameAndValueVO.setValue(currentDaytotalValue.setScale(2,BigDecimal.ROUND_HALF_UP));
                    }
                }else{
                    nameAndValueVO.setName(date);
                    nameAndValueVO.setValue(0);
                }
                values.add(nameAndValueVO);
            }
        }
        lineChartVO.setTotalValue(totalValue);
        lineChartVO.setValues(values);
        return lineChartVO;
    }
}
