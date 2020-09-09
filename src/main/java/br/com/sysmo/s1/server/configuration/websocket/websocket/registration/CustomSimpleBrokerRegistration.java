
package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler;
import org.springframework.scheduling.TaskScheduler;

public class CustomSimpleBrokerRegistration extends CustomAbstractBrokerRegistration {

    @Nullable
    private TaskScheduler taskScheduler;

    @Nullable
    private long[] heartbeat;

    @Nullable
    private String selectorHeaderName = "selector";

    public CustomSimpleBrokerRegistration(SubscribableChannel inChannel, MessageChannel outChannel, String[] prefixes) {
        super(inChannel, outChannel, prefixes);
    }

    public CustomSimpleBrokerRegistration setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        return this;
    }

    public CustomSimpleBrokerRegistration setHeartbeatValue(long[] heartbeat) {
        this.heartbeat = heartbeat;
        return this;
    }

    public void setSelectorHeaderName(@Nullable String selectorHeaderName) {
        this.selectorHeaderName = selectorHeaderName;
    }

    @Override
    protected SimpleBrokerMessageHandler getMessageHandler(SubscribableChannel brokerChannel) {
        SimpleBrokerMessageHandler handler = new SimpleBrokerMessageHandler(getClientInboundChannel(), getClientOutboundChannel(), brokerChannel,
                getDestinationPrefixes());
        if (this.taskScheduler != null) {
            handler.setTaskScheduler(this.taskScheduler);
        }
        if (this.heartbeat != null) {
            handler.setHeartbeatValue(this.heartbeat);
        }
        handler.setSelectorHeaderName(this.selectorHeaderName);
        return handler;
    }

}
