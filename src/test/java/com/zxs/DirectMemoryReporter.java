package com.zxs;

import io.netty.util.internal.PlatformDependent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
 
import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
 
/**
 * @date 2020/10/19 11:14 上午
 */
 @Slf4j
public class DirectMemoryReporter {
    private static final String BUSINESS_KEY = "netty_direct_memory";
    private AtomicLong directMemory;
    
    public void init(){
        Field field = ReflectionUtils.findField(PlatformDependent.class,"DIRECT_MEMORY_COUNTER");
        field.setAccessible(true);
        try{
            directMemory = ((AtomicLong)field.get(PlatformDependent.class));
        }catch (Exception e){
 
        }
    }
 
    public void doReport(String processName){
        try{
            long memoryInb = directMemory.get();
            log.error(processName + "**********" + BUSINESS_KEY + ":" + memoryInb);
        }catch (Exception e){
 
        }
    }
}
