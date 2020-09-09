package br.com.sysmo.s1.server.configuration.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebMvcStompEndpointRegistry;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketTransportRegistration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = { CustomTransportRegistration.class, WebSocketTaskSchedule.class })
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class EndPointConfigurer {

    @Bean
    public HandlerMapping customStompWebSocketHandlerMapping(@Autowired List<CustomWebSocketMessageBrokerConfigurer> configurers,
            @Autowired @Qualifier(value = "getTransportRegistration") CustomWebSocketTransportRegistration transportRegistration,
            @Autowired @Qualifier(value = "customSubProtocolWebSocketHandler") WebSocketHandler subProtocolWebSocketHandler,
            @Autowired @Qualifier(value = "customMessageBrokerTaskScheduler") ThreadPoolTaskScheduler messageBrokerTaskScheduler) {

        WebSocketHandler handler = decorateWebSocketHandler(subProtocolWebSocketHandler, transportRegistration);

        CustomWebMvcStompEndpointRegistry registry = new CustomWebMvcStompEndpointRegistry(handler, transportRegistration,
                messageBrokerTaskScheduler);

        configurers.forEach(configurer -> configurer.registerStompEndpoints(registry));

        return registry.getHandlerMapping();
    }

    private WebSocketHandler decorateWebSocketHandler(WebSocketHandler handler, CustomWebSocketTransportRegistration getTransportRegistration) {

        for (WebSocketHandlerDecoratorFactory factory : getTransportRegistration.getDecoratorFactories()) {
            handler = factory.decorate(handler);
        }

        return handler;
    }

}
