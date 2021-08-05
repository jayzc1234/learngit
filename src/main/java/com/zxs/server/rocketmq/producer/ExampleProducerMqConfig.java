package com.zxs.server.rocketmq.producer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.user.common.enums.ErrorCodeEnum;
import net.app315.nail.common.Assert;
import net.app315.nail.common.exception.BaseException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:   producer config
 * @Author:         xiaoliang.chen
 * @Date:     2019/2/21 上午11:56
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "hydra-test.rocketmq.producer")
public class ExampleProducerMqConfig {
    private String groupName;
    private String namesrvAddr;
    /** 4M */
    private int maxMessageSize;
    private int sendMsgTimeout;

    @Bean("testProducer")
    public DefaultMQProducer getRocketmqProducer(){
        Assert.assertNotNull(groupName, ErrorCodeEnum.PARAMS_ERROR, "groupName");
        Assert.assertNotNull(namesrvAddr, ErrorCodeEnum.PARAMS_ERROR, "namesrvAddr");
        DefaultMQProducer producer = new DefaultMQProducer(this.groupName);
        producer.setNamesrvAddr(this.namesrvAddr);
        producer.setMaxMessageSize(this.maxMessageSize);
        producer.setSendMsgTimeout(this.sendMsgTimeout);
        try {
            producer.start();
            log.info("bill producer is start ! groupName:[{}],namesrvAddr:[{}]" , this.groupName, this.namesrvAddr);
        } catch (MQClientException e) {
            log.error("bill producer is error {}", e);
            throw new BaseException(ErrorCodeEnum.SERVICE_MQ_PRODUCER_ERROR);
        }
        return producer;
    }

}
