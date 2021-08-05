package com.zxs.server.job.gugeng;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.constants.DateTimePatternConstant;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.enums.gugeng.SystemTypeEnum;
import net.app315.hydra.intelligent.planting.pojo.gugeng.salemanage.ProductionManageClient;
import net.app315.hydra.intelligent.planting.pojo.gugeng.statistics.ProductionManageSuperToken;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.salemanage.ProductManageClientMapper;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.CommonService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.common.MessageInformService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.*;
import net.app315.hydra.intelligent.planting.server.service.gugeng.synchronous_data.SynchronousDataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class SummaryStatisticsJob {

    @Autowired
    private SaleSummaryStatisticsService saleStatisticsService;

    @Autowired
    private WarehouseSummaryStatisticsService warehouseSummaryStatisticsService;

    @Autowired
    private ProduceSummaryStatisticsService produceSummaryStatisticsService;

    @Autowired
    private SaleOverviewStatisticsService saleOverviewStatisticsService;

    @Autowired
    private ProductionManageSuperTokenMapper superTokenMapper;

    @Autowired
    private ProductionManageClientOrderDataStatisticsService clientOrderDataStatisticsService;

    @Autowired
    private  ProductionManageOrderDataByTypeService orderDataByTypeService;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private GreenhouseHarvestWeightStatisticsJob weightStatisticsJob;

    @Autowired
    private ProductionYieldDataStatisticsJob yieldDataStatisticsJob;

    @Autowired
    private ReportLossStatisticsJob reportLossStatisticsJob;

    @Autowired
    private ShippingDataStatisticsJob shippingDataStatisticsJob;

    @Autowired
    private SaleTargetDataStatisticsJob saleTargetDataStatisticsJob;

    @Autowired
    private ProductProductionStatisticsJob productProductionStatisticsJob;

    @Autowired
    private ProductManageClientMapper clientMapper;

    @Autowired
    private MessageInformService messageInformService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private ProductRecordDataStatisticsJob productRecordDataStatisticsJob;

    @Autowired
    private SynchronousDataService synchronousDataService;

//    @Scheduled(cron ="0 0 3 * * ?")
    public void statisticsSummaryData() {

        List<ProductionManageSuperToken> superTokens= superTokenMapper.getSuperTokenList();
        for(ProductionManageSuperToken superToken: superTokens){
            commonUtil.setSessionToken(superToken);

            if(superToken.getType().equals(SystemTypeEnum.GUGENG.getKey())){
            }

            try{
                saleOverviewStatisticsService.setSuperToken(superToken);
                saleOverviewStatisticsService.statisticsSaleData();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            try{
                warehouseSummaryStatisticsService.setSuperToken(superToken);
                warehouseSummaryStatisticsService.statisticsWarehouseData();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            try{
                saleStatisticsService.setSuperToken(superToken);
                saleStatisticsService.statisticsSaleOrder();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            try{
                produceSummaryStatisticsService.setSuperToken(superToken);
                produceSummaryStatisticsService.statisticsProduceData();
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }

            commonUtil.removeSessionToken();
        }

        // 区域数据统计job
        try {
            weightStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 产品产量，产量对比job
        try {
            yieldDataStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 报损数据统计job
        try {
            reportLossStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 发货数据统计job
        try {
            shippingDataStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 客户订单数据统计
        try {
            clientOrderDataStatisticsService.timedTaskDataSync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 订单情况，按订单状态数据统计
//        try {
//            orderDataByTypeService.timedTaskDataSync();
//        } catch (Exception e) {
//            log.error(e.getMessage(), e);
//        }

        // 销售目标数据统计
        try {
            saleTargetDataStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 产品产量数据统计
        try {
            productProductionStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        // 产品档案数据统计
        try {
            productRecordDataStatisticsJob.statisticsData();
        } catch (Exception e) {
            log.warn("产品档案数据统计失败");
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * 古耕生日提醒
     */
//    @Scheduled(cron ="0 0 2 * * ?")
    public void birthDay() {
        Map<String,String> employeeMap=new HashMap<>();
        Date today = new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat(DateTimePatternConstant.YYYY_MM_DD);
        String todayFormat = dateFormat.format(today);
        QueryWrapper<ProductionManageClient> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("GgBirthDay",todayFormat);
        List<ProductionManageClient> toadyProductionManageClients = clientMapper.selectList(queryWrapper);
        sendBirthDayGreet(toadyProductionManageClients,"今天",employeeMap);

        //发送7天前提  醒
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(today);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth+7);
        Date sevenDay = calendar.getTime();
        String sevenFormat = dateFormat.format(sevenDay);
        queryWrapper.eq("GgBirthDay",sevenFormat);
        List<ProductionManageClient> sevenProductionManageClients = clientMapper.selectList(queryWrapper);
        sendBirthDayGreet(toadyProductionManageClients,"7",employeeMap);

        //30天
        calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth+23);
        Date time = calendar.getTime();
        String thirtyFormat = dateFormat.format(time);
        queryWrapper.eq("GgBirthDay",thirtyFormat);
        List<ProductionManageClient> productionManageClients = clientMapper.selectList(queryWrapper);
        sendBirthDayGreet(productionManageClients,"30",employeeMap);
    }

    private void sendBirthDayGreet(List<ProductionManageClient> productionManageClients, String prefix, Map<String, String> employeeMap) {
        if (CollectionUtils.isNotEmpty(productionManageClients)){
            for (ProductionManageClient productionManageClient : productionManageClients) {
                String saleUserId = productionManageClient.getSaleUserId();
                String saleUserName = productionManageClient.getSaleUserName();
                String clientName = productionManageClient.getClientName();
                if (StringUtils.isNotBlank(saleUserId)){
                    try {
                        String userIdByEmployId = employeeMap.get(saleUserId);
                        if (null==userIdByEmployId){
                            userIdByEmployId = commonService.getUserIdByEmployId(saleUserId);
                            employeeMap.put(saleUserId,userIdByEmployId);
                        }
                        messageInformService.sendOrgMessageToAllPartmentUser(userIdByEmployId,saleUserName,prefix+"天后是您的“"+clientName+"”客户生日，请留意并及时送上祝福哦",0,0,0);
                    }catch (Exception e){
                        log.error("给销售人员"+saleUserName+"发送生日提示失败："+e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * 种植存量数据同步
     * 每2小时执行一次
     *
     * @author shixiongfei
     * @date 2019-12-17
     * @updateDate 2019-12-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
//    @Scheduled(cron = "0 0 */2 * * ?")
    public void plantStockDataSync() {
        log.info("=====种植存量数据同步执行开始=====");
        synchronousDataService.plantStockStatisticsData();
        log.info("=====种植存量数据同步执行结束=====");
    }


    /**
     * 古耕订单情况
     * @author shixiongfei
     * @date 2019-12-17
     * @updateDate 2019-12-17
     * @updatedBy shixiongfei
     * @param   0 0/5 * * * ?
     * @return  * 1/1 * * * ?
     */
//    @Scheduled(cron = "0 0 4 * * ?")
    public void test() {
        // 订单情况，按订单状态数据统计
        try {
            orderDataByTypeService.timedTaskDataSync();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}