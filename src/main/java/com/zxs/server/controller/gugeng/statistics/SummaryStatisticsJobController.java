package com.zxs.server.controller.gugeng.statistics;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.wechat.token.WechatAccessToken;
import net.app315.hydra.intelligent.planting.server.job.gugeng.SummaryStatisticsJob;
import net.app315.hydra.intelligent.planting.server.mapper.gugeng.statistics.ProductionManageSuperTokenMapper;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.ProduceSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleOverviewStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.SaleSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.statistics.WarehouseSummaryStatisticsService;
import net.app315.hydra.intelligent.planting.server.service.gugeng.wechat.ProductionManageWechatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static net.app315.hydra.intelligent.planting.AppConstants.VALID_PATH;

@RestController
@RequestMapping(VALID_PATH+"/statistics/summarystatisticsjob")
@Api(tags = "summarystatisticsjob")
@Slf4j
public class SummaryStatisticsJobController {

    @Autowired
    private SummaryStatisticsJob summaryStatisticsJob;

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
    private CommonUtil commonUtil;


    @GetMapping("/statisticsSummaryData")
    @ApiOperation(value = "statisticsSummaryData", notes = "")
    public String statisticsSummaryData() throws Exception {
        summaryStatisticsJob.statisticsSummaryData();
        return "ok";
    }


    @Autowired
    private ProductionManageWechatMessageService messageService;

    @GetMapping("/test")
    @ApiImplicitParam(name = "super-token", paramType = "header", defaultValue = "64b379cd47c843458378f479a115c322", value = "token信息", required = true)
    public String test() throws Exception {

        //messageService.createOrderMessage();

        //int i=0;

/*        List<String> userIds=new ArrayList<>();
        userIds.add("11");
        userIds.add("22");
        messageService.createMessage("订单待审核", userIds, MessageTypeEnum.Order);
        messageService.createMessage("库存不足", userIds, MessageTypeEnum.Stock);*/



        WechatAccessToken t=new WechatAccessToken();
        //String token= t.getToken();
        String openId= t.getOpenId("071EcxmQ0V6Zs72V3CjQ0BjxmQ0EcxmJ");
        //MessageTemplate m=new MessageTemplate();
        //m.sendTemplate(token, "美月西瓜产品等级为美月尊贵的当前库存低于100斤。", 1);
        //m.sendTemplate(token, "您有一条待审核的订单，请登录系统及时审核。", 0);


/*        List<ProductionManageSuperToken> superTokens= superTokenMapper.getSuperTokenList();
        for(ProductionManageSuperToken superToken: superTokens){
            commonUtil.setSessionToken(superToken);

            try{
                saleOverviewStatisticsService.setSuperToken(superToken);
                saleOverviewStatisticsService.statisticsSaleData();
            }catch (Exception e){
                e.printStackTrace();
            }

        }*/

        return "ok";
    }

}
