package com.zxs.server.electronicscale.listener;

import net.app315.hydra.intelligent.planting.bo.common.AppPushMsgModel;
import net.app315.hydra.intelligent.planting.bo.common.ElectronicPushBO;
import net.app315.hydra.intelligent.planting.server.electronicscale.event.ScaleWeightEvent;
import net.app315.hydra.intelligent.planting.server.service.common.AppPlatformPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * app系统推送监听
 * @author zc
 */
@Component
public class AppPusherListener implements ScaleWeightListener<ScaleWeightEvent>{

    public  static final Map<String, ElectronicPushBO> PUSH_INFO_MAP = new HashMap<>();

    @Autowired
    private AppPlatformPushService appPlatformPushService;

    @Override
    public void onWeight(ScaleWeightEvent scaleWeightEvent) {
        ElectronicPushBO electronicPushBO = PUSH_INFO_MAP.get(scaleWeightEvent.getSerialNum());
        if (!Objects.isNull(electronicPushBO)){

            Double weight = scaleWeightEvent.getWeight();
            AppPushMsgModel appPushMsgModel = new AppPushMsgModel();
            appPushMsgModel.setAppType(2);

            List<String> cidList = new ArrayList<>();
            cidList.add(electronicPushBO.getPushId());
            AppPushMsgModel.PushMsgBody msgBody = new AppPushMsgModel.PushMsgBody();
            msgBody.setMsgContent(String.valueOf(weight));
            msgBody.setDisplayType("0");
            appPushMsgModel.setMsgBody(msgBody);
            appPushMsgModel.setCidList(cidList);
            appPlatformPushService.push(appPushMsgModel);
        }
    }
}
