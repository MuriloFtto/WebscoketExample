package br.com.sysmo.s1.server.configuration.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketTransportRegistration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = { WebSocketConfigurer.class })
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class CustomTransportRegistration {

    @Bean
    public CustomWebSocketTransportRegistration getTransportRegistration(@Autowired List<CustomWebSocketMessageBrokerConfigurer> configurers) {

        CustomWebSocketTransportRegistration transportRegistration = new CustomWebSocketTransportRegistration();

        configurers.forEach(configurer -> configurer.configureWebSocketTransport(transportRegistration));

        return transportRegistration;
    }

}
