package com.zxs.server.electronicscale.protocol.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.server.electronicscale.enums.MessageTypeEnum;
import net.app315.hydra.intelligent.planting.server.electronicscale.event.ScaleWeightEvent;
import net.app315.hydra.intelligent.planting.server.electronicscale.event.WebsocketEvent;
import net.app315.hydra.intelligent.planting.server.electronicscale.listener.ScaleWeightListener;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 连接类
 * @author zc
 */
@Slf4j
public class ConnectionHandler extends AbstractWebSocketHandler implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private static List<ScaleWeightListener> scaleWeightListenerList = new ArrayList<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        log.info("收到客户端消息{}",payload);
        if (StringUtils.isNotBlank(payload)){
            MessageMO message1 = JSONObject.parseObject(payload, MessageMO.class);
            for (ScaleWeightListener scaleWeightListener : scaleWeightListenerList) {
                WebsocketEvent websocketEvent = new WebsocketEvent();
                websocketEvent.setSession(session).setMessageType(message1.getMessageType()).setSerialNum(message1.getSerialNum()).setWeight(message1.getWeight());
                try {
                    scaleWeightListener.onWeight(websocketEvent);
                }catch (Exception e){
                   log.error("执行重量监听器onWeight方法异常,scaleWeightListener类型为：{}，异常信息：{}",scaleWeightListener.getClass().getName(),e.getMessage());
                }
            }
        }
    }

    public void sendWeight(String scalesNo,Double weight) throws Throwable {
        Throwable throwable = null;
        for (ScaleWeightListener scaleWeightListener : scaleWeightListenerList) {
            ScaleWeightEvent scaleWeightEvent = new ScaleWeightEvent();
            scaleWeightEvent.setSerialNum(scalesNo).setWeight(weight).setMessageType(MessageTypeEnum.RECEIVE_WEIGHT.getType());
            try {
                scaleWeightListener.onWeight(scaleWeightEvent);
            }catch (Exception e){
                throwable = e;
                log.error("sendWeight方法：执行重量监听器onWeight方法异常,scaleWeightListener类型为：{}，异常信息：{}",scaleWeightListener.getClass().getName(),e.getMessage());
            }
        }
        if (!Objects.isNull(throwable)){
            throw throwable;
        }
    }

    @PostConstruct
    public void init (){
        Map<String, ScaleWeightListener> scaleWeightListenerMap = applicationContext.getBeansOfType(ScaleWeightListener.class);
        if (!Objects.isNull(scaleWeightListenerMap) && !scaleWeightListenerMap.isEmpty()){
            scaleWeightListenerList.addAll(scaleWeightListenerMap.values());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
