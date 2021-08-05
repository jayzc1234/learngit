package com.zxs.server.service.common;


import net.app315.hydra.intelligent.planting.bo.common.AppPushMsgModel;

/**
 * app平台推送
 * @author zc
 */
public interface AppPlatformPushService {
    /**
     * app平台推送
     * @param appPushMsgModel
     */
    void push(AppPushMsgModel appPushMsgModel);
}
