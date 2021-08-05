package com.zxs.server.electronicscale.listener;

import net.app315.hydra.intelligent.planting.server.electronicscale.event.ScaleWeightEvent;

/**
 * 电子秤重量监听器
 * @author zc
 *
 */
public interface ScaleWeightListener<T extends ScaleWeightEvent> {

    /**
     * 获取重量事件
     * @param t
     */
   void onWeight(T t);
}
