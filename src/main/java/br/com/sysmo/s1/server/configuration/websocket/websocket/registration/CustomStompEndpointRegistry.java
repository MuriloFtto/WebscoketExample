package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.util.UrlPathHelper;

public interface CustomStompEndpointRegistry {

    StompWebSocketEndpointRegistration addEndpoint(String... paths);

    void setOrder(int order);

    void setUrlPathHelper(UrlPathHelper urlPathHelper);

}
