package br.com.sysmo.s1.server.configuration.websocket.websocket.registration;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;

public class CustomTaskExecutorRegistration {

    private final ThreadPoolTaskExecutor taskExecutor;

    @Nullable
    private Integer corePoolSize;

    @Nullable
    private Integer maxPoolSize;

    @Nullable
    private Integer keepAliveSeconds;

    @Nullable
    private Integer queueCapacity;

    public CustomTaskExecutorRegistration() {
        this.taskExecutor = new ThreadPoolTaskExecutor();
        this.taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        this.taskExecutor.setAllowCoreThreadTimeOut(true);
    }

    public CustomTaskExecutorRegistration(ThreadPoolTaskExecutor taskExecutor) {
        Assert.notNull(taskExecutor, "ThreadPoolTaskExecutor must not be null");
        this.taskExecutor = taskExecutor;
    }

    public CustomTaskExecutorRegistration corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public CustomTaskExecutorRegistration maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
        return this;
    }

    public CustomTaskExecutorRegistration keepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
        return this;
    }

    public CustomTaskExecutorRegistration queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        if (this.corePoolSize != null) {
            this.taskExecutor.setCorePoolSize(this.corePoolSize);
        }
        if (this.maxPoolSize != null) {
            this.taskExecutor.setMaxPoolSize(this.maxPoolSize);
        }
        if (this.keepAliveSeconds != null) {
            this.taskExecutor.setKeepAliveSeconds(this.keepAliveSeconds);
        }
        if (this.queueCapacity != null) {
            this.taskExecutor.setQueueCapacity(this.queueCapacity);
        }
        return this.taskExecutor;
    }

}
