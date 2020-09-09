package br.com.sysmo.s1.server.configuration.websocket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;
import org.springframework.messaging.simp.broker.AbstractBrokerMessageHandler;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompBrokerRelayMessageHandler;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.messaging.simp.user.MultiServerUserRegistry;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.messaging.simp.user.UserDestinationMessageHandler;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.simp.user.UserRegistryMessageHandler;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.PathMatcher;
import org.springframework.validation.Validator;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.messaging.DefaultSimpUserRegistry;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomMessageBrokerRegistry;
import br.com.sysmo.s1.server.configuration.websocket.websocket.registration.CustomWebSocketMessageBrokerConfigurer;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(value = { BrokerRegistration.class, CustomTransportRegistration.class, WebSocketTaskSchedule.class, EndPointConfigurer.class })
@ConditionalOnProperty(value = "websocket.enabled", matchIfMissing = false)
public class MessaginHandler {

    private static final boolean jackson2Present = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
            MessaginHandler.class.getClassLoader());

    @Autowired
    @Qualifier(value = "getBrokerRegistry")
    private CustomMessageBrokerRegistry getBrokerRegistry;

    @Autowired
    @Qualifier(value = "customBrokerChannel")
    private AbstractSubscribableChannel customBrokerChannel;

    @Autowired
    private List<CustomWebSocketMessageBrokerConfigurer> configurers;

    @Autowired
    @Qualifier(value = "customClientInboundChannel")
    private AbstractSubscribableChannel customClientInboundChannel;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SimpUserRegistry customUserRegistry() {
        SimpUserRegistry registry = createLocalUserRegistry(getBrokerRegistry.getUserRegistryOrder());

        boolean broadcast = getBrokerRegistry.getUserRegistryBroadcast() != null;
        return (broadcast ? new MultiServerUserRegistry(registry) : registry);
    }

    @Bean
    public MessageHandler customUserRegistryMessageHandler(
            @Autowired @Qualifier("customBrokerMessagingTemplate") SimpMessagingTemplate customBrokerMessagingTemplate,
            @Autowired @Qualifier(value = "customMessageBrokerTaskScheduler") ThreadPoolTaskScheduler customMessageBrokerTaskScheduler,
            @Autowired @Qualifier("customUserRegistry") SimpUserRegistry userRegistry) {

        if (getBrokerRegistry.getUserRegistryBroadcast() == null) {
            return null;
        }

        Assert.isInstanceOf(MultiServerUserRegistry.class, userRegistry, "MultiServerUserRegistry required");
        return new UserRegistryMessageHandler((MultiServerUserRegistry) userRegistry, customBrokerMessagingTemplate,
                getBrokerRegistry.getUserRegistryBroadcast(), customMessageBrokerTaskScheduler);
    }

    @Bean
    public UserDestinationResolver customUserDestinationResolver() {
        DefaultUserDestinationResolver resolver = new DefaultUserDestinationResolver(customUserRegistry());
        String prefix = getBrokerRegistry.getUserDestinationPrefix();
        if (prefix != null) {
            resolver.setUserDestinationPrefix(prefix);
        }
        return resolver;
    }

    @Bean
    public UserDestinationMessageHandler customUserDestinationMessageHandler(
            @Autowired @Qualifier("customUserDestinationResolver") UserDestinationResolver customUserDestinationResolver) {

        UserDestinationMessageHandler handler = new UserDestinationMessageHandler(customClientInboundChannel, customBrokerChannel,
                customUserDestinationResolver);
        String destination = getBrokerRegistry.getUserDestinationBroadcast();

        if (destination != null) {
            handler.setBroadcastDestination(destination);
        }
        return handler;
    }

    @Bean
    public AbstractBrokerMessageHandler customStompBrokerRelayMessageHandler(
            @Autowired @Qualifier("customUserRegistryMessageHandler") MessageHandler customUserRegistryMessageHandler,
            @Autowired @Qualifier("customUserDestinationMessageHandler") UserDestinationMessageHandler customUserDestinationMessageHandler,
            @Autowired @Qualifier("customUserDestinationResolver") UserDestinationResolver customUserDestinationResolver) {
        StompBrokerRelayMessageHandler handler = getBrokerRegistry.getStompBrokerRelay(customBrokerChannel);
        if (handler == null) {
            return null;
        }
        Map<String, MessageHandler> subscriptions = new HashMap<>(4);
        String destination = getBrokerRegistry.getUserDestinationBroadcast();
        if (destination != null) {
            subscriptions.put(destination, customUserDestinationMessageHandler);
        }
        destination = getBrokerRegistry.getUserRegistryBroadcast();
        if (destination != null) {
            subscriptions.put(destination, customUserRegistryMessageHandler);
        }
        handler.setSystemSubscriptions(subscriptions);
        updateUserDestinationResolver(handler, customUserDestinationResolver);

        return handler;
    }

    @Bean
    public CompositeMessageConverter customBrokerMessageConverter() {
        List<MessageConverter> converters = new ArrayList<>();
        boolean registerDefaults = configureMessageConverters(converters);
        if (registerDefaults) {
            converters.add(new StringMessageConverter());
            converters.add(new ByteArrayMessageConverter());
            if (jackson2Present) {
                converters.add(createJacksonConverter());
            }
        }
        return new CompositeMessageConverter(converters);
    }

    @Bean
    public SimpMessagingTemplate customBrokerMessagingTemplate(
            @Autowired @Qualifier("customBrokerMessageConverter") CompositeMessageConverter customBrokerMessageConverter) {
        SimpMessagingTemplate template = new SimpMessagingTemplate(customBrokerChannel);
        String prefix = getBrokerRegistry.getUserDestinationPrefix();
        if (prefix != null) {
            template.setUserDestinationPrefix(prefix);
        }
        template.setMessageConverter(customBrokerMessageConverter);
        return template;
    }

    @Bean
    public WebSocketMessageBrokerStats customWebSocketMessageBrokerStats(
            @Autowired @Qualifier("customStompBrokerRelayMessageHandler") AbstractBrokerMessageHandler relayBean,
            @Autowired @Qualifier(value = "customClientOutboundChannelExecutor") TaskExecutor customClientOutboundChannelExecutor,
            @Autowired @Qualifier(value = "customClientInboundChannelExecutor") TaskExecutor customClientInboundChannelExecutor,
            @Autowired @Qualifier(value = "customSubProtocolWebSocketHandler") WebSocketHandler customSubProtocolWebSocketHandler,
            @Autowired @Qualifier(value = "customMessageBrokerTaskScheduler") ThreadPoolTaskScheduler customMessageBrokerTaskScheduler) {

        WebSocketMessageBrokerStats stats = new WebSocketMessageBrokerStats();
        stats.setSubProtocolWebSocketHandler((SubProtocolWebSocketHandler) customSubProtocolWebSocketHandler);
        if (relayBean instanceof StompBrokerRelayMessageHandler) {
            stats.setStompBrokerRelay((StompBrokerRelayMessageHandler) relayBean);
        }
        stats.setInboundChannelExecutor(customClientInboundChannelExecutor);
        stats.setOutboundChannelExecutor(customClientOutboundChannelExecutor);
        stats.setSockJsTaskScheduler(customMessageBrokerTaskScheduler);
        return stats;
    }

    @Bean
    public AbstractBrokerMessageHandler customSimpleBrokerMessageHandler(
            @Autowired @Qualifier("customUserDestinationResolver") UserDestinationResolver customUserDestinationResolver) {
        SimpleBrokerMessageHandler handler = getBrokerRegistry.getSimpleBroker(customBrokerChannel);
        if (handler == null) {
            return null;
        }
        updateUserDestinationResolver(handler, customUserDestinationResolver);

        return handler;
    }

    @Bean
    public SimpAnnotationMethodMessageHandler createAnnotationMethodMessageHandler(
            @Autowired @Qualifier("customBrokerMessagingTemplate") SimpMessagingTemplate customBrokerMessagingTemplate,
            @Autowired @Qualifier(value = "customClientOutboundChannel") AbstractSubscribableChannel customClientOutboundChannel) {
        return new SimpAnnotationMethodMessageHandler(customClientInboundChannel, customClientOutboundChannel, customBrokerMessagingTemplate);
    }

    @Bean
    public SimpAnnotationMethodMessageHandler customSimpAnnotationMethodMessageHandler(
            @Autowired @Qualifier(value = "simpValidator") Validator simpValidator,
            @Autowired @Qualifier("customBrokerMessageConverter") CompositeMessageConverter customBrokerMessageConverter,
            @Autowired @Qualifier("createAnnotationMethodMessageHandler") SimpAnnotationMethodMessageHandler handler) {

        handler.setDestinationPrefixes(getBrokerRegistry.getApplicationDestinationPrefixes());
        handler.setMessageConverter(customBrokerMessageConverter);
        handler.setValidator(simpValidator);

        List<HandlerMethodArgumentResolver> argumentResolvers = new ArrayList<>();
        addArgumentResolvers(argumentResolvers);
        handler.setCustomArgumentResolvers(argumentResolvers);

        List<HandlerMethodReturnValueHandler> returnValueHandlers = new ArrayList<>();
        addReturnValueHandlers(returnValueHandlers);
        handler.setCustomReturnValueHandlers(returnValueHandlers);

        PathMatcher pathMatcher = getBrokerRegistry.getPathMatcher();
        if (pathMatcher != null) {
            handler.setPathMatcher(pathMatcher);
        }
        return handler;
    }

    private void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        for (CustomWebSocketMessageBrokerConfigurer configurer : this.configurers) {
            configurer.addArgumentResolvers(argumentResolvers);
        }
    }

    private void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
        for (CustomWebSocketMessageBrokerConfigurer configurer : this.configurers) {
            configurer.addReturnValueHandlers(returnValueHandlers);
        }
    }

    private MappingJackson2MessageConverter createJacksonConverter() {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setContentTypeResolver(resolver);

        Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.json();
        builder.applicationContext(applicationContext);
        converter.setObjectMapper(builder.build());

        return converter;
    }

    private boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        boolean registerDefaults = true;
        for (CustomWebSocketMessageBrokerConfigurer configurer : this.configurers) {
            if (!configurer.configureMessageConverters(messageConverters)) {
                registerDefaults = false;
            }
        }
        return registerDefaults;
    }

    private void updateUserDestinationResolver(AbstractBrokerMessageHandler handler, UserDestinationResolver customUserDestinationResolver) {
        Collection<String> prefixes = handler.getDestinationPrefixes();

        if (!prefixes.isEmpty() && !prefixes.iterator().next().startsWith("/")) {
            ((DefaultUserDestinationResolver) customUserDestinationResolver).setRemoveLeadingSlash(true);
        }
    }

    private SimpUserRegistry createLocalUserRegistry(@Nullable Integer order) {
        DefaultSimpUserRegistry registry = new DefaultSimpUserRegistry();
        if (order != null) {
            registry.setOrder(order);
        }
        return registry;
    }
}
