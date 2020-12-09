package com.zxs.test;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.locks.LockSupport;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootStartApplicationTests {
    @Resource
    private ThreadPoolTaskExecutor executor;
    @Test
    public void contextLoads() throws Exception {
        // 一共 10 批任务
        for(int i = 0; i < 10; i++) {
            // 每次执行一批任务
            doOnceTasks();
            System.out.println("---------------------------------------" + i);
        }
    }
    /**
     * 每次完成 15 个任务后，再进行下一次任务
     */
    private void doOnceTasks(){
        List<Future> futureList = Lists.newArrayListWithCapacity(15);
        for(int i = 0; i < 15; ++i){
            Future future = executor.submit(()->{
                // 随机睡 0-5 秒
                int sec = new Double(Math.random() * 5).intValue();
                LockSupport.parkNanos(sec * 1000 * 1000 * 1000);
                System.out.println(Thread.currentThread().getName() + "  end");
            });
            futureList.add(future);
        }
        // 等待所有任务执行结束
        for(Future future : futureList){
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}