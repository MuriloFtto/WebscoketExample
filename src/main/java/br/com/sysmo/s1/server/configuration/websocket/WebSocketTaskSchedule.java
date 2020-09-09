package br.com.sysmo.s1.server.configuration.websocket;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = { BrokerRegistration.class })
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class WebSocketTaskSchedule {

    @Bean()
    public ThreadPoolTaskScheduler customMessageBrokerTaskScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("MessageBroker-");
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        scheduler.setRemoveOnCancelPolicy(true);

        return scheduler;
    }

}
