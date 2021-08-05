package com.zxs.server.config.excel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 针对不同的业务场景使用不同的限流:在外部加k v[RateLimiter]不做在限流类里
 * 漏桶;[此场景更适合漏桶而不是令牌桶]
 * 把参数全部抽出到外部
 * 流动速率: ${number}/${time}单位是秒
 * 容量: ${capacity}
 */
@Configuration
public class RateLimiterConfig {
    // 流动速率:数量
    @Value("${common.excel.number}")
    private int number;
    // 流动速率:时间
    @Value("${common.excel.time}")
    private int time;


    @Value("${common.excel.capacity}")
    private int capacity;

    // 导出大小
    @Value("${common.excel.export-number}")
    private int exportSize;


    @Bean("excelLimiter")
    public RateLimiter getRateLimiter(){
        return new RateLimiter(number,time,capacity,exportSize);

    }

}
