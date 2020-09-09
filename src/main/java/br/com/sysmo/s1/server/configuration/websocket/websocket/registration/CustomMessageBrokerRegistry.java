package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.stomp.StompBrokerRelayMessageHandler;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

public class CustomMessageBrokerRegistry {

    private final SubscribableChannel clientInboundChannel;

    private final MessageChannel clientOutboundChannel;

    @Nullable
    private CustomSimpleBrokerRegistration simpleBrokerRegistration;

    @Nullable
    private CustomStompBrokerRelayRegistration brokerRelayRegistration;

    private final CustomChannelRegistration brokerChannelRegistration = new CustomChannelRegistration();

    @Nullable
    private String[] applicationDestinationPrefixes;

    @Nullable
    private String userDestinationPrefix;

    @Nullable
    private Integer userRegistryOrder;

    @Nullable
    private PathMatcher pathMatcher;

    @Nullable
    private Integer cacheLimit;

    private boolean preservePublishOrder;

    public CustomMessageBrokerRegistry(SubscribableChannel clientInboundChannel, MessageChannel clientOutboundChannel) {
        Assert.notNull(clientInboundChannel, "Inbound channel must not be null");
        Assert.notNull(clientOutboundChannel, "Outbound channel must not be null");
        this.clientInboundChannel = clientInboundChannel;
        this.clientOutboundChannel = clientOutboundChannel;
    }

    public CustomSimpleBrokerRegistration enableSimpleBroker(String... destinationPrefixes) {
        this.simpleBrokerRegistration = new CustomSimpleBrokerRegistration(this.clientInboundChannel, this.clientOutboundChannel,
                destinationPrefixes);
        return this.simpleBrokerRegistration;
    }

    public CustomStompBrokerRelayRegistration enableStompBrokerRelay(String... destinationPrefixes) {
        this.brokerRelayRegistration = new CustomStompBrokerRelayRegistration(this.clientInboundChannel, this.clientOutboundChannel,
                destinationPrefixes);
        return this.brokerRelayRegistration;
    }

    public CustomChannelRegistration configureBrokerChannel() {
        return this.brokerChannelRegistration;
    }

    public CustomChannelRegistration getBrokerChannelRegistration() {
        return this.brokerChannelRegistration;
    }

    public String getUserDestinationBroadcast() {
        return (this.brokerRelayRegistration != null ? this.brokerRelayRegistration.getUserDestinationBroadcast() : null);
    }

    @Nullable
    public String getUserRegistryBroadcast() {
        return (this.brokerRelayRegistration != null ? this.brokerRelayRegistration.getUserRegistryBroadcast() : null);
    }

    public CustomMessageBrokerRegistry setApplicationDestinationPrefixes(String... prefixes) {
        this.applicationDestinationPrefixes = prefixes;
        return this;
    }

    @Nullable
    public Collection<String> getApplicationDestinationPrefixes() {
        return (this.applicationDestinationPrefixes != null ? Arrays.asList(this.applicationDestinationPrefixes) : null);
    }

    public CustomMessageBrokerRegistry setUserDestinationPrefix(String destinationPrefix) {
        this.userDestinationPrefix = destinationPrefix;
        return this;
    }

    @Nullable
    public String getUserDestinationPrefix() {
        return this.userDestinationPrefix;
    }

    public void setUserRegistryOrder(int order) {
        this.userRegistryOrder = order;
    }

    @Nullable
    public Integer getUserRegistryOrder() {
        return this.userRegistryOrder;
    }

    public CustomMessageBrokerRegistry setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
        return this;
    }

    @Nullable
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }

    public CustomMessageBrokerRegistry setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
        return this;
    }

    public CustomMessageBrokerRegistry setPreservePublishOrder(boolean preservePublishOrder) {
        this.preservePublishOrder = preservePublishOrder;
        return this;
    }

    @Nullable
    public SimpleBrokerMessageHandler getSimpleBroker(SubscribableChannel brokerChannel) {
        if (this.simpleBrokerRegistration == null && this.brokerRelayRegistration == null) {
            enableSimpleBroker();
        }
        if (this.simpleBrokerRegistration != null) {
            SimpleBrokerMessageHandler handler = this.simpleBrokerRegistration.getMessageHandler(brokerChannel);
            handler.setPathMatcher(this.pathMatcher);
            handler.setCacheLimit(this.cacheLimit);
            handler.setPreservePublishOrder(this.preservePublishOrder);
            return handler;
        }
        return null;
    }

    @Nullable
    public StompBrokerRelayMessageHandler getStompBrokerRelay(SubscribableChannel brokerChannel) {
        if (this.brokerRelayRegistration != null) {
            StompBrokerRelayMessageHandler relay = this.brokerRelayRegistration.getMessageHandler(brokerChannel);
            relay.setPreservePublishOrder(this.preservePublishOrder);
            return relay;
        }
        return null;
    }

}
