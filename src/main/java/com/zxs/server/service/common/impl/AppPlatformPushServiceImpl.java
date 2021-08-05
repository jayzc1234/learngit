package com.zxs.server.service.common.impl;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.bo.common.AppPushMsgModel;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.common.gugeng.util.RestTemplateUtil;
import net.app315.hydra.intelligent.planting.server.service.common.AppPlatformPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zc
 */
@Slf4j
@Service
public class AppPlatformPushServiceImpl implements AppPlatformPushService {

    @Value("${app.platform.push.appid}")
    private String appPushAppId;

    @Value("${rest.user.url}")
    private String userUrl;

    @Autowired
    private CommonUtil commonUtil;

    @Autowired
    private RestTemplateUtil restTemplateUtil;

    @Override
    public void push(AppPushMsgModel appPushMsgModel) {
        appPushMsgModel.setAppId(appPushAppId);
        String json = JSONObject.toJSONString(appPushMsgModel);
        Map<String,String> headMap = new HashMap<>();
        ResponseEntity<String> stringResponseEntity = restTemplateUtil.postJsonDataAndReturnJosn(userUrl + "/push/app-transmission", json, headMap);
        if (!(stringResponseEntity.getStatusCodeValue()>= 200 && stringResponseEntity.getStatusCodeValue()<300)){
            log.info("调用app平台推送失败{}",stringResponseEntity.getBody());
            CommonUtil.throwSuperCodeExtException(500,"调用失败");
        }
    }

}
