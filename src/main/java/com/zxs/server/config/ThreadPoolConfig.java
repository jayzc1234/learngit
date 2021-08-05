package com.zxs.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 线程池config
 * @author liujianqiang
 * @date 2018年7月18日
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //线程池维护线程最小数量,线程池的大小
        executor.setCorePoolSize(5);
        //最大数量
        executor.setMaxPoolSize(10);
        //持有等待执行的任务队列,当当前线程数超过CorePoolSize就会等待
        executor.setQueueCapacity(100);
        //线程多久没执行关闭线程,单位秒,默认60秒
        executor.setKeepAliveSeconds(60);
        //线程名前缀
        executor.setThreadNamePrefix("runda_thread");
        executor.initialize();
        return executor;
    }


    @Bean(name = "excelExecutor")
    public TaskExecutor excelExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(0);
        executor.setKeepAliveSeconds(60);//超过核心线程的线程IDLE时间
        executor.setThreadNamePrefix("excel_thread");//线程名前缀
        executor.initialize();
        return executor;
    }

    /**
     * 用于job任务更新时所需的线程池
     * 目的是为了保证更新数据的同步，维持数据的最终一致性
     *
     * @author shixiongfei
     * @date 2019-10-17
     * @updateDate 2019-10-17
     * @updatedBy shixiongfei
     * @param
     * @return
     */
    @Bean(name = "jobExecutor")
    public TaskExecutor jobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(11);
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("job thread");
        // 超过核心线程的线程IDLE时间
        executor.setKeepAliveSeconds(0);
        executor.initialize();
        return executor;
    }
}