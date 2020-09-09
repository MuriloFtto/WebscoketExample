package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

public class CustomWebSocketTransportRegistration {

    @Nullable
    private Integer messageSizeLimit;

    @Nullable
    private Integer sendTimeLimit;

    @Nullable
    private Integer sendBufferSizeLimit;

    @Nullable
    private Integer timeToFirstMessage;

    private final List<WebSocketHandlerDecoratorFactory> decoratorFactories = new ArrayList<>(2);

    public CustomWebSocketTransportRegistration setMessageSizeLimit(int messageSizeLimit) {
        this.messageSizeLimit = messageSizeLimit;
        return this;
    }

    @Nullable
    protected Integer getMessageSizeLimit() {
        return this.messageSizeLimit;
    }

    public CustomWebSocketTransportRegistration setSendTimeLimit(int timeLimit) {
        this.sendTimeLimit = timeLimit;
        return this;
    }

    @Nullable
    protected Integer getSendTimeLimit() {
        return this.sendTimeLimit;
    }

    public CustomWebSocketTransportRegistration setSendBufferSizeLimit(int sendBufferSizeLimit) {
        this.sendBufferSizeLimit = sendBufferSizeLimit;
        return this;
    }

    @Nullable
    protected Integer getSendBufferSizeLimit() {
        return this.sendBufferSizeLimit;
    }

    public CustomWebSocketTransportRegistration setTimeToFirstMessage(int timeToFirstMessage) {
        this.timeToFirstMessage = timeToFirstMessage;
        return this;
    }

    @Nullable
    protected Integer getTimeToFirstMessage() {
        return this.timeToFirstMessage;
    }

    public CustomWebSocketTransportRegistration setDecoratorFactories(WebSocketHandlerDecoratorFactory... factories) {
        this.decoratorFactories.addAll(Arrays.asList(factories));
        return this;
    }

    public CustomWebSocketTransportRegistration addDecoratorFactory(WebSocketHandlerDecoratorFactory factory) {
        this.decoratorFactories.add(factory);
        return this;
    }

    public List<WebSocketHandlerDecoratorFactory> getDecoratorFactories() {
        return this.decoratorFactories;
    }

}
