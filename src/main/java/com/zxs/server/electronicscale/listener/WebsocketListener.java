package com.zxs.server.electronicscale.listener;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import net.app315.hydra.intelligent.planting.common.gugeng.util.CommonUtil;
import net.app315.hydra.intelligent.planting.server.electronicscale.enums.MessageTypeEnum;
import net.app315.hydra.intelligent.planting.server.electronicscale.event.ScaleWeightEvent;
import net.app315.hydra.intelligent.planting.server.electronicscale.event.WebsocketEvent;
import net.app315.hydra.intelligent.planting.server.electronicscale.protocol.websocket.MessageMO;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * pc端监听器
 * @author zc
 */
@Slf4j
@Component
public class WebsocketListener implements ScaleWeightListener<ScaleWeightEvent> {

    private static final ConcurrentHashMap<String,WebSocketSession> WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();

    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(1);

    @Override
    public void onWeight(ScaleWeightEvent scaleWeightEvent) {
        int messageType = scaleWeightEvent.getMessageType();

        if (scaleWeightEvent instanceof WebsocketEvent){
            WebsocketEvent websocketEvent = (WebsocketEvent) scaleWeightEvent;
            if (MessageTypeEnum.REGISTER.getType() == messageType){
                try {
                    register(websocketEvent.getSession(),websocketEvent.getSerialNum());
                } catch (IOException e) {
                    CommonUtil.throwSuperCodeExtException(500,e.getLocalizedMessage());
                }
            }
        }else {
            if (MessageTypeEnum.RECEIVE_WEIGHT.getType() == messageType){
                sendWeight(scaleWeightEvent);
            }
        }
    }

    /**
     * 发送重量到webscoket
     * @param scaleWeightEvent
     */
    private void sendWeight(ScaleWeightEvent scaleWeightEvent) {
        String scalesNo = scaleWeightEvent.getSerialNum();
        Double weight = scaleWeightEvent.getWeight();
        log.info("发送重量到web，电子秤编号：{}，重量：{}",scalesNo,weight);
        WebSocketSession webSocketSession = WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.get(scalesNo);
        if (Objects.isNull(webSocketSession)){
            log.info("接收到编号：{}的电子秤重量：{}，电子秤连接不存在",scalesNo,weight);
            return;
        }
        if (!webSocketSession.isOpen()){
            log.info("接收到编号：{}的电子秤重量：{}，电子秤连接已关闭",scalesNo,weight);
            try {
                webSocketSession.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.remove(scalesNo);
            }
        }
        MessageMO message1 = new MessageMO();
        message1.setMessageType(MessageTypeEnum.RECEIVE_WEIGHT.getType());
        message1.setState(200).setWeight(weight).setSerialNum(scalesNo);
        try {
            webSocketSession.sendMessage(new TextMessage(JSONObject.toJSONString(message1)));
        } catch (IOException e) {
            CommonUtil.throwSuperCodeExtException(500,e.getLocalizedMessage());
        }
    }

    /**
     * 注册
     * @param session
     * @param scalesNo
     * @throws IOException
     */
    private void register(WebSocketSession session, String scalesNo) throws IOException {
        MessageMO message = new MessageMO();
        if (StringUtils.isBlank(scalesNo)){
            message.setState(500).setMsg("电子秤编号不可为空");
            session.sendMessage( new TextMessage(JSONObject.toJSONString(message)));
            return;
        }
        WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.put(scalesNo,session);
        message.setState(200).setMsg("注册成功");
        session.sendMessage( new TextMessage(JSONObject.toJSONString(message)));
    }

    @PostConstruct
    public void init (){
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(()->{
            for (String key : WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.keySet()) {
                WebSocketSession webSocketSession = WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.get(key);
                if (!Objects.isNull(webSocketSession) && !webSocketSession.isOpen()){
                    try {
                        webSocketSession.close();
                    } catch (IOException e) {
                    }
                    WEB_SOCKET_SESSION_CONCURRENT_HASH_MAP.remove(key);
                }
            }

        },20,20, TimeUnit.MINUTES);
    }
}
