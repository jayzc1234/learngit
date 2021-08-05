package com.zxs.server.electronicscale.event;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebsocketEvent
 * @author zc
 */
@Data
@Accessors(chain = true)
public class WebsocketEvent extends ScaleWeightEvent {

    private WebSocketSession session;

    public static void main(String[] args) {

        WebsocketEvent websocketEvent = new WebsocketEvent();
    }
}
