package com.zxs.server.config.excel;

import java.lang.annotation.*;

/**
 * 所有包含该注解的方法将开启限流功能
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface EnableRateLimiter {
}
