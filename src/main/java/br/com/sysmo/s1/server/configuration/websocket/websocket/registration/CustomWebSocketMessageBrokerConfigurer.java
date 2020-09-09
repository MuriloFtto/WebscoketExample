package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import java.util.List;

import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;

public interface CustomWebSocketMessageBrokerConfigurer {

    default void registerStompEndpoints(CustomStompEndpointRegistry registry) {
    }

    default void configureWebSocketTransport(CustomWebSocketTransportRegistration registry) {
    }

    default void configureClientInboundChannel(CustomChannelRegistration registration) {
    }

    default void configureClientOutboundChannel(CustomChannelRegistration registration) {
    }

    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    default boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        return true;
    }

    default void configureMessageBroker(CustomMessageBrokerRegistry registry) {
    }

}
