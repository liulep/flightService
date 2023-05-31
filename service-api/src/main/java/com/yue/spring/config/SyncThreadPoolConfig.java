package com.yue.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class SyncThreadPoolConfig {

    @Bean(name="threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor getThreadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        //创建核心线程数
        threadPoolTaskExecutor.setCorePoolSize(10);
        //线程池维护线程的最大数量
        threadPoolTaskExecutor.setMaxPoolSize(100);
        //缓存队列
        threadPoolTaskExecutor.setQueueCapacity(50);
        //线程的空闲事件，当超过了核心线程数之外的线程在达到指定的空闲时间会被销毁
        threadPoolTaskExecutor.setKeepAliveSeconds(200);
        //异步方法内部线的名称
        threadPoolTaskExecutor.setThreadNamePrefix("yueue-thread-");
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
