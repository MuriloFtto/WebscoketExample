package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class CustomChannelRegistration {

    @Nullable
    private CustomTaskExecutorRegistration registration;

    private final List<ChannelInterceptor> interceptors = new ArrayList<>();

    public CustomTaskExecutorRegistration taskExecutor() {
        return taskExecutor(null);
    }

    public CustomTaskExecutorRegistration taskExecutor(@Nullable ThreadPoolTaskExecutor taskExecutor) {
        if (this.registration == null) {
            this.registration = (taskExecutor != null ? new CustomTaskExecutorRegistration(taskExecutor) : new CustomTaskExecutorRegistration());
        }
        return this.registration;
    }

    public CustomChannelRegistration interceptors(ChannelInterceptor... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
        return this;
    }

    public boolean hasTaskExecutor() {
        return (this.registration != null);
    }

    public boolean hasInterceptors() {
        return !this.interceptors.isEmpty();
    }

    public List<ChannelInterceptor> getInterceptors() {
        return this.interceptors;
    }

}
