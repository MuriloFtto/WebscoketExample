package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebMvcStompWebSocketEndpointRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;
import org.springframework.web.socket.server.support.WebSocketHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

public class CustomWebMvcStompEndpointRegistry implements CustomStompEndpointRegistry {

    private final WebSocketHandler webSocketHandler;

    private final TaskScheduler sockJsScheduler;

    private int order = 1;

    @Nullable
    private UrlPathHelper urlPathHelper;

    private final SubProtocolWebSocketHandler subProtocolWebSocketHandler;

    private final List<WebMvcStompWebSocketEndpointRegistration> registrations = new ArrayList<>();

    public CustomWebMvcStompEndpointRegistry(WebSocketHandler webSocketHandler, CustomWebSocketTransportRegistration transportRegistration,
            TaskScheduler defaultSockJsTaskScheduler) {

        Assert.notNull(webSocketHandler, "WebSocketHandler is required ");
        Assert.notNull(transportRegistration, "WebSocketTransportRegistration is required");

        this.webSocketHandler = webSocketHandler;
        this.subProtocolWebSocketHandler = unwrapSubProtocolWebSocketHandler(webSocketHandler);

        if (transportRegistration.getSendTimeLimit() != null) {
            this.subProtocolWebSocketHandler.setSendTimeLimit(transportRegistration.getSendTimeLimit());
        }
        if (transportRegistration.getSendBufferSizeLimit() != null) {
            this.subProtocolWebSocketHandler.setSendBufferSizeLimit(transportRegistration.getSendBufferSizeLimit());
        }
        if (transportRegistration.getTimeToFirstMessage() != null) {
            this.subProtocolWebSocketHandler.setTimeToFirstMessage(transportRegistration.getTimeToFirstMessage());
        }

        this.sockJsScheduler = defaultSockJsTaskScheduler;
    }

    private static SubProtocolWebSocketHandler unwrapSubProtocolWebSocketHandler(WebSocketHandler handler) {
        WebSocketHandler actual = WebSocketHandlerDecorator.unwrap(handler);
        if (!(actual instanceof SubProtocolWebSocketHandler)) {
            throw new IllegalArgumentException("No SubProtocolWebSocketHandler in " + handler);
        }
        return (SubProtocolWebSocketHandler) actual;
    }

    @Override
    public StompWebSocketEndpointRegistration addEndpoint(String... paths) {
        WebMvcStompWebSocketEndpointRegistration registration = new WebMvcStompWebSocketEndpointRegistration(paths, this.webSocketHandler,
                this.sockJsScheduler);
        this.registrations.add(registration);
        return registration;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    protected int getOrder() {
        return this.order;
    }

    @Override
    public void setUrlPathHelper(@Nullable UrlPathHelper urlPathHelper) {
        this.urlPathHelper = urlPathHelper;
    }

    @Nullable
    protected UrlPathHelper getUrlPathHelper() {
        return this.urlPathHelper;
    }

    public AbstractHandlerMapping getHandlerMapping() {
        Map<String, Object> urlMap = new LinkedHashMap<>();
        for (WebMvcStompWebSocketEndpointRegistration registration : this.registrations) {
            MultiValueMap<HttpRequestHandler, String> mappings = registration.getMappings();
            mappings.forEach((httpHandler, patterns) -> {
                for (String pattern : patterns) {
                    urlMap.put(pattern, httpHandler);
                }
            });
        }
        WebSocketHandlerMapping hm = new WebSocketHandlerMapping();
        hm.setUrlMap(urlMap);
        hm.setOrder(this.order);
        if (this.urlPathHelper != null) {
            hm.setUrlPathHelper(this.urlPathHelper);
        }
        return hm;
    }

}
