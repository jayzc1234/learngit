package com.zxs.server.interceptor;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * spring boot启动初始化好环境变量时校验服务名是否合法
 * @author zc
 */
public class ApplicationNameCheckListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    /**
     * 兼容历史注册服务名
     */
    private static final String[] LOCAL_ILLEGAL_SUFFIX = new String[]{"dev","test","DEV","TEST"};

    /**
     * 开发环境Apollo IP地址
     */
    private static final String DEV_IP = "http://192.168.20.51:8080";

    /**
     * 测试环境Apollo IP地址
     */
    private static final String TEST_IP = "http://192.168.20.222:8080";

    /**
     * Apollo 域名
     */
    private static final String DOMAIN_NAME = "jgw.apollo.com";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        /**
         * 判断是否需要执行校验逻辑
         */
        boolean needInterceptor = whetherNeedCheck(environment);

        if (needInterceptor){
            /**
             * 执行服务名校验
             */
            checkApplicationName(environment);
            /**
             * 执行Apollo与consul环境是否统一校验
             */
            checkUniqueEnvironment(environment);
        }
    }

    /**
     * 校验服务名
     * @param environment
     */
    private void checkApplicationName(ConfigurableEnvironment environment) {
        String applicationName = environment.getProperty("spring.application.name");
        String notLocalApplicationName = environment.getProperty("spring.application.name2","hydra-intelligent-planting");
        if (!notLocalApplicationName.isEmpty() && notLocalApplicationName.equals(applicationName)){
            throw new RuntimeException("服务名非法："+applicationName);
        }
        for (String illegalSuffix : LOCAL_ILLEGAL_SUFFIX) {
            boolean b = applicationName.endsWith(illegalSuffix);
            if (b){
                throw new RuntimeException("服务名非法："+applicationName);
            }
        }
    }
    /**
     * 校验环境是否统一
     * 1.Apollo的meta与namespaces是否统一
     * 2.consul是否与Apollo统一（暂未完成）
     * @param environment
     */
    private void checkUniqueEnvironment(ConfigurableEnvironment environment) {
        String namespaces = environment.getProperty("apollo.bootstrap.namespaces");
        /**
         * 当前启动环境
         */
        boolean isDev = judgeEnvironment(environment);
        String testSpace = "test.public.conf";
        /**
         * 目前不是dev就是test
         */
        if (isDev && namespaces.contains(testSpace)){
            throw new RuntimeException("当前apolloMeta为开发环境地址,namespaces中有测试环境配置："+namespaces);
        }
        //TODO Apollo与consul的统一校验还未完成

    }

    /**
     * 判断当前环境
     * @param environment
     * @return
     */
    private boolean judgeEnvironment(ConfigurableEnvironment environment) {
        boolean isDev =false;
        try {
            String apolloMeta = environment.getProperty("apollo.meta");
            if (apolloMeta.contains(DOMAIN_NAME)){
                apolloMeta = "http://"+ InetAddress.getByName(DOMAIN_NAME).getHostAddress()+":8080";
            }
            if (DEV_IP.equals(apolloMeta)){
                isDev =true;
            }else if (TEST_IP.equals(apolloMeta)){
                isDev =false;
            }
        } catch (UnknownHostException e) {
           e.printStackTrace();
        }
        return isDev;
    }

    /**
     * 判断是否是本地启动
     * @param environment
     * @return
     */
    private boolean whetherNeedCheck(ConfigurableEnvironment environment) {
        String run_mode = environment.getProperty("run_mode");
        if (StringUtils.isNotBlank(run_mode)){
            return true ;
        }
        return false;
    }
}
