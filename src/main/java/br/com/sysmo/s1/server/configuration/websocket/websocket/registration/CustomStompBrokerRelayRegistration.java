
package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.stomp.StompBrokerRelayMessageHandler;
import org.springframework.messaging.tcp.TcpOperations;
import org.springframework.util.Assert;

public class CustomStompBrokerRelayRegistration extends CustomAbstractBrokerRegistration {

    private String relayHost = "127.0.0.1";

    private int relayPort = 61613;

    private String clientLogin = "guest";

    private String clientPasscode = "guest";

    private String systemLogin = "guest";

    private String systemPasscode = "guest";

    @Nullable
    private Long systemHeartbeatSendInterval;

    @Nullable
    private Long systemHeartbeatReceiveInterval;

    @Nullable
    private String virtualHost;

    @Nullable
    private TcpOperations<byte[]> tcpClient;

    private boolean autoStartup = true;

    @Nullable
    private String userDestinationBroadcast;

    @Nullable
    private String userRegistryBroadcast;

    public CustomStompBrokerRelayRegistration(SubscribableChannel clientInboundChannel, MessageChannel clientOutboundChannel,
            String[] destinationPrefixes) {

        super(clientInboundChannel, clientOutboundChannel, destinationPrefixes);
    }

    public CustomStompBrokerRelayRegistration setRelayHost(String relayHost) {
        Assert.hasText(relayHost, "relayHost must not be empty");
        this.relayHost = relayHost;
        return this;
    }

    public CustomStompBrokerRelayRegistration setRelayPort(int relayPort) {
        this.relayPort = relayPort;
        return this;
    }

    public CustomStompBrokerRelayRegistration setClientLogin(String login) {
        Assert.hasText(login, "clientLogin must not be empty");
        this.clientLogin = login;
        return this;
    }

    public CustomStompBrokerRelayRegistration setClientPasscode(String passcode) {
        Assert.hasText(passcode, "clientPasscode must not be empty");
        this.clientPasscode = passcode;
        return this;
    }

    public CustomStompBrokerRelayRegistration setSystemLogin(String login) {
        Assert.hasText(login, "systemLogin must not be empty");
        this.systemLogin = login;
        return this;
    }

    public CustomStompBrokerRelayRegistration setSystemPasscode(String passcode) {
        Assert.hasText(passcode, "systemPasscode must not be empty");
        this.systemPasscode = passcode;
        return this;
    }

    public CustomStompBrokerRelayRegistration setSystemHeartbeatSendInterval(long systemHeartbeatSendInterval) {
        this.systemHeartbeatSendInterval = systemHeartbeatSendInterval;
        return this;
    }

    public CustomStompBrokerRelayRegistration setSystemHeartbeatReceiveInterval(long heartbeatReceiveInterval) {
        this.systemHeartbeatReceiveInterval = heartbeatReceiveInterval;
        return this;
    }

    public CustomStompBrokerRelayRegistration setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    public void setTcpClient(TcpOperations<byte[]> tcpClient) {
        this.tcpClient = tcpClient;
    }

    public CustomStompBrokerRelayRegistration setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
        return this;
    }

    public CustomStompBrokerRelayRegistration setUserDestinationBroadcast(String destination) {
        this.userDestinationBroadcast = destination;
        return this;
    }

    @Nullable
    protected String getUserDestinationBroadcast() {
        return this.userDestinationBroadcast;
    }

    public CustomStompBrokerRelayRegistration setUserRegistryBroadcast(String destination) {
        this.userRegistryBroadcast = destination;
        return this;
    }

    @Nullable
    protected String getUserRegistryBroadcast() {
        return this.userRegistryBroadcast;
    }

    @Override
    protected StompBrokerRelayMessageHandler getMessageHandler(SubscribableChannel brokerChannel) {

        StompBrokerRelayMessageHandler handler = new StompBrokerRelayMessageHandler(getClientInboundChannel(), getClientOutboundChannel(),
                brokerChannel, getDestinationPrefixes());

        handler.setRelayHost(this.relayHost);
        handler.setRelayPort(this.relayPort);

        handler.setClientLogin(this.clientLogin);
        handler.setClientPasscode(this.clientPasscode);

        handler.setSystemLogin(this.systemLogin);
        handler.setSystemPasscode(this.systemPasscode);

        if (this.systemHeartbeatSendInterval != null) {
            handler.setSystemHeartbeatSendInterval(this.systemHeartbeatSendInterval);
        }
        if (this.systemHeartbeatReceiveInterval != null) {
            handler.setSystemHeartbeatReceiveInterval(this.systemHeartbeatReceiveInterval);
        }
        if (this.virtualHost != null) {
            handler.setVirtualHost(this.virtualHost);
        }
        if (this.tcpClient != null) {
            handler.setTcpClient(this.tcpClient);
        }

        handler.setAutoStartup(this.autoStartup);

        return handler;
    }

}
