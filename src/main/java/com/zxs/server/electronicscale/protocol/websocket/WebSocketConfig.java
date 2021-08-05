package com.zxs.server.electronicscale.protocol.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * websocket配置
 * @author zc
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(connectionHandler(), "/hydra-intelligent-planting/websocket") // 注册handler
                .addInterceptors(new HttpSessionHandshakeInterceptor()) // 添加拦截器
                .setAllowedOrigins("*") ;// 允许非同源访问
//                .withSockJS(); // 使用
    }

    @Bean
    public WebSocketHandler connectionHandler() {
        return new ConnectionHandler();
    }

}