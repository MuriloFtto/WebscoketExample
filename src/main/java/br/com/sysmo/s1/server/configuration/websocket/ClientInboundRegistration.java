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

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomChannelRegistration;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomTaskExecutorRegistration;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = WebSocketConfigurer.class)
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class ClientInboundRegistration {

    @Bean
    public CustomChannelRegistration getClientInboundChannelRegistration(@Autowired List<CustomWebSocketMessageBrokerConfigurer> configurers) {

        CustomChannelRegistration registration = new CustomChannelRegistration();

        configurers.forEach(configurer -> configurer.configureClientInboundChannel(registration));

        registration.interceptors(new ImmutableMessageChannelInterceptor());

        return registration;
    }

    @Bean
    public TaskExecutor customClientInboundChannelExecutor(
            @Autowired @Qualifier("getClientInboundChannelRegistration") CustomChannelRegistration getClientInboundChannelRegistration) {
        CustomTaskExecutorRegistration reg = getClientInboundChannelRegistration.taskExecutor();
        ThreadPoolTaskExecutor executor = reg.getTaskExecutor();
        executor.setThreadNamePrefix("clientInboundChannel-");
        return executor;
    }

    @Bean
    public AbstractSubscribableChannel customClientInboundChannel(
            @Autowired @Qualifier("getClientInboundChannelRegistration") CustomChannelRegistration getClientInboundChannelRegistration,
            @Autowired @Qualifier("customClientInboundChannelExecutor") TaskExecutor customClientInboundChannelExecutor) {

        ExecutorSubscribableChannel channel = new ExecutorSubscribableChannel(customClientInboundChannelExecutor);
        channel.setLogger(SimpLogging.forLog(channel.getLogger()));

        CustomChannelRegistration reg = getClientInboundChannelRegistration;
        if (reg.hasInterceptors()) {
            channel.setInterceptors(reg.getInterceptors());
        }

        return channel;
    }

}
