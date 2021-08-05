package com.zxs.server.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description:    consumer handler
 * @Author:         xiaoliang.chen
 * @Date:     2019/2/21 上午11:55
 */
@Slf4j
@Component
@ConfigurationProperties(prefix = "hydra-example.rocketmq.producer")
public class ExampleMessageHandler {


    public boolean handleMessage(MessageExt messageExt) {
        try {

        } catch (Exception e) {

        }
        return true;
    }
}
