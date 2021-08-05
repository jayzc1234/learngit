package com.zxs.server.rocketmq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @Description:    普通消息listener
 * @Author:         xiaoliang.chen
 * @Date:     2019/2/21 上午11:55
 */
@Slf4j
public class ExampleMessageListener implements MessageListenerConcurrently {

	private ExampleMessageHandler msgConsumeComponent;

	public void setMsgConsumeComponent(ExampleMessageHandler msgConsumeComponent) {
		this.msgConsumeComponent = msgConsumeComponent;
	}


	@Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
    	for (MessageExt messageExt : msgs) {
			boolean result = msgConsumeComponent.handleMessage(messageExt);
			if (!result){
				return ConsumeConcurrentlyStatus.RECONSUME_LATER;
			}
    	}
    	return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}