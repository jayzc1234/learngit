package com.zxs.server.config;

import net.app315.hydra.intelligent.planting.server.interceptor.RestTemplateRequestInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * @author jgw136
 *
 */
@Configuration
public class RestTemplateConfig {
	
	/*@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return restTemplate;
	}*/
	
	@LoadBalanced
    @Bean
	public RestTemplate restTemplate() {
		/*RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));//解决中文乱码
        */
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new RestTemplateRequestInterceptor());
		return restTemplate;
	}
}
