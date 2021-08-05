package com.zxs.server.config.excel;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import lombok.Data;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 针对不同的业务场景使用不同的限流:在外部加k v[RateLimiter]不做在限流类里
 * 漏桶;[此场景更适合漏桶而不是令牌桶]
 * 把参数全部抽出到外部
 * 流动速率: ${number}/${time}单位是秒
 * 容量: ${capacity}
 *
 * 配置注意项:流动速率> 0，速率和容量保持均衡
 */
@Data
public class RateLimiter {
    // 流动速率:数量
    private int number;
    // 流动速率:时间
    private int time;
    // 桶大小
    private int capacity;
    private int exportSize;
    public static final int SYSTEM_EXPORT_SIZE = 10000;

    // 线性一致剩余流水 暂时不用
//    private AtomicInteger size ;
    // 剩余流水
    private int size;
    // 流量补充水龙头
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    /**
     *
     * @param number
     * @param time
     * @param capacity
     */
    public RateLimiter(int number, int time, int capacity,int exportSize ){
        this.capacity = capacity;
        this.number =number;
        this.time= time;
        this.exportSize= exportSize;
        size =capacity;
        // 这里可以不用AtomicInteger，允许限流存在一定的弹性
//        size = new AtomicInteger(capacity);
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
                if(size<capacity){
                    size += number/time;
                    if(size>capacity){
                        size = capacity;
                    }
                }else{
                    size = capacity;
                }
            // 按速率添加
        },0L,1, TimeUnit.SECONDS); // 每秒执行一次
    }
    public RateLimiter(){

    }

    // TODO 调用此方法判断是否能执行业务
    public boolean canDoBiz() throws SuperCodeException{
        if(size > 0){
            size--;
            return true;
        }else {
            throw new SuperCodeException("流量受限");
        }

    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter r = new RateLimiter(3,1,10,10000);
        while (true){
            try {
                if(r.canDoBiz()){
                    System.out.println("get");
                }else {
                    System.out.println("not get");
                }
            } catch (SuperCodeException e) {
                System.out.println("not ");
            }
        }
    }
}
