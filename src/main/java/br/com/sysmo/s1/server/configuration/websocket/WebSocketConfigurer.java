package br.com.sysmo.s1.server.configuration.websocket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class WebSocketConfigurer {

    @Bean()
    public List<CustomWebSocketMessageBrokerConfigurer> getConfigurers() {
        return new ArrayList<>();
    };
}
