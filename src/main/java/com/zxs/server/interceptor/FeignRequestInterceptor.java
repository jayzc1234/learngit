package com.zxs.server.interceptor;

import com.jgw.supercodeplatform.common.properties.config.SysAuthProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @Description:    拦截器
 * @Author:         zc 添加些feign的请求头
 * @Date:     2019/8/21 上午8:47
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Autowired
    private SysAuthProperties authProperties;

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        //添加token
        String token = request.getHeader("super-token");
        if (token != null && !"".equals(token.trim())) {
            template.header("super-token", token);
        } else {
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("super-token")) {
                    template.header("super-token", cookie.getValue());
                }
            }
        }
        // 加服务调用识别
        String secretKeyName = authProperties.getSecretKeyName();
        String secretKeyValue = authProperties.getSecretKeyValue();
        template.header(secretKeyName, secretKeyValue);
    }
}
