
package br.com.sysmo.s1.server.configuration.websocket;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.SimpSessionScope;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomMessageBrokerRegistry;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = MessaginHandler.class)
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class CustomDelegatingWebSocketMessageBrokerConfiguration {

    @Autowired
    private List<CustomWebSocketMessageBrokerConfigurer> configurers;

    @Autowired
    @Qualifier(value = "getBrokerRegistry")
    private CustomMessageBrokerRegistry getBrokerRegistry;

    @Nullable
    public final PathMatcher getPathMatcher() {
        return getBrokerRegistry.getPathMatcher();
    }

    @Bean
    public static CustomScopeConfigurer customWebSocketScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.addScope("websocket", new SimpSessionScope());
        return configurer;
    }

    @Autowired(required = false)
    public void setConfigurers(List<CustomWebSocketMessageBrokerConfigurer> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.configurers.addAll(configurers);
        }
    }

}
