package com.zxs.server.electronicscale.protocol.websocket;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消息
 * @author zc
 */
@Data
@Accessors(chain = true)
public class MessageMO {
    /**
     * 1.登陆
     */
    private int messageType;

    private String serialNum;

    private Double weight;

    private int state;

    private String msg;
}
