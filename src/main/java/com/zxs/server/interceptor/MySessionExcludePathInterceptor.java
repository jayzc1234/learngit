package com.zxs.server.interceptor;

import net.app315.hydra.user.sdk.support.AbstractWebMvcInterceptorPathConfigurer;
import org.springframework.stereotype.Component;

import static net.app315.hydra.intelligent.planting.AppConstants.*;
import static net.app315.hydra.intelligent.planting.AppConstants.PATH_NOT_VALID_PREFIX;

/**
 * @Description:    拦截器
 * @Author:         xiaoliang.chen
 * @Date:     2019/8/21 上午8:47
 */
@Component
public class MySessionExcludePathInterceptor extends AbstractWebMvcInterceptorPathConfigurer {
    @Override
    protected String[] excludePathPatternsExt() {
        String[] add = new String[]{
                //swagger
                "/swagger-resources/configuration/ui",
                "/swagger-resources",
                "/swagger-resources/configuration/security",
                "/swagger-ui.html",
                "/v2/**",
                "/doc.html",
                "/productmanage/harvest/damage/hi",
                "/webjars/**",

                "/hydra-intelligent-planting/swagger-resources/configuration/ui",
                "/hydra-intelligent-planting/swagger-resources",
                "/hydra-intelligent-planting/swagger-resources/configuration/security",
                "/hydra-intelligent-planting/swagger-ui.html",
                "/hydra-intelligent-planting/v2/**",
                "/hydra-intelligent-planting/doc.html",
                //静态资源
                "/hydra-intelligent-planting/webjars/**",
                "/healthCheck",
                "/hydra-intelligent-planting/valid/api/v1/electronic-scale/maintain/weight",
                "/hydra-intelligent-planting/valid/api/v1/product/level/maintain/select",
                "/hydra-intelligent-planting/valid/api/v1/dictionary/weight/unit/un-login",
                PATH_NOT_VALID_PREFIX
        };
        return add;
    }
}
