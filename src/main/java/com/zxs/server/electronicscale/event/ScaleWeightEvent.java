package com.zxs.server.electronicscale.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 获取重量事件
 * @author zc
 */
@Data
@Accessors(chain=true)
public class ScaleWeightEvent  {

    /**
     * 1.登陆
     */
    private int messageType;

    private String serialNum;

    private Double weight;
}
