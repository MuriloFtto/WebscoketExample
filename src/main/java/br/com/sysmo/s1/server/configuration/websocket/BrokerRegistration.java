package br.com.sysmo.s1.server.configuration.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.simp.SimpLogging;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.ImmutableMessageChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomChannelRegistration;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomMessageBrokerRegistry;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = { ClientOutboundRegistration.class, ClientInboundRegistration.class })
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class BrokerRegistration {

    @Autowired
    @Qualifier(value = "customClientInboundChannel")
    AbstractSubscribableChannel customClientInboundChannel;

    @Autowired
    @Qualifier(value = "customClientOutboundChannel")
    private AbstractSubscribableChannel customClientOutboundChannel;

    @Bean
    public CustomMessageBrokerRegistry getBrokerRegistry(@Autowired List<CustomWebSocketMessageBrokerConfigurer> configurers) {

        CustomMessageBrokerRegistry registry = new CustomMessageBrokerRegistry(customClientInboundChannel, customClientOutboundChannel);

        configurers.forEach(configurer -> configurer.configureMessageBroker(registry));

        return registry;
    }

    @Bean
    public TaskExecutor customBrokerChannelExecutor(@Autowired CustomMessageBrokerRegistry getBrokerRegistry) {
        CustomChannelRegistration reg = getBrokerRegistry.getBrokerChannelRegistration();
        ThreadPoolTaskExecutor executor;
        if (reg.hasTaskExecutor()) {
            executor = reg.taskExecutor().getTaskExecutor();
        } else {
            // Should never be used
            executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(0);
            executor.setMaxPoolSize(1);
            executor.setQueueCapacity(0);
        }
        executor.setThreadNamePrefix("brokerChannel-");
        return executor;
    }

    @Bean
    public AbstractSubscribableChannel customBrokerChannel(@Autowired CustomMessageBrokerRegistry brokerRegistry,
            @Autowired @Qualifier("customBrokerChannelExecutor") TaskExecutor taskExecutor) {
        CustomChannelRegistration reg = brokerRegistry.getBrokerChannelRegistration();
        ExecutorSubscribableChannel channel = (reg.hasTaskExecutor() ? new ExecutorSubscribableChannel(taskExecutor)
                : new ExecutorSubscribableChannel());
        reg.interceptors(new ImmutableMessageChannelInterceptor());
        channel.setLogger(SimpLogging.forLog(channel.getLogger()));
        channel.setInterceptors(reg.getInterceptors());
        return channel;
    }

    @Bean
    public WebSocketHandler customSubProtocolWebSocketHandler() {

        SubProtocolWebSocketHandler subProtocolWebSocketHandler = new SubProtocolWebSocketHandler(customClientInboundChannel,
                customClientOutboundChannel);
        subProtocolWebSocketHandler.addProtocolHandler(new StompSubProtocolHandler());

        return subProtocolWebSocketHandler;
    }

}
