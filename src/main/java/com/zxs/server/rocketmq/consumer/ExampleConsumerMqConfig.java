package com.zxs.server.rocketmq.consumer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.user.common.enums.ErrorCodeEnum;
import net.app315.nail.common.Assert;
import net.app315.nail.common.exception.BaseException;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:    consumer config
 * @Author:         xiaoliang.chen
 * @Date:     2019/2/21 上午11:55
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "hydra-example.rocketmq.consumer")
public class ExampleConsumerMqConfig {
    private String namesrvAddr;
    private String groupName;
    private String topic;
    private int consumeThreadMin;
    private int consumeThreadMax;

    @Autowired
    private ExampleMessageHandler msgConsumeComponent;

    @Bean("factoryConsumer")
    public DefaultMQPushConsumer getRocketmqConsumer(){
        Assert.assertNotNull(groupName, ErrorCodeEnum.PARAMS_ERROR, "groupName");
        Assert.assertNotNull(namesrvAddr, ErrorCodeEnum.PARAMS_ERROR, "namesrvAddr");
        Assert.assertNotNull(topic, ErrorCodeEnum.PARAMS_ERROR, "topic");

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        ExampleMessageListener messageListener = new ExampleMessageListener();
        messageListener.setMsgConsumeComponent(msgConsumeComponent);
        consumer.registerMessageListener(messageListener);
        try {
            consumer.subscribe(topic, "");
            consumer.start();
            log.info("test consumer is start !!! groupName:{},topic:{},namesrvAddr:{}",groupName,topic,namesrvAddr);
        } catch (MQClientException e){
            throw new BaseException(ErrorCodeEnum.SERVICE_MQ_CONSUMER_ERROR);
        }
        return consumer;
    }

}
